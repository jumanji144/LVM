package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.instructions.CallInstruction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.*;

public class CallExecutor implements Executor<CallInstruction> {

	@Override
	public void execute(CallInstruction inst, ExecutionContext ctx) {

		ClosureValue closure = (ClosureValue) ctx.get(inst.getRegister());
		Closure cl = closure.getClosure();

		int register = ctx.getBase() + inst.getRegister();
		if(inst.getNumArgs() != 0) ctx.setTop(register + inst.getNumArgs());

		ExecutionContext newCtx = ctx.getHelper().prepareCtx(ctx, cl, register, inst.getNumReturns() - 1);

		ctx.getHelper().invoke(newCtx);

		ctx.setStack(newCtx.getStack());
		if(inst.getNumReturns() == 0) ctx.setTop(newCtx.getTop());

	}
}
