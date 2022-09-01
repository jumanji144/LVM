package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.file.function.LuaFunction;
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
		Table coroutine = new Table();
		coroutine.set("create", newClosure(this::co_create));
		coroutine.set("status", newClosure(this::co_status));
		coroutine.set("resume", newClosure(this::co_resume));
		coroutine.set("yield", newClosure(this::co_yield));
		coroutine.set("wrap", newClosure(this::co_wrap));
		set("coroutine", new TableValue(coroutine));
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
		int level = ctx.optionalInt(1, 1);
		String msg = ctx.optionalString(0, "an error occurred");
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

	public int lua_setfenv(ExecutionContext ctx) {
		TableValue tableValue = ctx.checkType(1, Type.TABLE);
		ctx.getParent().setEnv(tableValue.getTable());
		return 1;
	}

	public int lua_pcall(ExecutionContext ctx) {
		ExecutionContext newContext = ctx.getHelper().prepareCtx(ctx, ctx.reg(0), -1);

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
		ClosureValue errorHandler = (ClosureValue) ctx.get(1);
		Closure eh = errorHandler.getClosure();

		int top = ctx.getTop(); // remember top

		ExecutionContext newContext = ctx.getHelper().prepareCtx(ctx, ctx.reg(0), -1);
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

	public int lua_unpack(ExecutionContext ctx) {
		TableValue val = ctx.checkType(0, Type.TABLE);
		Table table = val.getTable();
		int start = ctx.optionalInt(1, 1);
		int end;
		if(ctx.has(2)) end = (int) ctx.checkType(2, Type.NUMBER).asNumber();
		else end = table.getArray().size();
		if (start > end) return 0;  /* empty range */
		int n = end - start + 1;  /* number of elements */
		if (n <= 0)  /* n <= 0 means arith. overflow */
			ctx.throwError("too many results to unpack");
		ctx.push(table.get(n)); /* push arg[i] (avoiding overflow problems) */
		while (start++ < end)  /* push arg[i + 1...e] */
			ctx.push(table.get(start));
		return n;
	}

	public int lua_select(ExecutionContext ctx) {
		int n = ctx.size();
		Value v = ctx.get(0);
		if(v instanceof StringValue sv && sv.getValue().charAt(0) == '#') {
			ctx.push(new NumberValue(n - 1));
			return 1;
		} else {
			int i = (int) ctx.checkType(0, Type.NUMBER).asNumber();
			if (i < 0) i = n + i;
			else if (i > n) i = n;
			if(1 <= i) ctx.throwError("bad argument #1 to 'select'");
			return n - i;
		}
	}

	public int lua_tostring(ExecutionContext ctx) {
		Value v = ctx.get(0);
		if(ctx.getHelper().attemptMetamethod(ctx, v, NIL, ctx.reg(0), "__tostring")) return 1;
		if(v instanceof StringValue) {
			ctx.push(v);
		} else {
			ctx.push(new StringValue(v.toString()));
		}
		return 1;
	}

	public int lua_type(ExecutionContext ctx) {
		Value v = ctx.get(0);
		ctx.push(new StringValue(v.getType().getName()));
		return 1;
	}

	public int lua_rawget(ExecutionContext ctx) {
		TableValue val = ctx.checkType(0, Type.TABLE);
		Table table = val.getTable();
		ctx.push(table.get(ctx.get(1).asString()));
		return 1;
	}

	public int lua_rawset(ExecutionContext ctx) {
		TableValue val = ctx.checkType(0, Type.TABLE);
		Table table = val.getTable();
		table.set(ctx.get(1).asString(), ctx.get(2));
		return 0;
	}

	public int lua_rawequal(ExecutionContext ctx) {
		Value v1 = ctx.get(0);
		Value v2 = ctx.get(1);
		ctx.push(BooleanValue.valueOf(ctx.getHelper().rawEquals(ctx, v1, v2)));
		return 1;
	}

	// COROUTINES
	public static final int CO_STATUS_RUNNING = 0;
	public static final int CO_STATUS_SUSPENDED = 1;
	public static final int CO_STATUS_NORMAL = 2;
	public static final int CO_STATUS_DEAD = 3;

	public static final int CO_YIELD = 1;
	public static final int CO_RUNNING = 0;

	public static String[] CO_STATUS_NAMES = { "running", "suspended", "normal", "dead" };

	public int co_create(ExecutionContext ctx) {
		// create a new coroutine
		ClosureValue clv = ctx.checkType(0, Type.FUNCTION);
		Closure cl = clv.getClosure();
		LuaFunction fn = cl.getLuaFunction();
		ExecutionContext co = new ExecutionContext(new Value[fn.getMaxStackSize()]);
		co.setEnv(ctx.getEnv()); // prepare env
		co.setClosure(cl);
		co.setFunction(fn);
		co.setVm(ctx.getVm());
		co.set(0, clv);
		co.setParent(ctx);
		co.setStatus(CO_STATUS_SUSPENDED);
		ctx.push(new ThreadValue(co));
		return 1;
	}

	public int aux_status(ExecutionContext ctx, ExecutionContext co) {
		if(co == ctx) return CO_STATUS_RUNNING; // we are the thread
		switch (co.getStatus()) {
			case CO_YIELD: return CO_STATUS_SUSPENDED;
			case CO_RUNNING: {
				if(co.size() == 0) return CO_STATUS_DEAD; // thread has died
				else return CO_STATUS_SUSPENDED; // initial state
			}
			default: return CO_STATUS_DEAD;
		}
	}

	public int co_status(ExecutionContext ctx) {
		ThreadValue co = ctx.checkType(0, Type.THREAD);
		ctx.push(new StringValue(CO_STATUS_NAMES[aux_status(ctx, co.getContext())]));
		return 1;
	}

	public int aux_resume(ExecutionContext ctx, ExecutionContext co, int numArgs) {
		int status = aux_status(ctx, co);
		co.checkStack(numArgs); // ensure co can hold arguments
		if(status != CO_STATUS_SUSPENDED) {
			ctx.push(new StringValue("cannot resume " + CO_STATUS_NAMES[status] + " coroutine"));
			return -1;
		}
		// move args
		for(int i = 0; i < numArgs; i++) {
			co.push(ctx.get(i + 1));
		}
		int res = ctx.getHelper().resume(co, ctx.getTop() - numArgs);
		if(res == 1) {
			int nres = co.getNumResults();
			ctx.checkStack(nres + 1);
			// move all results to the caller
			for(int i = 0; i < nres; i++) {
				ctx.push(co.get(i));
			}
			return nres;
		} else {
			ctx.push(co.get(0));
			return -1;
		}

	}

	public int co_resume(ExecutionContext ctx) {
		ThreadValue co = ctx.checkType(0, Type.THREAD);
		int numArgs = ctx.size() - 1;
		int res = aux_resume(ctx, co.getContext(), numArgs);
		if (res < 0) {
			ctx.push(BooleanValue.FALSE);
			ctx.insert(ctx.reg(0), ctx.reg(1));
			return 2;
		} else {
			ctx.push(BooleanValue.TRUE);
			ctx.insert(ctx.reg(0), res + 1);
			return res + 1;
		}
	}

	public int co_yield(ExecutionContext ctx) {
		ctx.setStatus(CO_YIELD);
		return -1;
	}

	public int aux_wrap(ExecutionContext ctx) {
		ThreadValue value = (ThreadValue) ctx.getClosure().getUpvalue(0);
		int r = aux_resume(ctx, value.getContext(), ctx.size());
		if(r < 0) {
			ctx.throwError(ctx.get(0).asString());
		}
		return r;
	}

	public int co_wrap(ExecutionContext ctx) {
		co_create(ctx); // create coroutine
		ClosureValue clv = newClosure(this::aux_wrap);
		clv.getClosure().setUpvalue(0, ctx.get(1)); // give it the coroutine
		ctx.push(clv);
		return 1;
	}

	public int co_running(ExecutionContext ctx) {
		ctx.push(new ThreadValue(ctx));
		return 1;
	}

}
