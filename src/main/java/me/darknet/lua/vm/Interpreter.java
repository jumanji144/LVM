package me.darknet.lua.vm;

import me.darknet.lua.file.constant.StringConstant;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.*;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.error.Error;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.execution.executors.*;
import me.darknet.lua.vm.value.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
		install(GETGLOBAL, (Executor<LoadConstantInstruction>) (inst, ctx) -> {
			StringConstant constant = (StringConstant) inst.getConstant();
			ctx.set(inst.getRegister(), ctx.getEnv().get(constant.getValue()));
		});
		install(SETGLOBAL, (Executor<SetGlobalInstruction>) (inst, ctx) -> {
			StringConstant constant = (StringConstant) inst.getConstant();
			ctx.getEnv().set(constant.getValue(), ctx.get(inst.getRegister()));
		});
		install(LOADK, new LoadConstantExecutor());
		install(LOADBOOL, (Executor<LoadBoolInstruction>) (inst, ctx) -> {
			if(inst.getCondition() != 0) ctx.setPc(ctx.getPc() + 1);
			ctx.set(inst.getRegister(), new BooleanValue(inst.getValue()));
		});
		install(LOADNIL, (Executor<LoadInstruction>) (inst, ctx) -> ctx.set(inst.getRegister(), NilValue.NIL));
		install(NEWTABLE, (Executor<NewTableInstruction>) (inst, ctx) -> ctx.set(inst.getRegister(), new TableValue(new Table())));
		install(GETTABLE, new GetTableExecutor());
		install(SETTABLE, new SetTableExecutor());
		install(SELF, new SelfExecutor());
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
		install(JMP, (Executor<JumpInstruction>) (inst, ctx) -> ctx.setPc(ctx.getPc() + inst.getOffset() + 1));
		install(EQ, new CompExecutor(Double::equals));
		install(LT, new CompExecutor((a, b) -> a < b));
		install(LE, new CompExecutor((a, b) -> a <= b));
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
				throw new IllegalStateException("pc out of bounds");
			}

			Instruction instruction = instructions.get(pc);
			// check if there is an executor for this instruction
			Executor executor = executors.get(instruction.getOpcode());
			if (executor == null) {
				throw new IllegalStateException("no executor for instruction: " + OPCODES[instruction.getOpcode()]);
			}
			try {
				// execute the instruction
				executor.execute(instruction, ctx);
			} catch (VMException e) {
				if(ctx.getError() != null) {
					if(ctx.getErrorHandler() != null) {
						Error error = ctx.getError();
						int ret = ctx.getHelper().invoke(ctx, ctx.getErrorHandler(), 1, new StringValue(error.print()));
						ctx.setErrorHandlerReturn(ctx.getRaw(ret));
						return;
					}
					if(ctx.isReturnOnError()) {
						return;
					}
				}
				throw e;
			} catch (Exception e) {
				throw e;
			}

			if(ctx.isReturning()) return;

			// increment the pc
			ctx.setPc(ctx.getPc() + 1);
		}

	}

}
