package me.darknet.lua.file.constant;

public class BooleanConstant extends Constant {

	private final boolean value;

	public BooleanConstant(boolean value) {
		super(TBOOLEAN);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public int getVariant() {
		return value ? VTRUE : VFALSE;
	}
}
