package me.darknet.lua.vm.value;

import lombok.Getter;
import lombok.Setter;

public class StringValue implements Value{

	@Getter
	@Setter
	private String value;

	public StringValue(String value){
		this.value = value;
	}

	@Override
	public double asNumber() {
		return Double.parseDouble(value);
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public boolean asBoolean() {
		return value.length() > 0;
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.STRING;
	}

}

