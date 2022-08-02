package me.darknet.lua.file.instructions;

import me.darknet.lua.file.constant.Constant;

import java.util.Optional;

/**
 * Argument is an integer that can also be a Constant.
 */
public class Argument {

	int value;
	Optional<Constant> constant = Optional.empty();

	public Argument(int value) {
		this.value = value;
	}

	public Argument(Constant constant) {
		this.constant = Optional.of(constant);
	}

	public int getValue() {
		return value;
	}

	public Constant getConstant() {
		return constant.orElse(null);
	}

	public boolean isConstant() {
		return constant.isPresent();
	}

}
