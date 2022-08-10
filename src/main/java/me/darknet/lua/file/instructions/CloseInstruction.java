package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class CloseInstruction extends Instruction {

	int register;

	public CloseInstruction(int register) {
		super(CLOSE);
		this.register = register;
	}

}
