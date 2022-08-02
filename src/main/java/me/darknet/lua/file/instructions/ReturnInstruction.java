package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ReturnInstruction extends Instruction {
	int register;
	int numReturns;

	public ReturnInstruction(int register, int numReturns) {
		super(RETURN);
		this.register = register;
		this.numReturns = numReturns;
	}

	@Override
	public String print() {
		return getOpcodeName() + " R(" + getRegister() + ") <- " + getNumReturns();
	}
}
