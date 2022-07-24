package me.darknet.lua.file.constant;

public class IntConstant extends Constant {

	private final int value;

	public IntConstant(int value) {
		super(VNUMINT);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
