package me.darknet.lua.file.function;

import me.darknet.lua.file.LuaFile;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.*;

import java.util.ArrayList;
import java.util.List;

public class InstructionReader implements Opcodes {

	private LuaFunction function;

	public InstructionReader(LuaFunction function) {
		this.function = function;
	}

	public List<Instruction> read() {

		List<Instruction> output = new ArrayList<>();

		int[] code = function.getCode();

		for (int i = 0; i < code.length; i++) {
			int opcode = code[i];
			int op = Opcodes.getOpcode(opcode);
			Instruction inst = switch (op) {
				case LOADNIL,
						MOVE,
						GETUPVAL -> new LoadInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgB(opcode));
				case LOADK,
						GETGLOBAL -> new LoadInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgBx(opcode));
				case LOADBOOL -> new LoadBoolInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case GETTABLE -> new GetTableInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case SETGLOBAL,
						SETUPVAL -> new SetInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgB(opcode));
				case SETTABLE -> new SetTableInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case NEWTABLE -> new NewTableInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case SELF -> new SelfInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case ADD,
						SUB,
						MUL,
						DIV,
						MOD,
						POW,
						CONCAT -> new ArithmeticInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case UNM,
						NOT,
						LEN -> new UnaryInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgB(opcode));
				case JMP -> new JumpInstruction(Opcodes.getArgsBx(opcode));
				case EQ,
						LT,
						LE -> new CompareInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case TEST -> new TestInstruction(Opcodes.getArgA(opcode), Opcodes.getArgC(opcode));
				case TESTSET -> new TestSetInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case CALL,
						TAILCALL -> new CallInstruction(op, Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case RETURN -> new ReturnInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode));
				case FORLOOP,
						TFORLOOP -> new ForLoopInstruction(op,
								Opcodes.getArgA(opcode),
								opcode == FORLOOP ? Opcodes.getArgsBx(opcode) : Opcodes.getArgC(opcode));
				case FORPREP -> new ForPrepInstruction(Opcodes.getArgA(opcode), Opcodes.getArgsBx(opcode));
				case SETLIST -> new SetListInstruction(Opcodes.getArgA(opcode), Opcodes.getArgB(opcode), Opcodes.getArgC(opcode));
				case CLOSE -> new CloseInstruction(Opcodes.getArgA(opcode));
				case CLOSURE -> new ClosureInstruction(Opcodes.getArgA(opcode), Opcodes.getArgBx(opcode));
				default -> null;
			};
			if(inst == null) continue;
			inst.setLine(function.getLine(i));
			output.add(inst);
		}

		return output;

	}

}
