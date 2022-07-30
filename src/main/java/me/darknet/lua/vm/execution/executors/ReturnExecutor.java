package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.CallInstruction;
import me.darknet.lua.file.instructions.Instruction;
import me.darknet.lua.file.instructions.Opcodes;
import me.darknet.lua.file.instructions.ReturnInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.Value;

public class ReturnExecutor implements Executor<ReturnInstruction> {
	@Override
	public void execute(ReturnInstruction instruction, ExecutionContext ctx) {
		Value[] returnValues = new Value[instruction.getNumReturns() - 1];
		for(int i = 0; i < returnValues.length; i++) {
			returnValues[i] = ctx.get(instruction.getRegister() + i);
		}
		ctx.setReturnValues(returnValues);
		ctx.setReturning(true);
	}
}
