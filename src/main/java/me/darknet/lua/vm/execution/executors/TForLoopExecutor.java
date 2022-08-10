package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ForLoopInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.Value;

public class TForLoopExecutor implements Executor<ForLoopInstruction> {

	@Override
	public void execute(ForLoopInstruction inst, ExecutionContext ctx) {

		int register = ctx.reg(inst.getRegister());
		// prepare a call
		int callBase = register + 3;
		ctx.setRaw(callBase + 2, ctx.getRaw(register + 2));
		ctx.setRaw(callBase + 1, ctx.getRaw(register + 1));
		ctx.setRaw(callBase, ctx.getRaw(register));
		int oldTop = ctx.getTop();
		ctx.setTop(callBase + 3);
		ctx.getHelper().invoke(ctx, callBase, inst.getOffset()); // offset is in this case numRet
		ctx.setTop(oldTop); // restore old state
		Value value = ctx.getRaw(callBase); // get result value
		if (!value.isNil()) {
			ctx.setRaw(callBase - 1, value); // control value
		} else {
			// skip next instruction
			ctx.incPc();
		}

	}

}
