package me.darknet.lua.vm.execution.executors;

import me.darknet.lua.file.instructions.VarArgInstruction;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.execution.Executor;
import me.darknet.lua.vm.value.NilValue;

public class VarArgExecutor implements Executor<VarArgInstruction> {
	@Override
	public void execute(VarArgInstruction inst, ExecutionContext ctx) {

		int register = ctx.getBase() + inst.getRegister();
		int limit = inst.getLimit() - 1;
		Closure closure = ctx.getClosure();
		// numVarargs =
		// 			(ctx.base - caller.functionReturn) // end of function arguments
		//			- (closure.numParams - 1) // end of function parameters
		// 			leftover arguments must be varargs
		int numVarargs = (ctx.getBase() - ctx.getFunctionReturn()) - closure.getLuaFunction().getNumParams() - 1;
		if(limit == -1) {
			ctx.ensureSize(ctx.getTop() + numVarargs); // ensure space for all varargs
			limit = numVarargs; // the limit is the number of varargs
			ctx.setTop(register + numVarargs); // update top to be new varargs
		}
		// varargs get copied from the caller's stack to the callee's stack
		for(int i = 0; i < limit; i++) {
			if(i < numVarargs) { // as long as we have enough varargs to fill the limit
				int offset = ctx.getBase() - numVarargs + i; // the offset is the base - the number of varargs + i offset
				ctx.setRaw(register + i, ctx.getRaw(offset));
			} else { // else copy nil
				ctx.setRaw(register + i, NilValue.NIL);
			}
		}
	}
}
