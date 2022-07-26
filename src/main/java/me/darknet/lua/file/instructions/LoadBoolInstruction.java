package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class LoadBoolInstruction extends Instruction{

	int register;
	int value;
	int condition;

	public LoadBoolInstruction(int register, int value, int condition) {
		super(LOADBOOL);
		this.register = register;
		this.value = value;
		this.condition = condition;
	}



}
