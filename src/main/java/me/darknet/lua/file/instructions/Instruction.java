package me.darknet.lua.file.instructions;

public class Instruction implements Opcodes {

	int opcode;
	int line;

	public Instruction(int opcode) {
		this.opcode = opcode;
	}

	public int getOpcode() {
		return opcode;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getOpcodeName() {
		return Opcodes.OPCODES[opcode];
	}

	public String print() {
		return getOpcodeName();
	}

}
