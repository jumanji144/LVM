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

	/**
	 * Prepares a ctx for a function call.
	 * @param closure closure to prepare
	 * @param passed how many arguments were passed
	 * @param arguments actual arguments (also includes varargs)
	 * @return prepared ctx
	 */
	public ExecutionContext prepareCtx(Closure closure, int passed, Value... arguments) {
		ExecutionContext ctx2;

		// prepare context
		if(closure.isLuaFunction()) {

			LuaFunction function = closure.getLuaFunction();

			ctx2 = new ExecutionContext(vm, function.getMaxStackSize());

			for (int i = 0; i < function.getNumParams(); i++) {
				if(i >= arguments.length) ctx2.set(i, NilValue.NIL);
				else ctx2.set(i, arguments[i]);
			}

			if(function.isVararg()) {
				int offset = function.getNumParams() + 1;
				int numArgs = arguments.length - offset;
				Value[] args = new Value[numArgs];
				System.arraycopy(arguments, offset, args, 0, numArgs);
				ctx2.setVarargs(args);
			}

			// set up function
			ctx2.setCurrentFunction(function);
			ctx2.setCurrentClosure(closure);


		} else {

			int args = passed - 1;
			if(args== -1) { // arguments are varargs
				// cant know them so just make them all the register we have left
				args = arguments.length;
			}

			// create state for function
			ctx2 = new ExecutionContext(vm, args);

			// copy args to registers
			for (int i = 0; i < args; i++) {
				ctx2.set(i, arguments[i]);
			}

			// execute function
			ctx2.setPc(-1);
			ctx2.setCurrentClosure(closure);

		}
		return ctx2;
	}

}
