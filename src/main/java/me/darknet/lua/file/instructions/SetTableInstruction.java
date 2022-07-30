package me.darknet.lua.file.instructions;

import lombok.Getter;
import me.darknet.lua.file.constant.Constant;

@Getter
public class SetTableInstruction extends Instruction{

	int register;
	int keyRegister;
	int valueRegister;

	Constant key;
	Constant value;


	public SetTableInstruction(int register, int keyRegister, int valueRegister, Constant key, Constant value) {
		super(SETTABLE);
		this.register = register;
		this.keyRegister = keyRegister;
		this.valueRegister = valueRegister;
		this.key = key;
		this.value = value;
	}

	public int getRegister() {
		return register;
	}

}
