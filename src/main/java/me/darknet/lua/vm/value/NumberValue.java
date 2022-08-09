package me.darknet.lua.vm.value;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.util.StringUtil;

public class NumberValue implements Value{

	@Setter
	@Getter
	double value;

	public NumberValue(double value){
		this.value = value;
	}

	@Override
	public double asNumber() {
		return value;
	}

	@Override
	public String asString() {
		// test if value has no decimal places
		return StringUtil.asString(value); //TODO: make match the lua implementation
	}

	@Override
	public boolean asBoolean() {
		return value != 0;
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.NUMBER;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NumberValue){
			return value == ((NumberValue) obj).value;
		}
		return false;
	}
}
