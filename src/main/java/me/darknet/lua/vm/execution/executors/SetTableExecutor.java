package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.SetTableInstruction;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.TableValue;
import me.darknet.lua.vm.value.Value;

public class SetTableExecutor implements Executor<SetTableInstruction> {
	@Override
	public void execute(SetTableInstruction instruction, ExecutionContext ctx) {
		Value key = ctx.getKRegister(instruction.getKeyRegister());
		Value value = ctx.getKRegister(instruction.getValueRegister());

		TableValue table = (TableValue) ctx.get(instruction.getRegister());

		Table t = table.getTable();
		t.set(key.asString(), value);
	}
}
