package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.SetTableInstruction;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.TableValue;
import me.darknet.lua.vm.value.Value;

public class SetTableExecutor implements Executor<SetTableInstruction> {
	@Override
	public void execute(SetTableInstruction inst, ExecutionContext ctx) {
		Value key = ctx.getArgument(inst.getKey());
		Value value = ctx.getArgument(inst.getValue());
		Value table = ctx.get(inst.getRegister());

		ctx.getHelper().setTable(ctx, table, key, value);
	}
}
