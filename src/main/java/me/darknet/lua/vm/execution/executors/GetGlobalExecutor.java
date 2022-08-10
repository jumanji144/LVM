package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.GetGlobalInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.instructions.VMGetGlobalInstruction;

public class GetGlobalExecutor implements Executor<GetGlobalInstruction> {
	@Override
	public void execute(GetGlobalInstruction inst, ExecutionContext ctx) {
		if (inst instanceof VMGetGlobalInstruction vmInst) {
			ctx.getHelper().getTable(ctx, ctx.getEnvValue(), vmInst.getValue(), ctx.reg(inst.getRegister()));
		} else {
			ctx.getFunction().getInstructions().set(ctx.getPc(),
					new VMGetGlobalInstruction(inst.getRegister(), inst.getTarget(), inst.getConstant())
			);
			ctx.setPc(ctx.getPc() - 1);
		}
	}
}
