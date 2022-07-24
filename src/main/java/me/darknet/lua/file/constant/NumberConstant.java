package me.darknet.lua.file.constant;

public class NumberConstant extends Constant {

	private final double value;

	public NumberConstant(double value) {
		super(TNUMBER);
		this.value = value;
	}

	public double getValue() {
		return value;
	}

}
