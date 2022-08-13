package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.NilValue;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.StringValue;

public class OsLibrary extends Library {
	public OsLibrary() {
		super("os");
	}

	public int lua_clock(ExecutionContext ctx) {
		//Returns an approximation of the amount in seconds of CPU time used by the program.
		double clk = (System.nanoTime() - VM.VM_BOOT_TIME) / 1000000000.0;
		ctx.push(new NumberValue(clk));
		return 1;
	}

	public int lua_getenv(ExecutionContext ctx) {
		//Returns the value of the environment variable named name, or nil if the variable is not defined.
		String name = ctx.getRequired(0).asString();
		String value = System.getenv(name);
		if (value == null) {
			ctx.push(NilValue.NIL);
		} else {
			ctx.push(new StringValue(value));
		}
		return 1;
	}
}
