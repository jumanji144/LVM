package me.darknet.lua.file.function;

import me.darknet.lua.file.instructions.*;

import java.util.ArrayList;
import java.util.List;

public class LuaInstructionReader implements Opcodes {

	private LuaFunction function;

	public LuaInstructionReader(LuaFunction function) {
		this.function = function;
	}

	public List<Instruction> read() {

		List<Instruction> output = new ArrayList<>();

		int[] code = function.getCode();

		for (int i = 0; i < code.length; i++) {
			int opcode = code[i];
			int op = Opcodes.getOpcode(opcode);
			int A = Opcodes.getArgA(opcode);
			int B = Opcodes.getArgB(opcode);
			int C = Opcodes.getArgC(opcode);
			int Bx = Opcodes.getArgBx(opcode);
			int sBx = Opcodes.getArgsBx(opcode);
			Instruction inst = switch (op) {
				case LOADNIL,
						MOVE,
						GETUPVAL -> new LoadInstruction(op, A, B);
				case LOADK -> new LoadConstantInstruction(op, A, Bx, function.getConstants().get(Bx));
				case GETGLOBAL -> new GetGlobalInstruction(A, Bx, function.getConstants().get(Bx));
				case LOADBOOL -> new LoadBoolInstruction(A, B, C);
				case GETTABLE -> new GetTableInstruction(A, B, C);
				case SETGLOBAL -> new SetGlobalInstruction(A, Bx, function.getConstants().get(Bx));
				case SETUPVAL -> new SetInstruction(op, A, B);
				case SETTABLE -> new SetTableInstruction(A, B, C);
				case NEWTABLE -> new NewTableInstruction(A, B, C);
				case SELF -> new SelfInstruction(A, B, C);
				case ADD,
						SUB,
						MUL,
						DIV,
						MOD,
						POW,
						CONCAT -> new ArithmeticInstruction(op, A, B, C);
				case UNM,
						NOT,
						LEN -> new UnaryInstruction(op, A, B);
				case JMP -> new JumpInstruction(Bx);
				case EQ,
						LT,
						LE -> new CompareInstruction(op, A, B, C);
				case TEST -> new TestInstruction(A, C);
				case TESTSET -> new TestSetInstruction(A, B, C);
				case CALL,
						TAILCALL -> new CallInstruction(op, A, B, C);
				case RETURN -> new ReturnInstruction(A, B);
				case FORLOOP,
						TFORLOOP -> new ForLoopInstruction(op,
								A,
								op == FORLOOP ? Bx : C);
				case FORPREP -> new ForPrepInstruction(A, sBx);
				case SETLIST -> new SetListInstruction(A, B, C);
				case CLOSE -> new CloseInstruction(A);
				case CLOSURE -> new ClosureInstruction(A, Bx, function.getPrototype(Bx));
				default -> null;
			};
			if(inst == null) continue;
			inst.setLine(function.getLine(i));
			output.add(inst);
		}

		return output;

	}
}
