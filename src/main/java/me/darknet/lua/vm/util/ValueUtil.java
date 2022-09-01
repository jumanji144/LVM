package me.darknet.lua.vm.util;

import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.value.*;

public class ValueUtil {

	public static Value clone(Value value) {
		return switch (value.getType()) {
			case NONE, NIL -> value; // nil and none do not feature values
			case NUMBER -> new NumberValue(value.asNumber());
			case STRING -> new StringValue(value.asString());
			case BOOLEAN -> new BooleanValue(value.asBoolean());
			case FUNCTION -> new ClosureValue(((ClosureValue) value).getClosure());
			case TABLE -> new TableValue(((TableValue) value).getTable());
			case USERDATA -> new UserDataValue(((UserDataValue) value).getValue());
			case THREAD -> new ThreadValue(((ThreadValue) value).getContext());
		};
	}

}
