package me.darknet.lua.vm.value;

public enum Type {

	NONE(""),
	NIL("nil"),
	BOOLEAN("boolean"),
	NUMBER("number"),
	STRING("string"),
	TABLE("table"),
	FUNCTION("function"),
	USERDATA("userdata"),
	THREAD("thread");

	private String name;

	Type(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
