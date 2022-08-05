package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.BooleanValue;
import me.darknet.lua.vm.value.ClosureValue;
import me.darknet.lua.vm.value.StringValue;

public class BaseLibrary extends Library {

	private static final String VERSION = "Lua 5.1";

	public BaseLibrary() {
		super("base", "");
		set("_VERSION", new StringValue(VERSION));
	}

	public int lua_pcall(ExecutionContext ctx) {
		ClosureValue closure = (ClosureValue) ctx.get(0);
		Closure cl = closure.getClosure();

		ExecutionContext newContext = ctx.getHelper().prepareCtx(ctx, cl, ctx.reg(0), -1);

		ctx.getHelper().invoke(newContext);

		boolean status = newContext.getError() != null;

		ctx.push(new BooleanValue(status));
		if(!status) ctx.push(new StringValue(newContext.getError().print()));

		ctx.insert(ctx.getTop(), ctx.getBase());

		return ctx.getTop() - ctx.getBase();
	}

	public int lua_xpcall(ExecutionContext ctx) {

		// TODO: stack offsets are not correct, res1 of errFunc gets put at reg(3) instead of reg(1)
		// probably requires manual stack management

		ClosureValue closure = (ClosureValue) ctx.get(0);
		Closure cl = closure.getClosure();
		ClosureValue errorHandler = (ClosureValue) ctx.get(1);
		Closure eh = errorHandler.getClosure();

		int top = ctx.getTop(); // remember top

		ExecutionContext newContext = ctx.getHelper().prepareCtx(ctx, cl, ctx.reg(0), -1);
		newContext.setErrorHandler(eh);

		ctx.getHelper().invoke(newContext);

		boolean status = newContext.getError() == null;

		if(!status) {
			ctx.setTop(top); // restore top
			ctx.push(newContext.getErrorHandlerReturn());
		}

		ctx.push(new BooleanValue(status));

		ctx.insert(ctx.getTop(), ctx.getBase());

		return ctx.getTop() - ctx.getBase();
	}


	public int lua_print(ExecutionContext ctx) {
		int n = ctx.getTop() - ctx.getBase();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n; i++) {
			sb.append(ctx.get(i).asString());
			if(n > 1) sb.append("\t");
		}
		System.out.println(sb);
		return 0; // no return values
	}



}
