package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.UnaryInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.Value;

public class LenExecutor implements Executor<UnaryInstruction> {
	@Override
	public void execute(UnaryInstruction inst, ExecutionContext ctx) {
		Value a = ctx.get(inst.getA());
		if(a instanceof StringValue st) {
			ctx.set(inst.getRegister(), new NumberValue(st.getValue().length()));
		} else {
			//ctx.throwError("attempt to get length of a %s value", a.getType().name().toLowerCase());
		}
	}
}
