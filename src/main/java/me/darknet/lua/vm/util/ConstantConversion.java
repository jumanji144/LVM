package me.darknet.lua.vm.util;

import me.darknet.lua.file.constant.BooleanConstant;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.constant.NumberConstant;
import me.darknet.lua.file.constant.StringConstant;
import me.darknet.lua.vm.value.BooleanValue;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.Value;

import static me.darknet.lua.file.constant.ConstantTypes.*;

public class ConstantConversion {

	public static Value toValue(Constant constant) {
		switch (constant.getType()) {
			case TBOOLEAN:
				return new BooleanValue(((BooleanConstant) constant).getValue());
			case TNUMBER:
				return new NumberValue(((NumberConstant) constant).getValue());
			case TSTRING:
				return new StringValue(((StringConstant) constant).getValue());
			default:
				throw new IllegalArgumentException("Unsupported constant type: " + constant.getType());
		}
	}

}
