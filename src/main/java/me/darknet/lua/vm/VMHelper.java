package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.ClosureValue;
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

	public void callMetaMethod(Value value, String name, Value... args) {
	}

}
