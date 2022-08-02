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
	public String print() {
		return getOpcodeName() + " R(" + register + ")... -> " + limit + " := varargs";
	}
}
