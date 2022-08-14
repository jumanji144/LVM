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

	/**
	 * Raw invoke a {@link LuaFunction} with a supplied {@link Table} as environment.
	 * @param function function to invoke
	 * @param env environment to use
	 * @return result of the invocation
	 */
	public ExecutionContext invoke(LuaFunction function, Table env) {
		ExecutionContext ctx = new ExecutionContext(new Value[function.getMaxStackSize()]);
		ctx.setEnv(env);
		ctx.setVm(vm);
		ctx.setFunction(function);
		ctx.setClosure(new Closure(function, env));
		interpreter.execute(ctx, function);
		return ctx;
	}

	/**
	 * Invoke an already prepared {@link ExecutionContext}
	 * @param ctx context to invoke
	 */
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

	/**
	 * Invoke a function using the {@code func} register pointer and {@code numResults} as the number of results to return.
	 * Args need to be prepared before calling this method.
	 * The results will be placed like: R(func + 1), R(func + 2), ..., R(func + numResults).
	 * @param ctx caller context
	 * @param func register pointer to the function to invoke (non offset)
	 * @param numResults number of results to pull from this function
	 */
	public void invoke(ExecutionContext ctx, int func, int numResults) {
		ExecutionContext newCtx = prepareCtx(ctx, func, numResults);
		invoke(newCtx);
		finish(ctx, newCtx);
	}

	/**
	 * More friendly version of {@link #invoke(ExecutionContext, int, int)}. by allowing own supply of {@link Closure}
	 * and {@link Value[]} to use as arguments
	 * @param ctx caller context
	 * @param cl closure to use
	 * @param numResults number of results to pull from this function
	 * @param args arguments to use
	 * @return register where return values are placed
	 */
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

	/**
	 * Will call a method supplied by the {@code function} {@link Value} object
	 * @param ctx caller context
	 * @param res register to place return values in
	 * @param function function to call
	 * @param arg1 first argument to supply
	 * @param arg2 second argument to supply
	 */
	public void callMetamethod(ExecutionContext ctx, int res, Value function, Value arg1, Value arg2) {

		ctx.checkStack(3); // make room for 3 args

		ctx.push(function); // push the function to be called
		ctx.push(arg1); // first arg
		ctx.push(arg2); // second arg

		invoke(ctx, ctx.getTop() - 3, 1); // call the function

		ctx.setRaw(res, ctx.getRaw(ctx.getTop())); // set the result

	}

	/**
	 * Call a metamethod without return value
	 * @param ctx caller context
	 * @param function function to call
	 * @param arg1 first argument to supply
	 * @param arg2 second argument to supply
	 * @param arg3 third argument to supply
	 */
	public void callMetamethod(ExecutionContext ctx, Value function, Value arg1, Value arg2, Value arg3) {

		ctx.checkStack(4); // make room for 4 args

		ctx.push(function); // push the function to be called
		ctx.push(arg1); // first arg
		ctx.push(arg2); // second arg
		ctx.push(arg3); // third arg

		invoke(ctx, ctx.getTop() - 4, 0); // call the function
	}

	/**
	 * Will attempt to find and call a metamethod in the {@code arg1} {@link Value} or {@code arg2} {@link Value}
	 * @param ctx caller context
	 * @param obj1 first object to check for metamethod
	 * @param obj2 second object to check for metamethod
	 * @param res register to place return values in
	 * @param metamethod metamethod to find
	 * @return {@code true} if a metamethod was found and called, {@code false} otherwise
	 */
	public boolean attemptMetamethod(ExecutionContext ctx, Value obj1, Value obj2, int res, String metamethod) {
		Value meta = attemptFindMetaobject(obj1, metamethod);
		if (meta.isNil()) meta = attemptFindMetaobject(obj2, metamethod);
		if (meta.isNil()) return false;
		callMetamethod(ctx, res, meta, obj1, obj2);
		return true;
	}

	/**
	 * Attempt to find a metaobject in the {@code v} {@link Value}
	 * @param value value to check for metaobject
	 * @param obj object to find
	 * @return {@link Value} of the metaobject if found, {@link NilValue#NIL} otherwise
	 */
	public Value attemptFindMetaobject(Value value, String obj) {
		Table meta = getMetatable(value);
		return meta != null ? meta.get(obj) : NilValue.NIL;
	}

	/**
	 * Attempt to find the metatable in the {@code v} {@link Value}
	 * @param value value to check for metatable
	 * @return {@link Table} of the metatable if found, {@code null} otherwise
	 */
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

	/**
	 * Attempts to set the metatable in the {@code table} {@link Value} to the {@code meta} {@link Value}
	 * If value is not a {@link TableValue} or {@link UserDataValue} then it will be set to the global type metatable
	 * Will throw error if the metatable is not a {@link Table}
	 * @param ctx owner context
	 * @param table table to set metatable on
	 * @param meta metatable to set
	 */
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

	/**
	 * Internal function to adjust for varargs on a function call
	 * @param ctx caller context
	 * @param function function to call
	 * @param actual actual number of arguments supplied
	 * @return new base register for the function call
	 */
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

	/**
	 * Prepare a {@link ExecutionContext} for a function call
	 * @param parent parent context
	 * @param func function to call
	 * @param numResults number of results to expect
	 * @return {@link ExecutionContext} for the function call
	 */
	public ExecutionContext prepareCtx(ExecutionContext parent, int func, int numResults) {

		// attempt to resolve the function
		Closure cl;
		Value v = parent.getRaw(func);
		if(v instanceof ClosureValue cv) cl = cv.getClosure();
		else {
			// try metatable
			Value tm = attemptFindMetaobject(v, "__call");
			if(tm instanceof ClosureValue cv) {
				// we need to pretend like ths was our actual call
				// so open a hole in the stack
				for(int i = parent.getTop(); i > func; i--) {
					parent.setRaw(i, parent.getRaw(i-1));
				}
				parent.setTop(parent.getTop()+1);
				parent.setRaw(func, cv);
				cl = cv.getClosure();
			} else {
				parent.throwError("attempt to call a nil object");
				return null;
			}
		}

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

	/**
	 * Finish a function call via {@link ExecutionContext}
	 * @param ctx context to finish
	 * @param start start register to read return values from (inclusive)
	 */
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

	/**
	 * Take over control of the execution of a function (must be called after any {@code invoke} calls that do not finish the context)
	 * @param ctx new context
	 * @param oldCtx old context
	 */
	public void finish(ExecutionContext ctx, ExecutionContext oldCtx) {
		ctx.setStack(oldCtx.getStack());
	}

	/**
	 * Get a value from a table-like value and possibly call the metamethod {@code __index}
	 * Will throw an error if the value is not a table-like value or does not have a {@code __index} metamethod
	 * @param ctx caller context
	 * @param value table-like value
	 * @param indexValue index value
	 * @param register register to write result to
	 */
	public void getTable(ExecutionContext ctx, Value value, Value indexValue, int register) {
		for (int i = 0; i < 30; i++) {
			Value tm;
			if (value.getType() == Type.TABLE) {
				Table table = ((TableValue) value).getTable();
				Value res = tableGet(table, indexValue);
				if (!res.isNil() || (tm = table.getMetaobject("__index")).isNil()) {
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

	/**
	 * Raw getting of a value from a {@link Table} via a {@link Value} index
	 * @param table table to get from
	 * @param key key to get
	 * @return value at key or {@link NilValue#NIL} if not found
	 */
	public Value tableGet(Table table, Value key) {
		return switch (key.getType()) {
			case STRING -> table.get(key.asString());
			case NUMBER -> table.get((int) key.asNumber() - 1); // -1 because Lua arrays are 1-based
			default -> NilValue.NIL;
		};
	}

	/**
	 * Set a value in a table-like value and possibly call the metamethod {@code __newindex}
	 * Will throw an error if the value is not a table-like value or does not have a {@code __newindex} metamethod
	 * @param ctx caller context
	 * @param value table-like value
	 * @param indexValue index value
	 * @param newValue new value
	 */
	public void setTable(ExecutionContext ctx, Value value, Value indexValue, Value newValue) {
		for (int i = 0; i < 30; i++) {
			Value tm;
			if (value.getType() == Type.TABLE) {
				Table table = ((TableValue) value).getTable();
				Value oldValue = tableGet(table, indexValue);
				if (!oldValue.isNil() || (tm = table.getMetaobject("__newindex")).isNil()) {
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

	/**
	 * Raw setting of a value in a {@link Table} via a {@link Value} index
	 * Will throw an error if the keys is not {@link Type#STRING} or {@link Type#NUMBER}
	 * @param ctx caller context
	 * @param table table to set in
	 * @param key key to set
	 * @param newValue new value
	 */
	public void tableSet(ExecutionContext ctx, Table table, Value key, Value newValue) {
		switch (key.getType()) {
			case STRING -> table.set(key.asString(), newValue);
			case NUMBER -> table.set((int) key.asNumber() - 1, newValue); // -1 because Lua arrays are 1-based
			default -> ctx.throwError("attempt to index a " + key.getType().getName() + " value");
		}
	}

	/**
	 * Determine if a value is false
	 * @param value value to check
	 * @return {@code true} if the value is false
	 */
	public boolean isFalse(Value value) {
		return value.isNil() || (value instanceof BooleanValue && !((BooleanValue) value).isValue());
	}

	/**
	 * Less than comparison between two values and possible metamethod call of {@code __lt}
	 * Will throw an error if the values are not comparable
	 * @param ctx caller context
	 * @param a first value
	 * @param b second value
	 * @return {@code true} if a < b
	 */
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

	/**
	 * Greater or equal than comparison between two values and possible metamethod call of {@code __le}
	 * Will throw an error if the values are not comparable
	 * @param ctx caller context
	 * @param a first value
	 * @param b second value
	 * @return {@code true} if a > b
	 */
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

	/**
	 * Raw equal comparison between two values
	 * @param ctx caller context
	 * @param a first value
	 * @param b second value
	 * @return {@code true} if a == b
	 */
	public boolean rawEquals(ExecutionContext ctx, Value a, Value b) {
		return switch (a.getType()) {
			case NIL -> true; // nil is equal to nil
			case NUMBER -> a.asNumber() == b.asNumber();
			case BOOLEAN -> a.asBoolean() == b.asBoolean();
			case STRING -> a.asString().equals(b.asString());
			default -> a == b;
		};
	}

	/**
	 * Equal comparison between two values and possible metamethod call of {@code __eq}
	 * Will throw an error if the values are not comparable
	 * @param ctx caller context
	 * @param a first value
	 * @param b second value
	 * @return {@code true} if a == b
	 */
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
