package me.darknet.lua.vm.execution;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.Argument;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.VMHelper;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.error.Error;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.Value;

@Getter
@Setter
public class ExecutionContext {

	VM vm; // owning VM
	Value[] stack;
	int top, base; // top of stack
	int pc;
	// state
	int numResults;
	int functionReturn;
	boolean returning;
	Table env;
	LuaFunction function;
	Closure closure;
	Closure caller;
	// error handling
	Error error;
	Closure errorHandler;

	public ExecutionContext(Value[] stack) {
		this.stack = stack;
		this.top = stack.length;
	}

	public ExecutionContext(ExecutionContext ctx, int top, int base) {
		this.stack = ctx.getStack();
		setTop(top);
		setBase(base);
	}

	public Value get(int register) {
		return stack[base + register];
	}

	public void set(int register, Value value) {
		stack[base + register] = value;
	}

	public Value getRaw(int index) {
		return stack[index];
	}

	public void setRaw(int index, Value value) {
		stack[index] = value;
	}

	public void push(Value value) {
		stack[top++] = value;
	}

	public Value pop() {
		return stack[--top];
	}

	public void setTop(int top) {
		// grow stack if needed
		if (top > stack.length) {
			Value[] newStack = new Value[top * 2];
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
		this.top = top;
	}

	// helper methods
	public Value getArgument(Argument argument) {
		if(argument.isConstant()) {
			return ConstantConversion.toValue(argument.getConstant());
		} else {
			return get(argument.getValue());
		}
	}

	public Value[] stackView() {
		Value[] view = new Value[top - base];
		// create a view of the stack
		System.arraycopy(stack, base, view, 0, view.length);
		return view;
	}

	public void ensureSize(int i) {
		if (i > stack.length) {
			Value[] newStack = new Value[i * 2];
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
	}

	public VMHelper getHelper() {
		return vm.getHelper();
	}
}
