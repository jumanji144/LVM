package me.darknet.lua.vm.execution;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.value.Value;

@Getter
@Setter
public class ExecutionContext {

	Value[] registers;
	int pc;
	LuaFunction currentFunction;
	Closure currentClosure;
	boolean returning;
	Value[] returnValues = new Value[0];
	Value[] varargs = new Value[0];
	VM vm;

	public ExecutionContext(VM vm, int registerCount) {
		this.vm = vm;
		registers = new Value[registerCount];
	}

	public String viewFrame(String indent) {
		StringBuilder sb = new StringBuilder();
		// registers view
		sb.append('\n').append(indent).append("registers: \n");
		for(int i = 0; i < registers.length; i++) {
			sb.append(indent).append("\tR").append(i).append(": ").append(registers[i]).append("\n");
		}
		// pc view
		sb.append(indent).append("pc: ").append(pc).append("\n");
		if(varargs.length > 0) {
			sb.append(indent).append("varargs: \n");
			for(int i = 0; i < varargs.length; i++) {
				sb.append(indent).append("\t").append(i).append(": ").append(varargs[i]).append("\n");
			}
		}
		return sb.toString();
	}

	public void set(int register, Value value) {
		registers[register] = value;
	}

	public Value get(int register) {
		return registers[register];
	}
	public Table getEnv() {
		return currentClosure.getEnv();
	}

	public VM getVM() {
		return vm;
	}
}
