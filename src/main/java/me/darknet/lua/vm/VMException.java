package me.darknet.lua.vm;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;

public class VMException extends RuntimeException {

	public ExecutionContext ctx;
	public Exception cause;

	public VMException(ExecutionContext ctx) {
		this.ctx = ctx;
	}

	public VMException(ExecutionContext ctx, Exception cause) {
		super(cause);
		this.ctx = ctx;
		this.cause = cause;
	}

	public ExecutionContext getCtx() {
		return ctx;
	}

	@Override
	public String getMessage() {
		if (ctx.getError() != null)
			return ctx.getError().getMessage();
		else
			return super.getMessage();
	}

	public String constructStackTrace() {
		StringBuffer sb = new StringBuffer();
		sb.append("lua stack trace:\n");
		ExecutionContext ctx = this.ctx;
		while (ctx != null) {
			Closure cl = ctx.getClosure();
			if (cl.isLuaFunction()) {
				LuaFunction f = cl.getLuaFunction();
				int pc = ctx.getPc();
				int line = f.getLine(pc);
				sb.append(" - ").append(ctx.getSource()).append(":").append(line);
				if (ctx.getParent() == null) sb.append(" (main)");
				sb.append("\n");
			} else {
				sb.append("\t[java call]\n");
			}
			ctx = ctx.getParent();
		}
		return sb.toString();
	}
}
