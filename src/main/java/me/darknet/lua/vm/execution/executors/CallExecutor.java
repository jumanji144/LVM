package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.CallInstruction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.ClosureValue;
import me.darknet.lua.vm.value.NilValue;
import me.darknet.lua.vm.value.Value;

public class CallExecutor implements Executor<CallInstruction> {

	@Override
	public void execute(CallInstruction instruction, ExecutionContext ctx) {

		ClosureValue closureValue = (ClosureValue) ctx.get(instruction.getRegister());
		Closure closure = closureValue.getClosure();

		ExecutionContext ctx2;

		// prepare context
		if(closure.isLuaFunction()) {

			LuaFunction function = closure.getLuaFunction();

			ctx2 = new ExecutionContext(ctx.getVM(), function.getMaxStackSize());

			for (int i = 0; i < function.getNumParams(); i++) {
				int register = instruction.getRegister() + i + 1;
				if(register >= ctx.getRegisters().length) ctx2.set(i, NilValue.NIL);
				else ctx2.set(i, ctx.get(register));
			}

			if(function.isVararg()) {
				int register = instruction.getRegister() + function.getNumParams() + 1;
				int numArgs = ctx.getRegisters().length - register;
				Value[] args = new Value[numArgs];
				for(int i = 0; i < numArgs; i++) {
					args[i] = ctx.get(register + i);
				}
				ctx2.setVarargs(args);
			}

			// set up function
			ctx2.setCurrentFunction(function);
			ctx2.setCurrentClosure(closure);


		} else {

			int arguments = instruction.getNumArgs() - 1;
			if(arguments == -1) { // arguments are varargs
				// cant know them so just make them all the register we have left
				arguments = ctx.getRegisters().length - instruction.getRegister() - 1;
			}

			// create state for function
			ctx2 = new ExecutionContext(ctx.getVM(), arguments);

			// copy args to registers
			for (int i = 0; i < arguments; i++) {
				ctx2.set(i, ctx.get(instruction.getRegister() + i + 1));
			}

			// execute function
			ctx2.setPc(-1);
			ctx2.setCurrentClosure(closure);

		}

		ctx.getVM().getHelper().invoke(ctx2);

		Value[] returnValues = ctx2.getReturnValues();
		int returnAmount = instruction.getNumReturns() - 1;
		if(returnAmount == -1) { // return amount is varargs
			returnAmount = returnValues.length;
		}

		for (int i = 0; i < returnAmount; i++) {
			int register = instruction.getRegister() + i;
			if(i > returnValues.length - 1) ctx.set(register, NilValue.NIL);
			else ctx.set(register, returnValues[i]);
		}



	}
}
