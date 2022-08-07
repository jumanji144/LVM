package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ForLoopInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NumberValue;

public class ForLoopExecutor implements Executor<ForLoopInstruction> {
	@Override
	public void execute(ForLoopInstruction inst, ExecutionContext ctx) {

		double step = ctx.get(inst.getRegister() + 2).asNumber(); // step is the third register
		double limit = ctx.get(inst.getRegister() + 1).asNumber(); // limit is the second register
		NumberValue idx = (NumberValue) ctx.get(inst.getRegister()); // idx is the first register
		idx.setValue(idx.getValue() + step); // increment the index
		if(0 < step ? limit >= idx.getValue() : limit <= idx.getValue()) { // is withing limit range?
			ctx.setPc(ctx.getPc() + inst.getOffset()); // if so, continue
			ctx.set(inst.getRegister(), idx); // set the index back to the register
			ctx.set(inst.getRegister() + 3, new NumberValue(idx.getValue())); // set the index back to the register
		}

	}
}
