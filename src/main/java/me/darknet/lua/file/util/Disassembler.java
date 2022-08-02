package me.darknet.lua.file.util;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.function.LuaInstructionReader;
import me.darknet.lua.file.instructions.Instruction;

import java.io.PrintStream;
import java.util.List;

public class Disassembler {

	LuaFunction function;
	PrintStream out;

	public Disassembler(LuaFunction function, PrintStream out) {
		this.function = function;
		this.out = out;
	}

	public void disassemble() {
		out.println("Disassembly: " + function.getSource());
		disassemble(out, 0, 0);
	}

	private void disassemble(PrintStream out, int level, int index) {
		out.println(level == 0 ? "function entry " : ("function " + index + ", depth=" + level));
		out.println("slots: " + function.getMaxStackSize());
		for (int i = 0; i < function.getUpvalues().size(); i++) {
			out.println("\tupvalue [" + i + "]: " + function.getUpvalues().get(i));
		}
		for (Instruction instruction : function.getInstructions()) {
			out.print("\t");
			out.println(instruction.getLine() + ": " + instruction.print());
		}
		out.println();
		for (int i = 0; i < function.getProtos().size(); i++) {
			new Disassembler(function.getProtos().get(i), out).disassemble(out, level + 1, i);
		}
	}

}
