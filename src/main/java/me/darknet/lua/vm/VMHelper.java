package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.data.UserData;
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
		interpreter.execute(ctx, function);
		return ctx;
	}

	public void invoke(ExecutionContext ctx) {
		Closure cl = ctx.getClosure();
		if(cl.isLuaFunction()) {
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
		for(Value arg : args) {
			ctx.push(arg);
		}
		// top should already be adjusted
		invoke(ctx, register, numResults);
		return register; // return the return register
	}

	public void callMetamethod(ExecutionContext ctx, int res, Value function, Value arg1, Value arg2) {

		ctx.push(function); // push the function to be called
		ctx.push(arg1); // first arg
		ctx.push(arg2); // second arg

		invoke(ctx, ctx.getTop() - 3, 1); // call the function

		ctx.setTop(ctx.getTop() - 1); // pop the function
		ctx.setRaw(res, ctx.getRaw(ctx.getTop())); // set the result
	}

	public boolean attemptMetamethod(ExecutionContext ctx, Value obj1, Value obj2, int res, String metamethod) {
		Value meta = attemptFindMetaobject(obj1, metamethod);
		if(meta.isNil()) meta = attemptFindMetaobject(obj2, metamethod);
		if(meta.isNil()) return false;
		callMetamethod(ctx, res, meta, obj1, obj2);
		return true;
	}

	public Value attemptFindMetaobject(Value v, String method) {
		Table meta = switch (v.getType()) {
			case TABLE -> {
				TableValue t = (TableValue) v;
				yield t.getTable().getMetatable();
			}
			case USERDATA -> {
				UserDataValue u = (UserDataValue) v;
				yield u.getValue().getMetatable();
			}
			default -> {
				Table global = vm.getGlobal();
				Value res = global.get(v.getType().getName()); // get metatable
				if(res.isNil() || res.getType() != Type.TABLE) yield null;
				yield ((TableValue) res).getTable();
 			}
		};
		return meta != null ? meta.get(method) : NilValue.NIL;
	}

	private int adjustVarargs(ExecutionContext ctx, LuaFunction function, int actual) {
		int numFixed = function.getNumParams();
		int base, fixed;

		for(; actual < numFixed; actual++) {
			ctx.push(NilValue.NIL);
		}

		fixed = ctx.getTop() - actual;
		base = ctx.getTop();
		for(int i = 0; i < numFixed; i++) {
			ctx.push(ctx.getRaw(fixed + i));
			ctx.setRaw(fixed + i, NilValue.NIL);
		}

		return base;
	}

	public ExecutionContext prepareCtx(ExecutionContext parent, Closure cl, int func, int numResults) {
		ExecutionContext newCtx;
		if(cl.isLuaFunction()) { // is lua function
			LuaFunction function = cl.getLuaFunction();
			int base;
			int top = parent.getTop();
			if(!function.isVararg()) {
				base = func + 1; // base will be first argument
				if(top > base + function.getNumParams()) // if top is not already correct
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
			for(int st = parent.getTop(); st < top; st++) {
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
		for(i = wanted; i != 0 && start < ctx.getTop(); i--)
			ctx.setRaw(res++, ctx.getRaw(start++));
		while(i-- > 0)
			ctx.setRaw(res++, NilValue.NIL);

		ctx.setTop(res);
	}

	public void finish(ExecutionContext ctx, ExecutionContext oldCtx) {
		ctx.setStack(oldCtx.getStack());
		ctx.setTop(oldCtx.getTop());
	}

	public void getTable(ExecutionContext ctx, Value value, Value indexValue, int register) {
		for(int i = 0; i < 30; i++) {
			if (value.getType() == Type.TABLE) {
				Table table = ((TableValue) value).getTable();
				Value res = tableGet(table, indexValue);
				if (table.hasMetatable() && table.getMetatable().has("__index")) { // does table have index meta function
					Value obj = table.getMetatable().get("__index");
					ctx.getHelper().callMetamethod(ctx, register, obj, value, indexValue); // call it
				} else { // or else instead
					// set the register to the result
					ctx.setRaw(register, res); // set raw because register is already offset
				}
				return;
			} else { // get the meta object
				Value obj = attemptFindMetaobject(value, "__index");
				if (obj.isNil()) {
					ctx.throwError("attempt to index a " + value.getType().getName() + " value");
				}
				if (obj.getType() == Type.FUNCTION) {
					ctx.getHelper().callMetamethod(ctx, register, obj, value, indexValue); // call it
					return;
				}
				value = obj; // attempt to get the value again
			}
		}
	}

	public Value tableGet(Table table, Value key) {
		return switch (key.getType()) {
			case STRING -> table.get(key.asString());
			case NUMBER -> table.getArray().get((int) key.asNumber() - 1); // -1 because Lua arrays are 1-based
			default -> NilValue.NIL;
		};
	}

	public void callMetaMethod(Value value, String name, Value... args) {
	}

}
