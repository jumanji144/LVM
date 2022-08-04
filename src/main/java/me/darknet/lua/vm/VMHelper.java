package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.NilValue;
import me.darknet.lua.vm.value.Value;

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
			endCtx(ctx, results);
		}
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
			newCtx.ensureSize(top + 20); // +20 here because we don't know if libraries may add to stack
		}
		newCtx.setClosure(cl);
		newCtx.setNumResults(numResults);
		newCtx.setVm(vm);
		return newCtx;
	}

	public void endCtx(ExecutionContext ctx, int start) {
		int register = ctx.getBase() + start;

		int res = ctx.getFunctionReturn();
		int wanted = ctx.getNumResults();

		int i;
		for(i = wanted; i != 0 && register < ctx.getTop(); i--)
			ctx.setRaw(res++, ctx.getRaw(register++));
		while(i-- > 0)
			ctx.setRaw(res++, NilValue.NIL);

		ctx.setTop(res);
	}

	public void callMetaMethod(Value value, String name, Value... args) {
	}


}
