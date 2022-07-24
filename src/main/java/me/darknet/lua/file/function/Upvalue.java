package me.darknet.lua.file.function;

public class Upvalue {

	private String name;
	private final boolean instack;
	private final int idx;
	private final int kind;

	public Upvalue(String name, boolean instack, int idx, int kind) {
		this.name = name;
		this.instack = instack;
		this.idx = idx;
		this.kind = kind;
	}

	public String getName() {
		return name;
	}

	public String setName(String name) {
		return this.name = name;
	}

	public boolean isInstack() {
		return instack;
	}

	public int getIdx() {
		return idx;
	}

	public int getKind() {
		return kind;
	}
}
