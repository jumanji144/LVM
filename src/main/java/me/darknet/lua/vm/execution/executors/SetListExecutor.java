package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.SetListInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.TableValue;
import me.darknet.lua.vm.value.Value;

public class SetListExecutor implements Executor<SetListInstruction> {

	@Override
	public void execute(SetListInstruction inst, ExecutionContext ctx) {

		int amount = inst.getAmount();
		int offset = inst.getOffset();

		if(amount == 0) {
			amount = (ctx.getTop() - ctx.reg(inst.getRegister())) - 1;
		}

		TableValue table = (TableValue) ctx.get(inst.getRegister());

		for (int i = 0; i < amount; i++) {
			Value value = ctx.get(inst.getRegister() + i + 1);
			table.getTable().insert(i, value);
		}

	}

}
