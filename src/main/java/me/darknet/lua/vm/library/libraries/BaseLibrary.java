package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.darknet.lua.vm.value.NilValue.NIL;

public class BaseLibrary extends Library {

	private static final String VERSION = "Lua 5.1";

	public BaseLibrary() {
		super("base", "");
		set("_VERSION", new StringValue(VERSION));
	}

	public int lua_assert(ExecutionContext ctx) {
		Value value = ctx.getRequired(0);
		if (!value.asBoolean()) {
			String msg = "assertion failed!";
			if (ctx.has(1)) {
				Value msgValue = ctx.get(1);
				if (!msgValue.isNil()) msg = msgValue.asString();
			}
			ctx.throwError(msg);
		}
		return ctx.size();
	}

	public int lua_collectgarbage(ExecutionContext ctx) {
		ctx.push(new NumberValue(0));
		return 1;
	}

	public int lua_dofile(ExecutionContext ctx) {
		return ctx.size();
	}

	public int lua_error(ExecutionContext ctx) {
		int level = ctx.optionalInt(2, 1);
		String msg = ctx.optionalString(1, "an error occurred");
		ctx.throwError(msg);
		return 0; // execution quits
	}

	public int lua_gcinfo(ExecutionContext ctx) {
		ctx.push(new NumberValue(0));
		return 1;
	}

	public int lua_getfenv(ExecutionContext ctx) {
		if (ctx.has(0)) {
			Value value = ctx.getRequired(0);
			if (value.getType() != Type.FUNCTION) {
				ctx.throwError("bad argument #1 to 'getfenv' (function expected)");
			}
			ClosureValue closure = (ClosureValue) value;
			ctx.push(new TableValue(closure.getClosure().getEnv()));
		} else {
			ctx.push(new TableValue(ctx.getEnv()));
		}
		return 1;
	}

	public int lua_getmetatable(ExecutionContext ctx) {
		Table meta = ctx.getHelper().getMetatable(ctx.getRequired(0));
		if (meta == null) {
			ctx.push(NIL);
		} else {
			// check if __metatable field exists
			if (meta.has("__metatable")) {
				ctx.push(meta.get("__metatable"));
			} else {
				ctx.push(new TableValue(meta));
			}
		}
		return 1;
	}

	public int lua_setmetatable(ExecutionContext ctx) {
		Value table = ctx.getRequired(0);
		Value meta = ctx.getRequired(1);
		if (meta.getType() != Type.NIL && meta.getType() != Type.TABLE) {
			ctx.throwError("bad argument #2 to 'setmetatable' (nil or table expected)");
		}
		if (!ctx.getHelper().attemptFindMetaobject(table, "__metatable").isNil()) {
			ctx.throwError("cannot change a protected metatable");
		}
		ctx.getHelper().setMetatable(ctx, table, meta);
		ctx.push(table);
		return 1;
	}

	public int lua_pcall(ExecutionContext ctx) {
		ClosureValue closure = (ClosureValue) ctx.get(0);
		Closure cl = closure.getClosure();

		ExecutionContext newContext = ctx.getHelper().prepareCtx(ctx, cl, ctx.reg(0), -1);

		ctx.getHelper().invoke(newContext);

		boolean status = newContext.getError() != null;

		ctx.push(new BooleanValue(status));
		if (!status) ctx.push(new StringValue(newContext.getError().print()));

		ctx.insert(ctx.getTop(), ctx.getBase());

		return ctx.getTop() - ctx.getBase();
	}

	public int lua_xpcall(ExecutionContext ctx) {

		// TODO: stack offsets are not correct, res1 of errFunc gets put at reg(3) instead of reg(1)
		// probably requires manual stack management

		ClosureValue closure = (ClosureValue) ctx.get(0);
		Closure cl = closure.getClosure();
		ClosureValue errorHandler = (ClosureValue) ctx.get(1);
		Closure eh = errorHandler.getClosure();

		int top = ctx.getTop(); // remember top

		ExecutionContext newContext = ctx.getHelper().prepareCtx(ctx, cl, ctx.reg(0), -1);
		newContext.setErrorHandler(eh);

		ctx.getHelper().invoke(newContext);

		boolean status = newContext.getError() == null;

		if (!status) {
			ctx.setTop(top); // restore top
			ctx.push(newContext.getErrorHandlerReturn());
		}

		ctx.push(new BooleanValue(status));

		ctx.insert(ctx.getTop(), ctx.getBase());

		return ctx.getTop() - ctx.getBase();
	}


