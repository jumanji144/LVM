package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ConcatInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.Value;

public class ConcatExecutor implements Executor<ConcatInstruction> {
	@Override
	public void execute(ConcatInstruction inst, ExecutionContext ctx) {

		int begin = inst.getBegin();
		int end = inst.getEnd();

		StringBuilder builder = new StringBuilder();

		for(int i = begin; i <= end; i++) {
			Value a = ctx.get(i);
			try {
				builder.append(a.asString());
			} catch(Exception e) {
				//ctx.throwError("attempt to concatenate a %s value", a.getType().name().toLowerCase());
				return;
			}
		}

		ctx.set(inst.getRegister(), new StringValue(builder.toString()));

	}
}
