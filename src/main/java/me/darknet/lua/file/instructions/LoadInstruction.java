package me.darknet.lua.file.instructions;

/**
 * GETUPVAL
 * GETGLOBAL
 * LOADK
 * LOADNIL
 * MOVE
 */
public class LoadInstruction extends Instruction {

	int register;
	int target;

	public LoadInstruction(int opcode, int register, int target) {
		super(opcode);
		this.register = register;
		this.target = target;
	}

	public int getRegister() {
		return register;
	}

	public int getTarget() {
		return target;
	}

	@Override
	public String print() {
		return getLine() + ": " + getOpcodeName() + " R(" + getRegister() + ") := " + getTarget();
	}
}
