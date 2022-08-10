package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.*;

public class VMHelper {

	Interpreter interpreter;
	VM vm;

	public VMHelper(Interpreter interpreter, VM vm) {
		this.interpreter = interpreter;
		this.vm = vm;
	}

	public ExecutionContext invoke(LuaFunction function, Table env) {
		ExecutionContext ctx = new ExecutionContext(new Value[function.getMaxStackSize()]);
		ctx.setEnv(env);
		ctx.setVm(vm);
		ctx.setFunction(function);
		ctx.setClosure(new Closure(function, env));
		interpreter.execute(ctx, function);
		return ctx;
	}

	public void invoke(ExecutionContext ctx) {
		Closure cl = ctx.getClosure();
		if (cl.isLuaFunction()) {
			LuaFunction function = cl.getLuaFunction();
			interpreter.execute(ctx, function);
		} else {
			int results = cl.getJavaFunction().apply(ctx);
			endCtx(ctx, ctx.getTop() - results);
		}
	}

	public void invoke(ExecutionContext ctx, int func, int numResults) {
		ClosureValue closure = (ClosureValue) ctx.getRaw(func);
		Closure cl = closure.getClosure();

		ExecutionContext newCtx = prepareCtx(ctx, cl, func, numResults);
		invoke(newCtx);
		finish(ctx, newCtx);
	}

	public int invoke(ExecutionContext ctx, Closure cl, int numResults, Value... args) {
		// emulate the stack layout of a function call
		int register = ctx.getTop();
		ctx.push(new ClosureValue(cl));
		for (Value arg : args) {
			ctx.push(arg);
		}
		// top should already be adjusted
		invoke(ctx, register, numResults);
		return register; // return the return register
	}

	public void callMetamethod(ExecutionContext ctx, int res, Value function, Value arg1, Value arg2) {

		ctx.checkStack(3); // make room for 3 args

		ctx.push(function); // push the function to be called
		ctx.push(arg1); // first arg
		ctx.push(arg2); // second arg

		invoke(ctx, ctx.getTop() - 3, 1); // call the function

		ctx.setTop(ctx.getTop() - 1); // remove the function
		ctx.setRaw(res, ctx.getRaw(ctx.getTop())); // set the result

	}

	public void callMetamethod(ExecutionContext ctx, Value function, Value arg1, Value arg2, Value arg3) {

		ctx.checkStack(4); // make room for 4 args

		ctx.push(function); // push the function to be called
		ctx.push(arg1); // first arg
		ctx.push(arg2); // second arg
		ctx.push(arg3); // third arg

		invoke(ctx, ctx.getTop() - 4, 0); // call the function
	}

	public boolean attemptMetamethod(ExecutionContext ctx, Value obj1, Value obj2, int res, String metamethod) {
		Value meta = attemptFindMetaobject(obj1, metamethod);
		if (meta.isNil()) meta = attemptFindMetaobject(obj2, metamethod);
		if (meta.isNil()) return false;
		callMetamethod(ctx, res, meta, obj1, obj2);
		return true;
	}

	public Value attemptFindMetaobject(Value v, String method) {
		Table meta = getMetatable(v);
		return meta != null ? meta.get(method) : NilValue.NIL;
	}

	public Table getMetatable(Value value) {
		switch (value.getType()) {
			case TABLE -> {
				TableValue t = (TableValue) value;
				return t.getTable().getMetatable();
			}
			case USERDATA -> {
				UserDataValue u = (UserDataValue) value;
				return u.getValue().getMetatable();
			}
			default -> {
				Table global = vm.getGlobal();
				Value res = global.get(value.getType().getName()); // get metatable
				if (res.isNil() || res.getType() != Type.TABLE) return null;
				return ((TableValue) res).getTable();
			}
		}
	}

	public void setMetatable(ExecutionContext ctx, Value table, Value meta) {
		// TODO: cleanup
		Table mt = null;
		if (meta.isNil()) {
		} else if (meta.getType() == Type.TABLE) {
			mt = ((TableValue) meta).getTable();
		} else {
			ctx.throwError("metatable must be a table or nil");
		}
		switch (table.getType()) {
			case TABLE -> ((TableValue) table).getTable().setMetatable(mt);
			case USERDATA -> ((UserDataValue) table).getValue().setMetatable(mt);
			default -> ctx.getVm().getGlobal().set(table.getType().getName(), meta);
		}
	}

