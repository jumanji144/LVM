package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.LoadConstantInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.util.ConstantConversion;

public class LoadConstantExecutor implements Executor<LoadConstantInstruction> {
	@Override
	public void execute(LoadConstantInstruction inst, ExecutionContext ctx) {
		ctx.set(inst.getRegister(), ConstantConversion.toValue(inst.getConstant()));
	}
}
