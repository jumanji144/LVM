package me.darknet.lua.vm.execution;

import me.darknet.lua.file.instructions.Instruction;

public interface Executor<T extends Instruction> {

	void execute(T inst, ExecutionContext ctx);

}
