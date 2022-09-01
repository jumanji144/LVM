package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.CallInstruction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.ClosureValue;

public class CallExecutor implements Executor<CallInstruction> {

	@Override
	public void execute(CallInstruction inst, ExecutionContext ctx) {

		int register = ctx.getBase() + inst.getRegister();
		if (inst.getNumArgs() != 0) ctx.setTop(register + inst.getNumArgs());

		ExecutionContext newCtx = ctx.getHelper().prepareCtx(ctx, register, inst.getNumReturns() - 1);

		int status = ctx.getHelper().invoke(newCtx);
		if(status < 0) {
			// yield
			ctx.setReturning(true);
			ctx.setNumResults(newCtx.size());
			ctx.setPc(ctx.getPc() + 1);
			return;
		}

		ctx.setStack(newCtx.getStack());
		if (inst.getNumReturns() == 0) ctx.setTop(newCtx.getTop());

	}
}
