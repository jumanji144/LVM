package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ForPrepInstruction extends Instruction {
	int register;
	int offset;

	public ForPrepInstruction(int register, int offset) {
		super(FORPREP);
		this.register = register;
		this.offset = offset;
	}
}
