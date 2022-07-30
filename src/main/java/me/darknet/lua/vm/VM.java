package me.darknet.lua.vm;

import lombok.Getter;

@Getter
public class VM {

	VMHelper helper;
	Interpreter interpreter;

	public void initalize() {
		interpreter = new Interpreter();
		helper = new VMHelper(interpreter, this);
	}

}
