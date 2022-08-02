package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class SelfInstruction extends Instruction {

	int register;
	int table;
	Argument index;

	public SelfInstruction(int register, int table, Argument index) {
		super(SELF);
		this.register = register;
		this.table = table;
		this.index = index;
	}

}
