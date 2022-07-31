package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static me.darknet.lua.vm.value.NilValue.NIL;

public class BaseLibrary extends Library {

	private static final String VERSION = "Lua 5.1";

	public BaseLibrary() {
		super("base", "");
		set("_VERSION", new StringValue(VERSION));
	}

	public void lua_assert(ExecutionContext ctx) {
		if(!ctx.get(0).asBoolean()) throw new AssertionError("Assertion failed");
	}

	public void lua_collectgarbage(ExecutionContext ctx) {
		// TODO
	}

	public void lua_dofile(ExecutionContext ctx) {
		String argument = ctx.has(0) ? ctx.get(0).asString() : null;
		// TODO
	}

	public void lua_gcinfo(ExecutionContext ctx) {
		ctx.setReturnValues(new NumberValue(0));
	}

	public void lua_getfenv(ExecutionContext ctx) {
		Table env = ctx.getCaller().getEnv();
		TableValue val;
		if(env == null) {
			val = new TableValue(ctx.getVM().getGlobal());
		} else {
			val = new TableValue(env);
		}
		ctx.setReturnValues(val);
	}

	public void lua_getmetatable(ExecutionContext ctx) {
		// If object does not have a metatable, returns nil.
		// Otherwise, if the object's metatable has a __metatable field, returns the associated value.
		// Otherwise, returns the metatable of the given object.
		Value obj = ctx.get(0);
		Value returnValue;
		if(obj instanceof TableValue tb) {
			Table meta = tb.getTable().getMetatable();
			if(meta.has("__metatable")) {
				returnValue = meta.get("__metatable");
			} else {
				returnValue = new TableValue(meta);
			}
		} else {
			returnValue = NIL;
		}
		ctx.setReturnValues(returnValue);
	}

	public void lua_loadfile(ExecutionContext ctx) {
		// TODO
	}

	public void lua_load(ExecutionContext ctx) {
		// TODO
	}

	public void lua_loadstring(ExecutionContext ctx) {
		// TODO
	}

	public void lua_next(ExecutionContext ctx) {
		TableValue table = (TableValue) ctx.get(0);
		Map<String, Value> map = table.getTable().getTable();
		Value key = ctx.getOrNil(1);
		if(key.isNil()) { // return first pair in table
			for(String k : map.keySet()) {
				ctx.ret(new StringValue(k), map.get(k));
				return;
			}
			ctx.ret(NIL, NIL);
		} else { // else return the key that comes after the given key
			// if the key doesn't exist at all return nil
			if(!map.containsKey(key.asString())) {
				ctx.ret(NIL, NIL);
				return;
			}
			// get the next key
			String nextKey = null;
			for (String s : map.keySet()) {
				if(s.equals(key.asString())) {
					nextKey = s;
					break;
				}
			}
			if(nextKey == null) {
				ctx.ret(NIL, NIL);
				return;
			}

			// get the next value
			Value nextValue = map.get(nextKey);
			ctx.ret(new StringValue(nextKey), nextValue);
		}
	}
	public void lua_pcall(ExecutionContext ctx) {
		ClosureValue closure = (ClosureValue) ctx.get(0);
		Closure cl = closure.getClosure();

		Value[] arguments = new Value[ctx.getRegisters().length - 1];
		System.arraycopy(ctx.getRegisters(), 1, arguments, 0, arguments.length);

		ExecutionContext newCtx = ctx.getVM().getHelper().prepareCtx(cl, arguments.length, arguments);

		AtomicReference<Value> ret = new AtomicReference<>(NIL);
		Closure perrorHandler = new Closure((c) -> ret.set(c.get(0)), null);

		newCtx.setCatchFunction(perrorHandler);
		newCtx.setCaller(ctx); // we are the caller of the closure

		// call the closure
		ctx.getVM().getHelper().invoke(newCtx);

		Value errorVal = ret.get();
		int returns = !errorVal.isNil() ? 2 : 1 + newCtx.getReturnValues().length;
		Value[] returnArray = new Value[returns];
		Value[] returnValues = newCtx.getReturnValues();
		BooleanValue status = new BooleanValue(errorVal.isNil());
		returnArray[0] = status;
		if(errorVal.isNil()) {
			// copy return values to return array
			System.arraycopy(returnValues, 0, returnArray, 1, returnValues.length);
		} else {
			returnArray[1] = errorVal;
		}

		ctx.ret(returnArray);
	}

	public void lua_xpcall(ExecutionContext ctx) {
		ClosureValue target = (ClosureValue) ctx.get(0);
		ClosureValue errorHandler = (ClosureValue) ctx.get(1);

		Closure cl = target.getClosure();
		Closure eh = errorHandler.getClosure();

		AtomicReference<Value> ret = new AtomicReference<>(NIL);
		Closure perrorHandler = new Closure((c) -> ret.set(c.get(0)), null);

		// no arguments are passed to the closure
		ExecutionContext newCtx = ctx.getVM().getHelper().prepareCtx(cl, 0);

		newCtx.setCatchFunction(perrorHandler);
		newCtx.setCaller(ctx); // we are the caller of the closure

		// call the closure
		ctx.getVM().getHelper().invoke(newCtx);

		Value errorVal = ret.get();
		if(!errorVal.isNil())
			// we can just call invoke, because we have a guarantee that the error handler will be a 1 argument function
			ctx.getVM().getHelper().invoke(eh, errorVal);

		Value[] returns = new Value[2];
		returns[0] = new BooleanValue(errorVal.isNil());
		returns[1] = errorVal;

		ctx.ret(returns);

	}

	public void lua_print(ExecutionContext ctx) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < ctx.getRegisters().length; i++) {
			sb.append(ctx.get(i).asString());
			if(i < ctx.getRegisters().length - 1) sb.append("\t");
		}
		System.out.println(sb);
	}


}
