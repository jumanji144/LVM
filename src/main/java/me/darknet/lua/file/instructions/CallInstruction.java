package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class CallInstruction extends Instruction {

	int register;
	int numArgs;
	int numReturns;

	public CallInstruction(int opcode, int register, int numArgs, int numReturns) {
		super(opcode);
		this.register = register;
		this.numArgs = numArgs;
		this.numReturns = numReturns;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ")(" + getNumArgs() + ") -> " + getNumReturns();
	}
}
