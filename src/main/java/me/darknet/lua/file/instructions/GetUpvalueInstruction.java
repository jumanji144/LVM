package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class GetUpvalueInstruction extends Instruction {

	int register;
	int upvalue;

	public GetUpvalueInstruction(int register, int upvalue) {
		super(GETUPVAL);
		this.register = register;
		this.upvalue = upvalue;
	}

	@Override
	public String print() {
		return getOpcodeName() + " R(" + getRegister() + ") <- upvalue(" + getUpvalue() + ")";
	}

}
