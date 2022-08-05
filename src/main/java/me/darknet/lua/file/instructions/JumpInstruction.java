package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class JumpInstruction extends Instruction {
	int offset;

	public JumpInstruction(int offset) {
		super(JMP);
		this.offset = offset;
	}

	@Override
	public String print() {
		return super.print() + " -> " + offset;
	}
}
