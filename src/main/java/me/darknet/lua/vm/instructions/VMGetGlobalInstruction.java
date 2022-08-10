package me.darknet.lua.vm.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.instructions.GetGlobalInstruction;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.Value;

public class VMGetGlobalInstruction extends GetGlobalInstruction {

	@Getter
	Value value;

	public VMGetGlobalInstruction(int register, int target, Constant constant) {
		super(register, target, constant);
		this.value = ConstantConversion.toValue(constant);
	}
}
