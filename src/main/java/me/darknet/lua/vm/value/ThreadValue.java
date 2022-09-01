package me.darknet.lua.vm.value;

import me.darknet.lua.vm.execution.ExecutionContext;

public class ThreadValue implements Value{

	ExecutionContext context;

	public ThreadValue(ExecutionContext context) {
		this.context = context;
	}

	public ExecutionContext getContext() {
		return context;
	}

	@Override
	public double asNumber() {
		throw new IllegalStateException("Thread is not a number");
	}

	@Override
	public String asString() {
		return "thread: " + Integer.toHexString(hashCode());
	}

	@Override
	public boolean asBoolean() {
		return true;
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.THREAD;
	}
}
