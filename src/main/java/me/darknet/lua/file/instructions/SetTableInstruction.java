package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class SetTableInstruction extends Instruction {

	int register;
	Argument key;
	Argument value;

	public SetTableInstruction(int register, Argument key, Argument value) {
		super(SETTABLE);
		this.register = register;
		this.key = key;
		this.value = value;
	}

	public int getRegister() {
		return register;
	}

}