	public int lua_print(ExecutionContext ctx) {
		int n = ctx.getTop() - ctx.getBase();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(ctx.get(i).asString());
			if (n > 1) sb.append("\t");
		}
		System.out.println(sb);
		return 0; // no return values
	}

	public int lua_pairs(ExecutionContext ctx) {
		ctx.push(this.get("next"));
		ctx.push(ctx.get(0));
		ctx.push(NIL);
		return 3;
	}

	public int lua_next(ExecutionContext ctx) {
		Value value = ctx.get(0);
		if (value.getType() != Type.TABLE) ctx.throwError("bad argument #1 to 'next' (table expected)");
		Value key = ctx.has(1) ? ctx.get(1) : NIL;
		TableValue tv = (TableValue) value;
		Table table = tv.getTable();
		if (key.isNil()) {
			// return first key
			if (table.getArray().size() != 0) {
				// return the last element
				ctx.push(new NumberValue(table.getArray().size() - 1));
				ctx.push(table.getArray().get(table.getArray().size() - 1));
				return 2;
			} else {
				// return the first key in the hashmap
				for (String k : table.getTable().keySet()) {
					ctx.push(new StringValue(k));
					ctx.push(table.get(k));
					return 2;
				}
			}
		} else {
			// if it is a number then access array part
			if (key.getType() == Type.NUMBER) {
				int index = (int) key.asNumber();
				if (index > 0 && index < table.getArray().size()) {
					// now get the next element in the array, if the key is the last element then return nil
					ctx.push(new NumberValue(index + 1));
					if (index + 1 < table.getArray().size()) {
						ctx.push(table.getArray().get(index + 1));
					} else {
						ctx.push(NIL);
					}
				} else {
					ctx.throwError("invalid key to 'next'");
				}
			} else {
				// try to find the key in the hashmap
				String keyStr = key.asString();
				// the keys are searched in reverse order
				Map<String, Value> map = table.getTable();
				List<String> keys = new ArrayList<>(map.keySet());
				for (int i = 0; i < keys.size(); i++) {
					String k = keys.get(i);
					if (k.equals(keyStr)) {
						if (i + 1 < keys.size()) {
							ctx.push(new StringValue(keys.get(i + 1)));
							ctx.push(map.get(keys.get(i + 1)));
						} else {
							ctx.push(NIL);
							return 1;
						}
						return 2;
					}

				}
			}
		}
		ctx.push(NIL);
		return 1;
	}

	public int ipairs(ExecutionContext ctx) {
		int i = (int) ctx.get(1).asNumber();
		TableValue tv = (TableValue) ctx.get(0);
		Table table = tv.getTable();
		i++;
		ctx.push(new NumberValue(i));
		Value v = table.arrayGet(i - 1);
		ctx.push(v);
		return v.isNil() ? 0 : 2;
	}

	public int lua_ipairs(ExecutionContext ctx) {
		ctx.push(new ClosureValue(new Closure(this::ipairs, ctx.getEnv())));
		ctx.push(ctx.get(0));
		ctx.push(new NumberValue(0));
		return 3;
	}

	public int lua_tonumber(ExecutionContext ctx) {
		int base = ctx.optionalInt(1, 10);
		if(base == 10) {
			ctx.push(new NumberValue(ctx.get(0).asNumber()));
		} else {
			if(base >= 36 || base <= 2) ctx.throwError("base out of range");
			String s = ctx.get(0).asString();
			try {
				ctx.push(new NumberValue(Long.parseLong(s, base)));
			} catch (NumberFormatException e) {
				ctx.push(NIL);
			}
		}
		return 1;
	}


}
