package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.LoadConstantInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.util.ConstantConversion;

public class LoadConstantExecutor implements Executor<LoadConstantInstruction> {
	@Override
	public void execute(LoadConstantInstruction instruction, ExecutionContext ctx) {
		ctx.set(instruction.getRegister(), ConstantConversion.toValue(instruction.getConstant()));
	}
}
