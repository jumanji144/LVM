package me.darknet.lua.file.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;

public class SetGlobalInstruction extends SetInstruction{

	@Getter
	Constant constant;

	public SetGlobalInstruction(int register, int target, Constant constant) {
		super(SETGLOBAL, target, register);
		this.constant = constant;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " GLOBAL[K(" + getTarget() + ") [" + getConstant() + "]] := " + "R(" + getRegister() + ")";
	}
}
