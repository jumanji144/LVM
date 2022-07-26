package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class GetTableInstruction extends Instruction {

	int register;
	int table;
	int index;

	public GetTableInstruction(int register, int table, int index) {
		super(GETTABLE);
		this.register = register;
		this.table = table;
		this.index = index;
	}
}
