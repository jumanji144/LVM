package me.darknet.lua.file.instructions;

public class SetTableInstruction extends Instruction{

	int register;
	int table;
	int index;

	public SetTableInstruction(int register, int table, int index) {
		super(SETTABLE);
		this.register = register;
		this.table = table;
		this.index = index;
	}

	public int getRegister() {
		return register;
	}

	public int getTable() {
		return table;
	}

	public int getIndex() {
		return index;
	}

}
