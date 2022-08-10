package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class TestInstruction extends Instruction {

	int a;
	int b;

	public TestInstruction(int a, int b) {
		super(TEST);
		this.a = a;
		this.b = b;
	}

}
