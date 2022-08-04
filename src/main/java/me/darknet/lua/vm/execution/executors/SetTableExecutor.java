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

		TableValue table = (TableValue) ctx.get(inst.getRegister());
		Table t = table.getTable();

		if(!t.has(key.asString())) { // key does not exist yet
			Table meta = t.getMetatable();
			if (meta != null) { // check if metatable exists
				Value metaFn = meta.get("__newindex"); // get __newindex
				if (metaFn instanceof TableValue tb) { // if it is a table
					tb.getTable().set(key.asString(), value); // set the value in that table
					return;
				}
				// else call the __newindex meta function
				ctx.getVm().getHelper().callMetaMethod(table, "__newindex", table, key, value);
				return;
			}
		}

		t.set(key.asString(), value);
	}
}
