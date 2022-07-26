package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ClosureInstruction extends Instruction {
	int register;
	int proto;

	public ClosureInstruction(int register, int proto) {
		super(CLOSURE);
		this.register = register;
		this.proto = proto;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ") <- func(" + getProto() + ")";
	}
}
