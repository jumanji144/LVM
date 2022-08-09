package me.darknet.lua.vm.value;

public class NilValue implements Value {

	public static final NilValue NIL = new NilValue();

	@Override
	public double asNumber() {
		throw new UnsupportedOperationException("Nil cannot be a number");
	}

	@Override
	public String asString() {
		return "nil";
	}

	@Override
	public boolean asBoolean() {
		return false;
	}

	@Override
	public boolean isNil() {
		return true;
	}

	@Override
	public Type getType() {
		return Type.NIL;
	}
}

