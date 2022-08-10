package me.darknet.lua.vm.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.instructions.Instruction;
import me.darknet.lua.file.instructions.LoadConstantInstruction;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.Value;

public class VMLoadConstantInstruction extends LoadConstantInstruction {

	@Getter
	Value value;

	public VMLoadConstantInstruction(int opcode, int register, int target, Constant constant) {
		super(opcode, register, target, constant);
		this.value = ConstantConversion.toValue(constant);
	}
}
