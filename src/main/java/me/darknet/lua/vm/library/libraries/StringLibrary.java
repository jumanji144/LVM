package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Libraries;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.TableValue;
import me.darknet.lua.vm.value.Value;

public class StringLibrary extends Library {

	public StringLibrary() {
		super("string");
	}

	@Override
	public Table construct() {
		Table table = super.construct();
		this.getVm().getGlobal().getMetatable().set("string", new TableValue(table));
		return table;
	}

	public int lua_len(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		ctx.push(new NumberValue(value.length()));
		return 1;
	}

	public int lua_sub(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		int start = (int) ctx.getRequired(1).asNumber();
		int end = ctx.optionalInt(2, value.length());
		ctx.push(new StringValue(value.substring(start, end)));
		return 1;
	}

	public int lua_reverse(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		StringBuilder reversed = new StringBuilder();
		for(int i = value.length() - 1; i >= 0; i--) {
			reversed.append(value.charAt(i));
		}
		ctx.push(new StringValue(reversed.toString()));
		return 1;
	}

	public int lua_lower(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		ctx.push(new StringValue(value.toLowerCase()));
		return 1;
	}

	public int lua_upper(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		ctx.push(new StringValue(value.toUpperCase()));
		return 1;
	}

	public int lua_rep(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		int count = (int) ctx.getRequired(1).asNumber();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < count; i++) {
			builder.append(value);
		}
		ctx.push(new StringValue(builder.toString()));
		return 1;
	}

	public int lua_byte(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		int start = ctx.optionalInt(1, 1);
		int end = ctx.optionalInt(2, start);
		if(start <= 0) start = 1;
		if(end > value.length()) end = value.length();
		if(start > end) return 0;
		for (int i = start - 1; i < end; i++) {
			ctx.push(new NumberValue(value.charAt(i)));
		}
		return 1;
	}

	public int lua_char(ExecutionContext ctx) {
		int count = ctx.size();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < count; i++) {
			builder.append((char) ctx.get(i).asNumber());
		}
		ctx.push(new StringValue(builder.toString()));
		return 1;
	}

}
