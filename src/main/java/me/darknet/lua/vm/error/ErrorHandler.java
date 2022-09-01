package me.darknet.lua.vm.error;

import me.darknet.lua.file.constant.StringConstant;
import me.darknet.lua.file.function.Local;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.*;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.Value;

import java.util.List;

public class ErrorHandler implements Opcodes{

	/**
	 * Symbolically reconstruct which instruction contributed to the creation of a register / lead to the instruction
	 * being executed
	 * @param fn function from there the instruction originates
	 * @param lastPc last instruction in the chain
	 * @param reg register affected
	 * @return instruction pc of last contributing instruction
	 */
	public static int symbolicExecution(LuaFunction fn, int lastPc, int reg) {
		int[] code = fn.getCode();
		int last = code.length - 1;
		for (int pc = 0; pc < lastPc; pc++) {
			int inst = code[pc];
			int opcode = Opcodes.getArgA(inst);
			int a = Opcodes.getArgA(inst);
			int b = 0;
			int c = 0;
			// define b and c based on argument modes
			switch (Opcodes.getOpMode(opcode)) {
				case iABC -> { // b and c are just normal args
					b = Opcodes.getArgB(inst);
					c = Opcodes.getArgC(inst);
				}
				case iABx -> b = Opcodes.getArgBx(inst); // c doesn't exist and b is Bx
				case iAsBx -> b = Opcodes.getArgsBx(inst); // c doesn't exist and b is sBx
			}
			if(Opcodes.isAMode(opcode)) if(a == reg) last = pc;
			// check opcode and mostly validate out invalid opcodes
			switch (opcode) {
				case LOADBOOL -> {
					if(c == 1) if(pc + 2 >= code.length) return 0;
				}
				case LOADNIL -> {
					if(a <= reg && reg <= b) last = pc; // load nil set this
				}
				case SELF -> {
					if(reg == a+1) last = pc; /* self is the table, so it was this instruction */
				}
				case TFORLOOP -> {
					if(reg >= a+2) last = pc; // TFORLOOP copies the value a before to reg
				}
				case FORLOOP, FORPREP, JMP -> {
					int dest = pc + 1 + b; // jump destination
					// is this jump even valid?
					if(reg != MAXARG_A && pc < dest && dest <= lastPc) {
						// yes? do it
						pc += b;
					}
				}
				case CALL, TAILCALL -> {
					if(reg >= a) last = pc; // this call affected all calls above base
				}
				case CLOSURE -> {
					if(reg != MAXARG_A) pc += fn.getUpvalues().size(); // skip all the pseudo instructions for upvals
				}
			}
		}
		return last;
	}

	/**
	 * Attempt to get a name for an object at {@code stackPosition}
	 * @param ctx the referencing context
	 * @param cl the closure where the object originates
	 * @param stackPosition where the object lives in the stack
	 * @return {@link ObjInfo} containing the information of the object
	 */
	public static ObjInfo getObjName(ExecutionContext ctx, Closure cl, int stackPosition) {

		LuaFunction fn = cl.getLuaFunction();
		int pc = ctx.getPc();

		String kind = "";
		String name = tryGetLocalName(fn, stackPosition + 1, pc);
		if(name != null) kind = "local";
		Instruction inst = fn.getInstructions().get(symbolicExecution(fn, pc, stackPosition));
		if(inst instanceof GetGlobalInstruction glb) {
			name = ((StringConstant)glb.getConstant()).getValue();
			kind = "global";
		} else if(inst instanceof MoveInstruction mv) {
			int a = mv.getRegister();
			int b = mv.getFrom();
			if(b < a) return getObjName(ctx, cl, b); // name for be because we are referring to 'b'
			return new ObjInfo(name, null);
		} else if(inst instanceof GetTableInstruction gt) {
			name = gt.getIndex().isConstant() ? ((StringConstant)gt.getIndex().getConstant()).getValue() : "?";
			kind = "field";
		} else if(inst instanceof GetUpvalueInstruction gu) {
			int upIndex = gu.getUpvalue();
			name = fn.getUpvalues().size() > 0 ? fn.getUpvalues().get(upIndex).getName() : "?";
			kind = "upvalue";
		} else if(inst instanceof SelfInstruction sf) {
			name = sf.getIndex().isConstant() ? ((StringConstant)sf.getIndex().getConstant()).getValue() : "?";
			kind = "method";
		}
		return new ObjInfo(kind, name);
	}

	/**
	 * Throw a type error for an object at {@code stackIndex}
	 * @param ctx the referencing context
	 * @param stackIndex the stack index where the object lives
	 * @param operation the operation that failed
	 */
	public static void throwTypeError(ExecutionContext ctx, int stackIndex, String operation) {
		Value v = ctx.get(stackIndex);
		String typeName = v.getType().getName();
		ObjInfo info = getObjName(ctx, ctx.getClosure(), stackIndex);
		if(info.type != null) {
			ctx.throwError("attempt to %s %s '%s'" + " (a %s value)", operation, info.type, info.name, typeName);
		} else {
			ctx.throwError("attempt to %s a %s value", operation, typeName);
		}
	}

	/**
	 * Attempt to get the local name of a local with the index {@code localNumber}
	 * @param fn the referencing function
	 * @param localNumber the local number
	 * @param pc the pc where this is requested
	 * @return the name of the local or {@code null} if no name was found
	 */
	public static String tryGetLocalName(LuaFunction fn, int localNumber, int pc) {
		List<Local> locals = fn.getLocals();
		Local local;
		for (int i = 0; i < locals.size() && (local = locals.get(i)).getStartpc() <= pc; i++) {
			if(pc < local.getEndpc()) { // variable is active
				localNumber--;
				if(localNumber == 0) return local.getVarname();
			}
		}
		return null;
	}

	public record ObjInfo(String type, String name) {}

}
