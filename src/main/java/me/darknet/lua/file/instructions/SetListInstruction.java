package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class SetListInstruction extends Instruction {

	int register;
	int amount;
	int offset;

	public SetListInstruction(int register, int amount, int offset) {
		super(SETLIST);
		this.register = register;
		this.amount = amount;
		this.offset = offset;
	}

}
