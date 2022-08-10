package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ForPrepInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.Type;
import me.darknet.lua.vm.value.Value;

public class ForPrepExecutor implements Executor<ForPrepInstruction> {
	@Override
	public void execute(ForPrepInstruction inst, ExecutionContext ctx) {
		Value initial = ctx.get(inst.getRegister());
		Value plimit = ctx.get(inst.getRegister() + 1);
		Value pstep = ctx.get(inst.getRegister() + 2);

		if (initial.getType() != Type.NUMBER) ctx.throwError("for initial value must be a number");
		if (plimit.getType() != Type.NUMBER) ctx.throwError("for limit value must be a number");
		if (pstep.getType() != Type.NUMBER) ctx.throwError("for step value must be a number");
		// we actually need to subtract 1 step from the initial value because the for loop will increment it

		ctx.set(inst.getRegister(), new NumberValue(initial.asNumber() - pstep.asNumber()));

		ctx.setPc(ctx.getPc() + inst.getOffset());
	}
}
