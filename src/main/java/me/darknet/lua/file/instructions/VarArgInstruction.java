package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class VarArgInstruction extends Instruction {

	int register;
	int limit;

	public VarArgInstruction(int register, int limit) {
		super(VARARG);
		this.register = register;
		this.limit = limit;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + register + ")... -> " + limit + " := varargs";
	}
}
