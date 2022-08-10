package me.darknet.lua.vm.util;

import me.darknet.lua.file.constant.BooleanConstant;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.constant.NumberConstant;
import me.darknet.lua.file.constant.StringConstant;
import me.darknet.lua.vm.value.*;

import static me.darknet.lua.file.constant.ConstantTypes.*;

public class ConstantConversion {

	public static Value toValue(Constant constant) {
		return switch (constant.getType()) {
			case TNIL -> NilValue.NIL;
			case TBOOLEAN -> BooleanValue.valueOf(((BooleanConstant) constant).getValue());
			case TNUMBER -> new NumberValue(((NumberConstant) constant).getValue());
			case TSTRING -> new StringValue(((StringConstant) constant).getValue());
			default -> throw new IllegalArgumentException("Unsupported constant type: " + constant.getType());
		};
	}

}
