package me.darknet.lua.file.constant;

public class Constant implements ConstantTypes {

	private final int type;

	public Constant(int type) {
		this.type = type;
	}

	public int getVariant() {
		return type;
	}

	public int getType() {
		return type;
	}

}
