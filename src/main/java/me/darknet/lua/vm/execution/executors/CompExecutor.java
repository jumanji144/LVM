package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.CompareInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.Value;

import java.util.function.BiFunction;

public class CompExecutor implements Executor<CompareInstruction> {

	BiFunction<Double, Double, Boolean> comp;

	public CompExecutor(BiFunction<Double, Double, Boolean> comp) {
		this.comp = comp;
	}

	@Override
	public void execute(CompareInstruction inst, ExecutionContext ctx) {
		Value a = ctx.getArgument(inst.getA());
		Value b = ctx.getArgument(inst.getB());
		int cond = inst.getRegister();
		boolean result;
		if(a.getType() == b.getType()) {
			if(a.isNil() || b.isNil()) {
				if(a.isNil()) {
					result = b.isNil();
				} else {
					result = a.isNil();
				}
			}else result = comp.apply(a.asNumber(), b.asNumber());
		} else {
			result = false;
		}
		// check if result == cond, then pc++
		if (result == (cond == 0)) {
			ctx.setPc(ctx.getPc() + 1);
		}
	}
}
