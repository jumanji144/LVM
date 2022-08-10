package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.LoadConstantInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.instructions.VMLoadConstantInstruction;

public class LoadConstantExecutor implements Executor<LoadConstantInstruction> {
	@Override
	public void execute(LoadConstantInstruction inst, ExecutionContext ctx) {
		if (inst instanceof VMLoadConstantInstruction vmInst)
			ctx.set(inst.getRegister(), vmInst.getValue());
		else {
			ctx.getFunction().getInstructions().set(ctx.getPc(),
					new VMLoadConstantInstruction(inst.getOpcode(), inst.getRegister(), inst.getTarget(), inst.getConstant())
			);
			ctx.setPc(ctx.getPc() - 1);
		}
	}
}
