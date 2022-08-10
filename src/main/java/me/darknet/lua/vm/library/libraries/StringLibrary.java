package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.NumberValue;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.TableValue;

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
		for (int i = value.length() - 1; i >= 0; i--) {
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
		for (int i = 0; i < count; i++) {
			builder.append(value);
		}
		ctx.push(new StringValue(builder.toString()));
		return 1;
	}

	public int lua_byte(ExecutionContext ctx) {
		String value = ctx.getRequired(0).asString();
		int start = ctx.optionalInt(1, 1);
		int end = ctx.optionalInt(2, start);
		if (start <= 0) start = 1;
		if (end > value.length()) end = value.length();
		if (start > end) return 0;
		for (int i = start - 1; i < end; i++) {
			ctx.push(new NumberValue(value.charAt(i)));
		}
		return 1;
	}

	public int lua_char(ExecutionContext ctx) {
		int count = ctx.size();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append((char) ctx.get(i).asNumber());
		}
		ctx.push(new StringValue(builder.toString()));
		return 1;
	}

	public boolean isFormatModifier(char c) {
		return c == '-' || c == '#' || c == '0' || c == ' ' || c == '+';
	}

	public void sprintf(StringBuilder buffer, String fmt, Object... args) {
		buffer.append(String.format(fmt, args));
	}

	public String qoute(String value) {
		char[] chars = value.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (char c : chars) {
			builder.append(switch (c) {
				case '"', '\\', '\n' -> ('\\' + c);
				case 'r' -> "\\r";
				case '\0' -> "\\000";
				default -> c;
			});
		}
		return builder.toString();
	}

	public int lua_format(ExecutionContext ctx) {
		String format = ctx.getRequired(0).asString();
		int argSize = ctx.size();
		int arg = 0;
		StringBuilder output = new StringBuilder();
		char[] chars = format.toCharArray();
		int i = 0;
		while (i < chars.length) {
			if (chars[i] != '%') output.append(chars[i++]);
			else if (chars[++i] == '%') output.append(chars[i++]);
			else {
				if (++arg > argSize) ctx.throwError("bad argument #" + arg + " to 'format' (too few arguments)");
				// parse format
				String fmt = "%";
				int formBegin = i;
				while (i < chars.length && isFormatModifier(chars[i])) i++;
				char c = chars[i];
				if (Character.isDigit(c)) c = chars[++i];
				if (Character.isDigit(c)) c = chars[++i]; // at most 2 width digits
				if (c == '.') { // precision
					c = chars[++i];
					if (Character.isDigit(c)) c = chars[++i];
					if (Character.isDigit(c)) c = chars[++i]; // at most 2 precision digits
				}
				if (Character.isDigit(c)) ctx.throwError("invalid format (width or precision too long)");
				for (int j = formBegin; j < i + 1; j++) {
					fmt += chars[j];
				}
				switch (chars[i++]) {
					case 'c' -> sprintf(output, fmt, (char) ctx.get(arg).asNumber());
					case 'd', 'i' -> sprintf(output, fmt, (long) ctx.get(arg).asNumber());
					case 'o', 'u', 'x', 'X' -> sprintf(output, fmt, (long) ctx.get(arg).asNumber());
					case 'e', 'E', 'f', 'g', 'G' -> sprintf(output, fmt, ctx.get(arg).asNumber());
					case 'q' -> sprintf(output, fmt, qoute(ctx.get(arg).asString()));
					case 's' -> sprintf(output, fmt, ctx.get(arg).asString());
					default -> ctx.throwError("invalid option '" + chars[i - 1] + "' to 'format'");
				}
			}
		}
		ctx.push(new StringValue(output.toString()));
		return 1;
	}

}
