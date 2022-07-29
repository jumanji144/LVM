package me.darknet.lua.file.function;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LuaFunction {

	private String source; // debug data
	private int lineDefined;
	private int lastLineDefined;
	private int numUps;
	private int numParams;
	private int isVararg;
	private int maxStackSize;
	private int[] code;
	private List<Constant> constants;
	private List<LuaFunction> protos = new ArrayList<>();
	private List<Upvalue> upvalues = new ArrayList<>();
	// DEBUG
	private List<Integer> lines = new ArrayList<>();
	private List<Local> locals = new ArrayList<>();
	private List<Instruction> instructions;

	public void addPrototype(LuaFunction readFunction) {
		protos.add(readFunction);
	}

	public void addUpvalue(Upvalue upvalue) {
		upvalues.add(upvalue);
	}

	public void addLocal(Local local) {
		locals.add(local);
	}

	public void addLine(int line) {
		lines.add(line);
	}

	public LuaFunction getPrototype(int index) {
		return protos.get(index);
	}

	public Upvalue getUpvalue(int index) {
		return upvalues.get(index);
	}

	public Local getLocal(int index) {
		return locals.get(index);
	}

	public int getLine(int index) {
		return lines.get(index);
	}

	public boolean isVararg() {
		return isVararg > 0;
	}
}
