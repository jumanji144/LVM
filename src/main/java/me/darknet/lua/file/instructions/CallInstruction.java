package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class CallInstruction extends Instruction {

	int register;
	int nargs;
	int nresults;

	public CallInstruction(int opcode, int register, int nargs, int nresults) {
		super(opcode);
		this.register = register;
		this.nargs = nargs;
		this.nresults = nresults;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ")(" + getNargs() + ") -> " + getNresults();
	}
}
