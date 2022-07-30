package me.darknet.lua.vm.value;

public interface Value {

	double asNumber();
	String asString();
	boolean asBoolean();
	boolean isNil();
	Type getType();

}
