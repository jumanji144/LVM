package me.darknet.lua.file.instructions;

import lombok.Getter;

@Getter
public class SetInstruction extends Instruction{

	int target;
	int register;

	public SetInstruction(int op, int target, int register) {
		super(op);
		this.target = target;
		this.register = register;
	}

	@Override
	public String toString() {
		return getLine() + ": "  + getOpcodeName() + " " + getTarget() + " := R(" + getRegister() + ")";
	}
}
