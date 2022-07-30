package me.darknet.lua.vm;

import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.constant.StringConstant;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.*;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.execution.executors.*;
import me.darknet.lua.vm.value.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Opcodes {

	private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

	Map<Integer, Executor<? extends Instruction>> executors = new HashMap<>();

	public Interpreter() {
		installAll();
	}

	void install(int inst, Executor<? extends Instruction> executor) {
		executors.put(inst, executor);
	}

	void installAll() {
		install(MOVE, (Executor<LoadInstruction>) (inst, ctx) -> ctx.set(inst.getRegister(), ctx.get(inst.getTarget())));
		install(GETGLOBAL, (Executor<LoadInstruction>) (inst, ctx) -> {
			StringConstant constant = (StringConstant) ctx.getCurrentFunction().getConstants().get(inst.getTarget());
			ctx.set(inst.getRegister(), ctx.getEnv().get(constant.getValue()));
		});
		install(SETGLOBAL, (Executor<SetGlobalInstruction>) (inst, ctx) -> {
			StringConstant constant = (StringConstant) inst.getConstant();
			ctx.getEnv().set(constant.getValue(), ctx.get(inst.getRegister()));
		});
		install(LOADK, new LoadConstantExecutor());
		install(LOADNIL, (Executor<LoadInstruction>) (inst, ctx) -> ctx.set(inst.getRegister(), NilValue.NIL));
		install(ADD, new ArithExecutor((a, b) -> new NumberValue(a.asNumber() + b.asNumber())));
		install(SUB, new ArithExecutor((a, b) -> new NumberValue(a.asNumber() - b.asNumber())));
		install(MUL, new ArithExecutor((a, b) -> new NumberValue(a.asNumber() * b.asNumber())));
		install(DIV, new ArithExecutor((a, b) -> new NumberValue(a.asNumber() / b.asNumber())));
		install(MOD, new ArithExecutor((a, b) -> new NumberValue(a.asNumber() % b.asNumber())));
		install(POW, new ArithExecutor((a, b) -> new NumberValue(Math.pow(a.asNumber(), b.asNumber()))));
		install(UNM, new ArithExecutor((a, b) -> new NumberValue(-a.asNumber())));
		install(NOT, new ArithExecutor((a, b) -> new BooleanValue(!a.asBoolean())));
		install(LEN, new LenExecutor());
		install(CONCAT, new ConcatExecutor());
		install(CALL, new CallExecutor());
		install(RETURN, new ReturnExecutor());
		install(CLOSURE, (Executor<ClosureInstruction>) (inst, ctx) -> ctx.set(
				inst.getRegister(),
				new ClosureValue(new Closure(inst.getFunction(), ctx.getEnv())) // inherit env from current function
		));
		install(VARARG, new VarArgExecutor());
	}

	public void execute(ExecutionContext ctx, LuaFunction fn) {
		List<Instruction> instructions = fn.getInstructions();

		while(true) {
			int pc = ctx.getPc();
			if (pc < 0 || pc >= instructions.size()) {
				logger.debug("pc out of bounds: {}", pc);
				logger.debug("frame: {" + ctx.viewFrame("\t") + "}");
				throw new VMException("Execution impossible", "pc out of bounds");
			}

			Instruction instruction = instructions.get(pc);
			// check if there is an executor for this instruction
			Executor executor = executors.get(instruction.getOpcode());
			if (executor == null) {
				logger.error("no executor for instruction: {}, insn=[{}], func={}", OPCODES[instruction.getOpcode()], instruction, fn);
				throw new VMException("Execution impossible", "no executor for instruction: " + instruction.getOpcode());
			}
			try {
				// execute the instruction
				executor.execute(instruction, ctx);
			} catch (Exception e) {
				logger.debug("frame: {" + ctx.viewFrame("\t") + "}");
				throw e;
			}

			if(ctx.isReturning()) return;

			// increment the pc
			ctx.setPc(pc + 1);
		}

	}

}