	private int adjustVarargs(ExecutionContext ctx, LuaFunction function, int actual) {
		int numFixed = function.getNumParams();
		int base, fixed;

		for (; actual < numFixed; actual++) {
			ctx.push(NilValue.NIL);
		}

		fixed = ctx.getTop() - actual;
		base = ctx.getTop();
		for (int i = 0; i < numFixed; i++) {
			ctx.push(ctx.getRaw(fixed + i));
			ctx.setRaw(fixed + i, NilValue.NIL);
		}

		return base;
	}

	public ExecutionContext prepareCtx(ExecutionContext parent, Closure cl, int func, int numResults) {
		ExecutionContext newCtx;
		if (cl.isLuaFunction()) { // is lua function
			LuaFunction function = cl.getLuaFunction();
			int base;
			int top = parent.getTop();
			if (!function.isVararg()) {
				base = func + 1; // base will be first argument
				if (top > base + function.getNumParams()) // if top is not already correct
					top = base + function.getNumParams(); // top is end of arguments
			} else {
				int args = (parent.getTop() - func) - 1; // number of ACTUAL arguments
				base = adjustVarargs(parent, function, args); // adjust for varargs
			}
			newCtx = new ExecutionContext(parent, top, base); // create new context
			newCtx.setFunction(function); // set function
			newCtx.setFunctionReturn(func); // set which register to write back to
			// create and clear old stack
			newCtx.ensureSize(top + function.getMaxStackSize()); // ensure stack is large enough to fit new stack
			top = base + function.getMaxStackSize(); // top is end of stack
			for (int st = parent.getTop(); st < top; st++) {
				newCtx.setRaw(st, NilValue.NIL); // file with nil to mark empty slots
			}
			// update correct top pointer
			newCtx.setTop(top); // re-set the stack top
		} else {
			int base = func + 1; // base is simply start of arguments
			int top = parent.getTop(); // top is top, so end of arguments
			newCtx = new ExecutionContext(parent, top, base); // create new context
			newCtx.setFunctionReturn(func); // set which register to write back to
			newCtx.ensureSize(top + 20); // +20 here because we don't know if libraries may add to stack
		}
		newCtx.setClosure(cl);
		newCtx.setNumResults(numResults);
		newCtx.setVm(vm);
		newCtx.setEnv(parent.getEnv());
		return newCtx;
	}

	public void endCtx(ExecutionContext ctx, int start) {

		int res = ctx.getFunctionReturn();
		int wanted = ctx.getNumResults();

		int i;
		for (i = wanted; i != 0 && start < ctx.getTop(); i--)
			ctx.setRaw(res++, ctx.getRaw(start++));
		while (i-- > 0)
			ctx.setRaw(res++, NilValue.NIL);

		ctx.setTop(res);
	}

	public void finish(ExecutionContext ctx, ExecutionContext oldCtx) {
		ctx.setStack(oldCtx.getStack());
	}

	public void getTable(ExecutionContext ctx, Value value, Value indexValue, int register) {
		for (int i = 0; i < 30; i++) {
			Value tm = NilValue.NIL;
			if (value.getType() == Type.TABLE) {
				Table table = ((TableValue) value).getTable();
				Value res = tableGet(table, indexValue);
				if (!res.isNil() || !table.hasMetaobject("__index")) {
					// set the register to the result
					ctx.setRaw(register, res); // set raw because register is already offset
					return;
				}
			} else if ((tm = attemptFindMetaobject(value, "__index")).isNil()) {
				ctx.throwError("attempt to index a " + value.getType().getName() + " value");
			}
			if (tm.getType() == Type.FUNCTION) {
				ctx.getHelper().callMetamethod(ctx, register, tm, value, indexValue); // call it
				return;
			}
			value = tm; // attempt to get the value again
		}
	}

	public Value tableGet(Table table, Value key) {
		return switch (key.getType()) {
			case STRING -> table.get(key.asString());
			case NUMBER -> table.get((int) key.asNumber() - 1); // -1 because Lua arrays are 1-based
			default -> NilValue.NIL;
		};
	}

