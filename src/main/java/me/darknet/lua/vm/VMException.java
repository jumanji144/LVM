package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.execution.ExecutionContext;

public class VMException extends RuntimeException{

	public ExecutionContext ctx;

	public VMException(ExecutionContext ctx) {
		this.ctx = ctx;
	}

	public ExecutionContext getCtx() {
		return ctx;
	}

	@Override
	public String getMessage() {
		return ctx.getCurrentError().print();
	}

	public String constructStackTrace() {
		StringBuffer sb = new StringBuffer();
		sb.append("lua stack trace:\n");
		ExecutionContext ctx = this.ctx;
		String source = ctx.getCurrentError().getSource();
		while(ctx != null) {
			LuaFunction function = ctx.getCurrentFunction();
			sb.append("\t- ").append(source).append(':').append(function.getLine(ctx.getPc())).append("\n");
			ctx = ctx.getCaller();
		}
		return sb.toString();
	}
}
