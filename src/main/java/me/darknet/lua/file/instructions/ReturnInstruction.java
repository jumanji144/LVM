package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ReturnInstruction extends Instruction {
	int register;
	int nreturn;

	public ReturnInstruction(int register, int nreturn) {
		super(RETURN);
		this.register = register;
		this.nreturn = nreturn;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ") <- " + getNreturn();
	}
}
