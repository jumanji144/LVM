package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ReturnInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NilValue;

public class ReturnExecutor implements Executor<ReturnInstruction> {
	@Override
	public void execute(ReturnInstruction inst, ExecutionContext ctx) {

		int returns = inst.getNumReturns();
		int register = ctx.getBase() + inst.getRegister();
		if(returns != 0) ctx.setTop(register + returns - 1);

		ctx.getHelper().endCtx(ctx, register);

		ctx.setReturning(true);
	}
}
