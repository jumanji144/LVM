package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.GetTableInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.Value;

public class GetTableExecutor implements Executor<GetTableInstruction> {

	@Override
	public void execute(GetTableInstruction inst, ExecutionContext ctx) {
		Value value = ctx.get(inst.getTable());
		Value indexValue = ctx.getArgument(inst.getIndex());
		int register = ctx.reg(inst.getRegister());

		ctx.getHelper().getTable(ctx, value, indexValue, register);

	}
}
