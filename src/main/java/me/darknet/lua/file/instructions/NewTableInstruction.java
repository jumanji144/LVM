package me.darknet.lua.file.instructions;

public class NewTableInstruction extends Instruction {

	int register;
	int narray;
	int nrec;

	public NewTableInstruction(int register, int narray, int nrec) {
		super(NEWTABLE);
		this.register = register;
		this.narray = narray;
		this.nrec = nrec;
	}

	public int getRegister() {
		return register;
	}

	public int getNarray() {
		return narray;
	}

	public int getNrec() {
		return nrec;
	}
}