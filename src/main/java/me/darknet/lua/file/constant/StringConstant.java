package me.darknet.lua.file.constant;

public class StringConstant extends Constant {

	private final String value;

	public StringConstant(String value) {
		super(TSTRING);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
