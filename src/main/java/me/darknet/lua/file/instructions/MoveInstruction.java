package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class MoveInstruction extends Instruction {

	int register;
	int from;

	public MoveInstruction(int register, int from) {
		super(MOVE);
		this.register = register;
		this.from = from;
	}

	@Override
	public String print() {
		return getOpcodeName() + " R(" + getRegister() + ") <- R(" + getFrom() + ")";
	}
}
