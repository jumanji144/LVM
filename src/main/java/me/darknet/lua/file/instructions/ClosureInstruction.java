package me.darknet.lua.file.instructions;

import lombok.Getter;
import me.darknet.lua.file.function.LuaFunction;

@Getter
public class ClosureInstruction extends Instruction {
	int register;
	int proto;
	LuaFunction function;

	public ClosureInstruction(int register, int proto, LuaFunction function) {
		super(CLOSURE);
		this.register = register;
		this.proto = proto;
		this.function = function;
	}

	@Override
	public String print() {
		return getOpcodeName() + " R(" + getRegister() + ") <- func(" + getProto() + ")";
	}
}
