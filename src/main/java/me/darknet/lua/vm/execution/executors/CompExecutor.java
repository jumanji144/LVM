package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.CompareInstruction;
import me.darknet.lua.file.instructions.Opcodes;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.util.TriExecutor;
import me.darknet.lua.vm.value.Value;

public class CompExecutor implements Executor<CompareInstruction> {
	TriExecutor comp;

	public CompExecutor(TriExecutor comp) {
		this.comp = comp;
	}

	@Override
	public void execute(CompareInstruction inst, ExecutionContext ctx) {
		Value a = ctx.getArgument(inst.getA());
		Value b = ctx.getArgument(inst.getB());
		int cond = inst.getRegister();
		int result = (a.getType() == b.getType() && comp.accept(ctx, a, b)) ? 1 : 0;
		// check if result == cond, then pc++
		if (result != cond) {
			ctx.setPc(ctx.getPc() + 1);
		}
	}
}
