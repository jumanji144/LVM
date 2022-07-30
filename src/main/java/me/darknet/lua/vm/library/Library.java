package me.darknet.lua.vm.library;

import lombok.Getter;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.util.MethodConsumer;
import me.darknet.lua.vm.value.ClosureValue;
import me.darknet.lua.vm.value.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Library {

	private String name;
	private String globalName;
	private Map<String, Consumer<ExecutionContext>> methods = new HashMap<>();
	private Map<String, Value> constants = new HashMap<>();

	public Library(String name) {
		this.name = name;
		this.globalName = name;
	}

	public Library(String name, String globalName) {
		this.name = name;
		this.globalName = globalName;
	}

	public void set(String name, Consumer<ExecutionContext> ctx) {
		methods.put(name, ctx);
	}

	public void set(String name, Value value) {
		constants.put(name, value);
	}

	public void collect() {
		for (Method declaredMethod : getClass().getDeclaredMethods()) {
			if(declaredMethod.getName().startsWith("lua_")) {
				methods.put(declaredMethod.getName().substring(4), new MethodConsumer<>(declaredMethod));
			}
		}
	}

	public Table construct() {
		Table table = new Table();
		methods.forEach((name, value) -> table.set(name, new ClosureValue(new Closure(value, null))));
		constants.forEach(table::set);
		return table;
	}

}
