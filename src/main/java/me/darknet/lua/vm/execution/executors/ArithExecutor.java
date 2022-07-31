package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.instructions.ArithmeticInstruction;
import me.darknet.lua.file.instructions.Opcodes;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.VMException;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.Value;

import java.util.List;
import java.util.function.BiFunction;

public class ArithExecutor implements Executor<ArithmeticInstruction> {

	BiFunction<Value, Value, Value> op;

	public ArithExecutor(BiFunction<Value, Value, Value> op) {
		this.op = op;
	}

	@Override
	public void execute(ArithmeticInstruction instruction, ExecutionContext ctx) {
		Value a = ctx.getKRegister(instruction.getA());
		Value b = ctx.getKRegister(instruction.getB());
		try {
			Value result = op.apply(a, b);
			ctx.set(instruction.getRegister(), result);
		} catch (Exception e) {
			// problem type:
			Value problem = a instanceof NumberValue ? b : a;
			ctx.throwError("attempt to perform arithmetic on a %s value", problem.getType().name().toLowerCase());
		}
	}
}