	public void setTable(ExecutionContext ctx, Value value, Value indexValue, Value newValue) {
		for (int i = 0; i < 30; i++) {
			Value tm = NilValue.NIL;
			if (value.getType() == Type.TABLE) {
				Table table = ((TableValue) value).getTable();
				Value oldValue = tableGet(table, indexValue);
				if (!oldValue.isNil() || !table.hasMetaobject("__index")) {
					tableSet(ctx, table, indexValue, newValue);
					return;
				}
			} else if ((tm = attemptFindMetaobject(value, "__newindex")).isNil()) {
				ctx.throwError("attempt to index a " + tm.getType().getName() + " value");
			}
			if (tm.getType() == Type.FUNCTION) {
				ctx.getHelper().callMetamethod(ctx, tm, value, indexValue, newValue); // call it
				return;
			}
			value = tm; // attempt to get the value again
		}
	}

	public void tableSet(ExecutionContext ctx, Table table, Value key, Value newValue) {
		switch (key.getType()) {
			case STRING -> table.set(key.asString(), newValue);
			case NUMBER -> table.set((int) key.asNumber() - 1, newValue); // -1 because Lua arrays are 1-based
			default -> ctx.throwError("attempt to index a " + key.getType().getName() + " value");
		}
	}

	public boolean isFalse(Value value) {
		return value.isNil() || (value instanceof BooleanValue && !((BooleanValue) value).isValue());
	}

	public boolean lessThen(ExecutionContext ctx, Value a, Value b) {
		// assume that both types are the same
		return switch (a.getType()) {
			case NUMBER -> a.asNumber() < b.asNumber();
			case STRING -> a.asString().compareTo(b.asString()) < 0;
			default -> {
				// attempt to call __lt
				Value tm = attemptFindMetaobject(a, "__lt");
				Value tm2 = attemptFindMetaobject(b, "__lt");
				if (tm.isNil()) {
					ctx.throwError("attempt to compare a " + a.getType().getName() + " value");
					yield false;
				}
				if (tm != tm2) {
					ctx.throwError("attempt to compare a " + a.getType().getName() + " value");
					yield false;
				}
				ctx.getHelper().callMetamethod(ctx, ctx.getTop(), tm, a, b); // call it
				boolean res = !isFalse(ctx.get(ctx.getTop())); // return the result
				yield res;

			}
		};
	}

	public boolean lessEqual(ExecutionContext ctx, Value a, Value b) {
		// assume that both types are the same
		return switch (a.getType()) {
			case NUMBER -> a.asNumber() <= b.asNumber();
			case STRING -> a.asString().compareTo(b.asString()) <= 0;
			default -> {
				// attempt to call __le
				Value tm = attemptFindMetaobject(a, "__le");
				Value tm2 = attemptFindMetaobject(b, "__le");
				if (tm.isNil()) {
					ctx.throwError("attempt to compare a " + a.getType().getName() + " value");
					yield false;
				}
				if (tm != tm2) {
					ctx.throwError("attempt to compare a " + a.getType().getName() + " value");
					yield false;
				}
				callMetamethod(ctx, ctx.getTop(), tm, a, b); // call it
				boolean res = !isFalse(ctx.get(ctx.getTop()));
				yield res; // return the result
			}
		};
	}

	public boolean equals(ExecutionContext ctx, Value a, Value b) {
		Value tm;
		switch (a.getType()) {
			case NIL:
				return true; // nil is equal to nil
			case NUMBER:
				return a.asNumber() == b.asNumber();
			case BOOLEAN:
				return a.asBoolean() == b.asBoolean();
			case STRING:
				return a.asString().equals(b.asString());
			case USERDATA:
			case TABLE: {
				if (a == b) return true;
				tm = attemptFindMetaobject(a, "__eq");
				break;
			}
			default:
				return a == b;
		}
		if (tm.isNil()) return false;
		callMetamethod(ctx, ctx.getTop(), tm, a, b); // call it
		return !isFalse(ctx.get(ctx.getTop())); // return the result
	}
}
