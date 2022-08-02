package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class CompareInstruction extends Instruction {

	int register;
	Argument a;
	Argument b;

	public CompareInstruction(int opcode, int register, Argument a, Argument b) {
		super(opcode);
		this.register = register;
		this.a = a;
		this.b = b;
	}

	@Override
	public String print() {
		return getOpcodeName() + " " + getRegister() + " := R(" + getA() + ") cmp R(" + getB() + ")";
	}
}
