package me.darknet.lua.file.instructions;

public class JumpInstruction extends Instruction {
	int offset;

	public JumpInstruction(int offset) {
		super(JMP);
		this.offset = offset;
	}
}
