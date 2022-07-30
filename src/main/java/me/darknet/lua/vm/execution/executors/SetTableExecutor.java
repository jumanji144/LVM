package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.instructions.SetTableInstruction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.TableValue;
import me.darknet.lua.vm.value.Value;

public class SetTableExecutor implements Executor<SetTableInstruction> {
	@Override
	public void execute(SetTableInstruction instruction, ExecutionContext ctx) {
		Constant key = instruction.getKey();
		Constant value = instruction.getValue();
		Value keyValue = ConstantConversion.toValue(key);
		Value valueValue = ConstantConversion.toValue(value);

		TableValue table = (TableValue) ctx.get(instruction.getRegister());

	}
}
