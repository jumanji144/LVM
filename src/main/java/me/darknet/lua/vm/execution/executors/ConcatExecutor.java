package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ArithmeticInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.VMException;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.Value;

public class ConcatExecutor implements Executor<ArithmeticInstruction> {
	@Override
	public void execute(ArithmeticInstruction instruction, ExecutionContext ctx) {

		int begin = instruction.getA();
		int end = instruction.getB();

		StringBuilder builder = new StringBuilder();

		for(int i = begin; i <= end; i++) {
			Value a = ctx.get(i);
			try {
				builder.append(a.asString());
			} catch(Exception e) {
				throw new VMException("attempt to concat [" + a + "]", e);
			}
		}

		ctx.set(instruction.getRegister(), new StringValue(builder.toString()));

	}
}
