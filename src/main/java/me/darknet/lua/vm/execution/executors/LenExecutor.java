package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.UnaryInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.*;

public class LenExecutor implements Executor<UnaryInstruction> {
	@Override
	public void execute(UnaryInstruction inst, ExecutionContext ctx) {
		Value a = ctx.get(inst.getA());
		if(a instanceof StringValue st) {
			ctx.set(inst.getRegister(), new NumberValue(st.getValue().length()));
		} else if(a instanceof TableValue t) {
			ctx.set(inst.getRegister(), new NumberValue(t.getTable().getArray().size()));
		} else {
			if(!ctx.getHelper().attemptMetamethod(ctx, a, NilValue.NIL, ctx.reg(inst.getRegister()), "__len")) {
				ctx.throwError("attempt to get length of a " + a.getType().getName() + " value");
			}
 		}
	}
}
