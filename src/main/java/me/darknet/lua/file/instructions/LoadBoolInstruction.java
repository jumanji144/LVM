package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class LoadBoolInstruction extends LoadInstruction{

	int register;
	int value;
	int condition;

	public LoadBoolInstruction(int register, int value, int condition) {
		super(LOADBOOL, register, value);
		this.register = register;
		this.value = value;
		this.condition = condition;
	}

	public boolean getValue() {
		return value != 0;
	}

	@Override
	public String toString() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ") := " + getValue() + ((getCondition() == 1) ? " skip next" : "");
	}
}
