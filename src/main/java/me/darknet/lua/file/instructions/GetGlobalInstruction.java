package me.darknet.lua.file.instructions;

import me.darknet.lua.file.constant.Constant;

public class GetGlobalInstruction extends LoadConstantInstruction{
	public GetGlobalInstruction(int register, int target, Constant constant) {
		super(GETGLOBAL, register, target, constant);
	}

	@Override
	public String print() {
		return getOpcodeName() + " R(" + getRegister() + ") := GLOBAL[K(" + getTarget() + ") [" + getConstant() + "]]";
	}
}
