package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.VarArgInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NilValue;
import me.darknet.lua.vm.value.Value;

public class VarArgExecutor implements Executor<VarArgInstruction> {
	@Override
	public void execute(VarArgInstruction instruction, ExecutionContext ctx) {
		Value[] varargs = ctx.getVarargs();
		// if varargs are of length 0, then set to nil
		int register = instruction.getRegister();
		int limit = instruction.getLimit() == 0 ? ctx.getRegisters().length : instruction.getLimit();
		for (int i = register; i < limit; i++) {
			ctx.set(i, varargs[i - register]);
		}
	}
}
