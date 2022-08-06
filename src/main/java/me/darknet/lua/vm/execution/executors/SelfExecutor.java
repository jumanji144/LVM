package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.SelfInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.Value;

public class SelfExecutor implements Executor<SelfInstruction> {
	@Override
	public void execute(SelfInstruction inst, ExecutionContext ctx) {
		int register = inst.getRegister();
		Value table = ctx.get(inst.getTable());

		// self will get the value of the table and copy the table after that
		// -> [value, self] (self being the table)
		// mostly value is a function, and then self is already pushed and is the first argument

		ctx.set(register + 1, table); // we have copied the table to the next register
		// now get the table value
		ctx.getHelper().getTable(ctx, table, ctx.getArgument(inst.getIndex()), register);
	}
}
