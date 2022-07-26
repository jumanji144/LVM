package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class TestSetInstruction extends Instruction {

	int register;
	int a;
	int b;

	public TestSetInstruction(int register, int a, int b) {
		super(TESTSET);
		this.register = register;
		this.a = a;
		this.b = b;
	}

}
