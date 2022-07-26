package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class CompareInstruction extends Instruction {

	int register;
	int a;
	int b;

	public CompareInstruction(int opcode, int register, int a, int b) {
		super(opcode);
		this.register = register;
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " " + getRegister() + " := R(" + getA() + ") cmp R(" + getB() + ")";
	}
}
