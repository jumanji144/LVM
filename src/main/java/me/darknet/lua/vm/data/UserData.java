package me.darknet.lua.vm.data;

import lombok.Data;

@Data
public class UserData {

	Object value;
	Table metatable;
	Table env;

}
