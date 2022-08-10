package me.darknet.lua.vm.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.instructions.SetGlobalInstruction;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.Value;

public class VMSetGlobalInstruction extends SetGlobalInstruction {

	@Getter
	Value value;

	public VMSetGlobalInstruction(int register, int target, Constant constant) {
		super(register, target, constant);
		this.value = ConstantConversion.toValue(constant);
	}
}
