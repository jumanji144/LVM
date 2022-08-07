package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ForLoopInstruction extends Instruction {

	int register;
	int offset;

	public ForLoopInstruction(int opcode, int register, int offset) {
		super(opcode);
		this.register = register;
		this.offset = offset;
	}

	@Override
	public String print() {
		return super.print() + " jmp " + (offset - 1);
	}
}
