package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class GetTableInstruction extends Instruction {

	int register;
	int table;
	Argument index;

	public GetTableInstruction(int register, int table, Argument index) {
		super(GETTABLE);
		this.register = register;
		this.table = table;
		this.index = index;
	}

	@Override
	public String print() {
		return super.print() + " R(" + table + ")[" + index.toString() + "] -> R(" + register + ")";
	}
}
