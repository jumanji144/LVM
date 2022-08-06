package me.darknet.lua.vm.value;

import lombok.Getter;
import me.darknet.lua.vm.data.UserData;

public class UserDataValue implements Value{

	@Getter
	UserData value;

	public UserDataValue(UserData value) {
		this.value = value;
	}

	@Override
	public double asNumber() {
		throw new UnsupportedOperationException("Cannot convert userdata to number");
	}

	@Override
	public String asString() {
		return Integer.toHexString(value.hashCode());
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
		return Type.USERDATA;
	}
}
