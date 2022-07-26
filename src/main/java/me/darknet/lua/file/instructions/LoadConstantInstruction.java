package me.darknet.lua.file.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;

public class LoadConstantInstruction extends LoadInstruction {

	@Getter
	Constant constant;

	public LoadConstantInstruction(int opcode, int register, int target, Constant constant) {
		super(opcode, register, target);
		this.constant = constant;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ") := K(" + getTarget() + ") [" + constant.toString() + "]";
	}
}
