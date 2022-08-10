package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ArithmeticInstruction extends Instruction {

	int register;
	Argument a;
	Argument b;

	public ArithmeticInstruction(int opcode, int register, Argument a, Argument b) {
		super(opcode);
		this.register = register;
		this.a = a;
		this.b = b;
	}

	@Override
	public String print() {
		return getOpcodeName() + " R(" + a + ") & R(" + b + ")" + " := R(" + register + ")";

	}
}
