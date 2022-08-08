package me.darknet.lua.vm.data;

import lombok.Getter;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Lua closures are a way to represent functions.
 */
@Getter
public class Closure {

	Table env;
	LuaFunction luaFunction;
	Function<ExecutionContext, Integer> javaFunction;
	List<Value> upvalues = new ArrayList<>();

	public Closure(LuaFunction function, Table env) {
		this.luaFunction = function;
		this.env = env;
	}

	public Closure(Function<ExecutionContext, Integer> javaFunction, Table env) {
		this.javaFunction = javaFunction;
		this.env = env;
	}

	public boolean isLuaFunction() {
		return luaFunction != null;
	}

	public void setUpvalue(int index, Value value) {
		upvalues.add(index, value);
	}

	public Value getUpvalue(int index) {
		return upvalues.get(index);
	}

}
