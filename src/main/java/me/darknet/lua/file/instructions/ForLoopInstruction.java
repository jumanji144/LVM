package me.darknet.lua.file.instructions;

public class ForLoopInstruction extends Instruction {

	int register;
	int offset;

	public ForLoopInstruction(int opcode, int register, int offset) {
		super(opcode);
		this.register = register;
		this.offset = offset;
	}

}
