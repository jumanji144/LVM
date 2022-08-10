package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.GetGlobalInstruction;
import me.darknet.lua.file.instructions.SetGlobalInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.instructions.VMGetGlobalInstruction;
import me.darknet.lua.vm.instructions.VMSetGlobalInstruction;
import me.darknet.lua.vm.value.TableValue;

public class SetGlobalExecutor implements Executor<SetGlobalInstruction> {
	@Override
	public void execute(SetGlobalInstruction inst, ExecutionContext ctx) {
		if(inst instanceof VMSetGlobalInstruction vmInst) {
			ctx.getHelper().setTable(ctx, ctx.getEnvValue(), vmInst.getValue(), ctx.get(inst.getRegister()));
		} else {
			ctx.getFunction().getInstructions().set(ctx.getPc(),
					new VMSetGlobalInstruction(inst.getRegister(), inst.getTarget(), inst.getConstant())
			);
			ctx.setPc(ctx.getPc() - 1);
		}
	}
}
