package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.ArithmeticInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.VMException;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.Value;

import java.util.function.BiFunction;

public class ArithExecutor implements Executor<ArithmeticInstruction> {

	BiFunction<Value, Value, Value> op;

	public ArithExecutor(BiFunction<Value, Value, Value> op) {
		this.op = op;
	}

	@Override
	public void execute(ArithmeticInstruction instruction, ExecutionContext ctx) {
		Value a = ctx.get(instruction.getA());
		Value b = ctx.get(instruction.getB());
		try {
			Value result = op.apply(a, b);
			ctx.set(instruction.getRegister(), result);
		} catch (Exception e) {
			// problem type:
			Value problem = a instanceof NumberValue ? b : a;
			throw new VMException("attempt to perform arithmetic on a non-number [" + problem + "]", e);
		}
	}
}
