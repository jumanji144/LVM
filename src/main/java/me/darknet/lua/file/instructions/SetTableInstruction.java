package me.darknet.lua.file.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;

@Getter
public class SetTableInstruction extends Instruction{

	int register;
	int keyRegister;
	int valueRegister;

	public SetTableInstruction(int register, int keyRegister, int valueRegister) {
		super(SETTABLE);
		this.register = register;
		this.keyRegister = keyRegister;
		this.valueRegister = valueRegister;
	}

	public int getRegister() {
		return register;
	}

}
