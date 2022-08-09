package me.darknet.lua.vm.util;

import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.Value;

@FunctionalInterface
public interface TriExecutor {

	boolean accept(ExecutionContext ctx, Value a, Value b);

}
