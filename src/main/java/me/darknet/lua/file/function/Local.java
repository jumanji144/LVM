package me.darknet.lua.file.function;

public class Local {

	private final String varname;
	private final int startpc;
	private final int endpc;

	public Local(String varname, int startpc, int endpc) {
		this.varname = varname;
		this.startpc = startpc;
		this.endpc = endpc;
	}

	public String getVarname() {
		return varname;
	}

	public int getStartpc() {
		return startpc;
	}

	public int getEndpc() {
		return endpc;
	}

}
