package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.Value;

public class VMHelper {

	Interpreter interpreter;
	VM vm;

	public VMHelper(Interpreter interpreter, VM vm) {
		this.interpreter = interpreter;
		this.vm = vm;
	}

	public ExecutionContext invoke(LuaFunction function, Table env) {
		return invoke(new Closure(function, env));
	}

	public ExecutionContext invoke(Closure closure, Value... args) {
		if(closure.isLuaFunction()) {
			LuaFunction function = closure.getLuaFunction();
			// create state for function
			ExecutionContext ctx = new ExecutionContext(vm, function.getMaxStackSize());
			// copy args to registers
			for (int i = 0; i < args.length; i++) {
				ctx.set(i, args[i]);
			}

			// set up function
			ctx.setCurrentFunction(function);
			ctx.setPc(0);
			ctx.setCurrentClosure(closure);

			interpreter.execute(ctx, function);
			return ctx;
		} else {
			ExecutionContext ctx = new ExecutionContext(vm, args.length);
			for (int i = 0; i < args.length; i++) {
				ctx.set(i, args[i]);
			}
			ctx.setPc(-1);
			ctx.setCurrentClosure(closure);

			closure.getJavaFunction().accept(ctx);

			return ctx;
		}
	}

	public void invoke(ExecutionContext ctx) {
		Closure closure = ctx.getCurrentClosure();
		if(closure.isLuaFunction()) {
			interpreter.execute(ctx, closure.getLuaFunction());
		} else {
			closure.getJavaFunction().accept(ctx);
		}
	}

}
