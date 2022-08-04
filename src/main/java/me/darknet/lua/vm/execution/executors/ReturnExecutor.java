package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ReturnInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NilValue;

public class ReturnExecutor implements Executor<ReturnInstruction> {
	@Override
	public void execute(ReturnInstruction inst, ExecutionContext ctx) {

		ctx.getHelper().endCtx(ctx, inst.getRegister());

		ctx.setReturning(true);
	}
}
