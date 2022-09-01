package me.darknet.lua.vm.execution;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.Argument;
import me.darknet.lua.file.instructions.Instruction;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.VMException;
import me.darknet.lua.vm.VMHelper;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.error.Error;
import me.darknet.lua.vm.error.ErrorHandler;
import me.darknet.lua.vm.util.ConstantConversion;
import me.darknet.lua.vm.value.TableValue;
import me.darknet.lua.vm.value.Type;
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
	Value envValue;
	LuaFunction function;
	Closure closure;
	Closure caller;
	ExecutionContext parent;
	// error handling
	Error error;
	Closure errorHandler;
	Value errorHandlerReturn;
	boolean returnOnError;
	int status;

	public ExecutionContext(Value[] stack) {
		this.stack = stack;
		this.top = stack.length;
	}

	public ExecutionContext(ExecutionContext ctx, int top, int base) {
		this.stack = ctx.getStack();
		setTop(top);
		setBase(base);
		this.parent = ctx;
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

	public int reg(int register) {
		return base + register;
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

	public void setEnv(Table env) {
		this.env = env;
		this.envValue = new TableValue(env);
	}

	// helper methods
	public Value getArgument(Argument argument) {
		if (argument.isConstant()) {
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

	public void checkStack(int size) {
		if (top + size > stack.length) {
			ensureSize(top + size);
		}
	}

	public VMHelper getHelper() {
		return vm.getHelper();
	}

	public void insert(int target, int location) {
		// first move everything to the right
		for (int i = top + 1; i > location - 1; i--) {
			stack[i + 1] = stack[i];
		}
		// then insert the value
		stack[location] = stack[target];
	}

	public void throwError(String fmt, Object... args) {
		if (closure.isLuaFunction())
			error = new Error(function.getSource(), function.getLine(pc), String.format(fmt, args));
		else error = new Error("", -1, String.format(fmt, args));
		throw new VMException(this);
	}

	public int findStackIndex(Value value) {
		if(!closure.isLuaFunction()) return -1;
		for (int i = base; i < function.getMaxStackSize(); i++) {
			if(stack[i] == value) return i - base;
		}
		return -1;
	}

	public void throwTypeError(Value value, String operation) {
		// first we must find the register this value lives in
		int index = findStackIndex(value);
		if(index == -1) { // value not found
			// fall back to basic type error
			throwError("attempt to %s a %s value", operation, value.getType().getName());
		} else {
			ErrorHandler.throwTypeError(this, index, operation);
		}
	}

	public boolean has(int register) {
		return register < (top - base);
	}

	public Value getRequired(int register) {
		if (!has(register)) throwError("bad argument %d value expected", register);
		return get(register);
	}

	public String optionalString(int register, String defaultValue) {
		if (!has(register)) return defaultValue;
		return get(register).asString();
	}

	public int optionalInt(int register, int defaultValue) {
		if (!has(register)) return defaultValue;
		return (int) get(register).asNumber();
	}

	public int size() {
		return top - base;
	}

	public int incPc() {
		return pc++;
	}

	public Instruction nextInstruction() {
		return function.getInstructions().get(incPc());
	}

	public String getSource() {
		if(closure.isLuaFunction()) {
			String source = function.getSource();
			if(source.equals("")) {
				if(parent != null) {
					return parent.getSource();
				} else {
					return "";
				}
			} else {
				return source;
			}
		}
		return "";
	}

	public <T extends Value> T checkType(int register, Type type) {
		Value value = get(register);
		if (value.getType() != type) throwError("bad argument %d expected %s got %s", register, type.getName(), get(register).getType().getName());
		return (T) value;
	}

}
