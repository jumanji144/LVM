package me.darknet.lua.vm.data;

import me.darknet.lua.vm.value.Value;

public class Stack {

	Value[] backing;
	int sp;

	public Stack(int size) {
		backing = new Value[size];
		sp = 0;
	}

	public void push(Value value) {
		backing[sp++] = value;
	}

	public Value pop() {
		return backing[--sp];
	}

	public Value peek() {
		return backing[sp - 1];
	}

	public String view() {
		StringBuilder sb = new StringBuilder();
		if (sp < 0) {
			return "[UNDERFLOW]";
		}
		if (sp > backing.length) {
			return "[OVERFLOW]";
		}
		sb.append("{");
		for (int i = 0; i < sp; i++) {
			sb.append(backing[i]);
			if (i < sp - 1) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}


}
