package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class ConcatInstruction extends Instruction {

	int register;
	int begin;
	int end;

	public ConcatInstruction(int register, int begin, int end) {
		super(CONCAT);
		this.register = register;
		this.begin = begin;
		this.end = end;
	}


}
