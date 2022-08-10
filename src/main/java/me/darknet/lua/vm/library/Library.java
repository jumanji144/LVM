package me.darknet.lua.vm.library;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.Interpreter;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.VMException;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.ClosureValue;
import me.darknet.lua.vm.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
public class Library {

	private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
	private final String name;
	private final String globalName;
	private final Map<String, Function<ExecutionContext, Integer>> methods = new HashMap<>();
	private final Map<String, Value> constants = new HashMap<>();
	@Setter
	private VM vm;
	private Table table;

	public Library(String name) {
		this.name = name;
		this.globalName = name;
	}

	public Library(String name, String globalName) {
		this.name = name;
		this.globalName = globalName;
	}

	public void set(String name, Function<ExecutionContext, Integer> ctx) {
		methods.put(name, ctx);
	}

	public void set(String name, Value value) {
		constants.put(name, value);
	}

	public Value get(String index) {
		return table.get(index);
	}

	public ClosureValue newClosure(Function<ExecutionContext, Integer> function) {
		return new ClosureValue(new Closure(function, null));
	}

	public void collect() {
		for (Method declaredMethod : getClass().getDeclaredMethods()) {
			if (declaredMethod.getName().startsWith("lua_")) {
				methods.put(declaredMethod.getName().substring(4), (ctx) -> {
					try {
						Object returnValue = declaredMethod.invoke(this, ctx);
						if (returnValue == null) {
							logger.error("Method {} returned null (not a int method)", declaredMethod.getName());
							throw new RuntimeException("Method returned null");
						} else return (int) returnValue;
					} catch (InvocationTargetException e) {
						if (e.getCause() instanceof VMException vm) {
							throw vm;
						}
						throw new RuntimeException(e);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
		}
	}

	public Table construct() {
		Table table = new Table();
		methods.forEach((name, value) -> table.set(name, new ClosureValue(new Closure(value, null))));
		constants.forEach(table::set);
		this.table = table;
		return table;
	}

}
