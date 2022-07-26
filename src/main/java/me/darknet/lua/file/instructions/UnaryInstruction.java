package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class UnaryInstruction extends Instruction {

	int register;
	int a;

	public UnaryInstruction(int opcode, int register, int a) {
		super(opcode);
		this.register = register;
		this.a = a;
	}

}
