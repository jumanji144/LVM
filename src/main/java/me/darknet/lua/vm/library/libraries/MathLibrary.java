package me.darknet.lua.vm.library.libraries;

import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.NumberValue;

import java.util.Random;

public class MathLibrary extends Library {

	private static final Random random = new Random();

	public MathLibrary() {
		super("math", "math");
		set("pi", new NumberValue(Math.PI));
		set("huge", new NumberValue(Double.POSITIVE_INFINITY));
	}

	// Trigeometrical Functions
	public int lua_abs(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.abs(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_sin(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.sin(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_sinh(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.sinh(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_cos(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.cos(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_cosh(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.cosh(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_tan(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.tan(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_tanh(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.tanh(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_asin(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.asin(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_acos(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.acos(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_atan(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.atan(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_atan2(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.atan2(ctx.getRequired(0).asNumber(), ctx.getRequired(1).asNumber())));
		return 1;
	}

	// floating point arithmetic functions
	public int lua_ceil(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.ceil(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_floor(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.floor(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_fmod(ExecutionContext ctx) {
		ctx.push(new NumberValue(ctx.getRequired(0).asNumber() % ctx.getRequired(1).asNumber()));
		return 1;
	}

	public int lua_modf(ExecutionContext ctx) {
		double[] result = new double[2];
		double value = ctx.getRequired(0).asNumber();
		result[0] = Math.floor(value);
		result[1] = value - result[0];
		ctx.push(new NumberValue(result[0]));
		ctx.push(new NumberValue(result[1]));
		return 2;
	}

	// general math functions

	public int lua_sqrt(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.sqrt(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_pow(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.pow(ctx.getRequired(0).asNumber(), ctx.getRequired(1).asNumber())));
		return 1;
	}

	public int lua_log(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.log(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_log10(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.log10(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_exp(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.exp(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_deg(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.toDegrees(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_rad(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.toRadians(ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_frexp(ExecutionContext ctx) {
		double[] result = new double[2];
		double value = ctx.getRequired(0).asNumber();
		result[0] = Math.floor(value);
		result[1] = value - result[0];
		ctx.push(new NumberValue(result[0]));
		ctx.push(new NumberValue(result[1]));
		return 2;
	}

	public int lua_ldexp(ExecutionContext ctx) {
		ctx.push(new NumberValue(Math.pow(2, ctx.getRequired(0).asNumber())));
		return 1;
	}

	public int lua_min(ExecutionContext ctx) {
		int numArgs = ctx.getTop() - ctx.getBase();
		double min = ctx.getRequired(0).asNumber();
		for (int i = 2; i < numArgs; i++) {
			double d = ctx.getRequired(i).asNumber();
			if(d < min) {
				min = d;
			}
		}
		ctx.push(new NumberValue(min));
		return 1;
	}

	public int lua_max(ExecutionContext ctx) {
		int numArgs = ctx.getTop() - ctx.getBase();
		double max = ctx.getRequired(0).asNumber();
		for (int i = 2; i < numArgs; i++) {
			double d = ctx.getRequired(i).asNumber();
			if(d > max) {
				max = d;
			}
		}
		ctx.push(new NumberValue(max));
		return 1;
	}

	public int lua_random(ExecutionContext ctx) {
		double number = Math.random();
		switch (ctx.size()) {
			case 0 -> { // no arguments
				ctx.push(new NumberValue(number));
				return 1;
			}
			case 1 -> { // upper limit
				int upper = (int) ctx.getRequired(0).asNumber();
				if (1 <= upper) ctx.throwError("bad argument 1 interval is empty");
				ctx.push(new NumberValue(Math.floor(number * upper) + 1));
				return 1;
			}
			case 2 -> { // lower and upper limit
				int lower = (int) ctx.getRequired(0).asNumber();
				int upper = (int) ctx.getRequired(1).asNumber();
				if (lower > upper) ctx.throwError("bad argument 1 interval is empty");
				ctx.push(new NumberValue(Math.floor(number * (upper - lower + 1)) + lower));
				return 1;
			}
			default -> {
				ctx.throwError("wrong number of arguments");
				return 0;
			}
		}
	}

	public int lua_randomseed(ExecutionContext ctx) {
		random.setSeed((long) ctx.getRequired(0).asNumber());
		return 0;
	}



}
