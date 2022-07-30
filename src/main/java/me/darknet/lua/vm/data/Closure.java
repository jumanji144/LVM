package me.darknet.lua.vm.data;

import lombok.Getter;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.function.Upvalue;
import me.darknet.lua.vm.execution.ExecutionContext;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lua closures are a way to represent functions.
 */
@Getter
public class Closure {

	Table env;
	LuaFunction luaFunction;
	Consumer<ExecutionContext> javaFunction;

	public Closure(LuaFunction function, Table env) {
		this.luaFunction = function;
		this.env = env;
	}

	public Closure(Consumer<ExecutionContext> javaFunction, Table env) {
		this.javaFunction = javaFunction;
		this.env = env;
	}

	public boolean isLuaFunction() {
		return luaFunction != null;
	}

}
