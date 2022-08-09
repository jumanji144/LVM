package me.darknet.lua.vm.value;

import lombok.Getter;
import lombok.Setter;

public class BooleanValue implements Value {
	@Getter
	@Setter
	private boolean value;

	public BooleanValue(boolean value) {
		this.value = value;
	}

	public boolean asBoolean() {
		return value;
	}

	public String asString() {
		return value ? "true" : "false";
	}

	public double asNumber() {
		return value ? 1 : 0;
	}

	public boolean isNil() {
		return false;
	}

	public Type getType() {
		return Type.BOOLEAN;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BooleanValue){
			return value == ((BooleanValue) obj).value;
		}
		return false;
	}
}
