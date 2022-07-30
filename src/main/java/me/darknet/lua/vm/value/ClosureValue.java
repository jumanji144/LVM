package me.darknet.lua.vm.value;

import lombok.Getter;
import me.darknet.lua.vm.data.Closure;

@Getter
public class ClosureValue implements Value{

	Closure closure;

	public ClosureValue(Closure closure){
		this.closure = closure;
	}

	@Override
	public double asNumber() {
		throw new UnsupportedOperationException("Closure cannot be a number");
	}

	@Override
	public String asString() {
		return "function " + Integer.toHexString(hashCode());
	}

	@Override
	public boolean asBoolean() {
		throw new UnsupportedOperationException("Closure cannot be a boolean");
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.FUNCTION;
	}
}
