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
		int amount = instruction.getNumReturns() == 0 ?
				ctx.getRegisters().length - instruction.getRegister()
				: instruction.getNumReturns() - 1;
		Value[] returnValues = new Value[amount];
		System.arraycopy(ctx.getRegisters(), instruction.getRegister(), returnValues, 0, returnValues.length);
		ctx.setReturnValues(returnValues);
		ctx.setReturning(true);
	}
}
