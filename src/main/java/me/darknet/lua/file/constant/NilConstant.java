package me.darknet.lua.file.constant;

public class NilConstant extends Constant {

	public NilConstant() {
		super(TNIL);
	}

	@Override
	public String toString() {
		return "nil";
	}
}
