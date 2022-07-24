package me.darknet.lua.file.function;

public class AbsoluteLineInfo {

	private final int line;
	private final int pc;

	public AbsoluteLineInfo(int line, int pc) {
		this.line = line;
		this.pc = pc;
	}

	public int getLine() {
		return line;
	}

	public int getPc() {
		return pc;
	}
}
