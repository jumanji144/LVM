package me.darknet.lua.vm.data;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.value.NilValue;
import me.darknet.lua.vm.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lua table
 */
@Getter
public class Table {

	Map<String, Value> table = new HashMap<>();
	Map<Integer, Value> array = new HashMap<>();

	@Setter
	Table metatable;

	public void insert(int index, Value value) {
		// insert value into array part
		array.put(index, value);
	}

	public void insert(Value value) {
		array.put(array.size(), value);
	}

	public void remove(int index) {
		array.remove(index);
	}

	public void remove() {
		array.remove(array.size() - 1);
	}

	public String concat(String delm, int from, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < end; i++) {
			sb.append(array.get(i).asString());
			sb.append(delm);
		}
		return sb.toString();
	}

	public void set(String key, Value value) {
		table.put(key, value);
	}

	public void set(int index, Value value) {
		array.put(index, value);
	}

	public Value get(String value) {
		Value v = table.get(value);
		return v == null ? NilValue.NIL : v;
	}

	public Value get(int index) {
		return array.getOrDefault(index, NilValue.NIL);
	}

	public boolean has(String key) {
		return table.containsKey(key);
	}

	public void merge(Table other) {
		// copy all values from this table into other
		table.forEach(other::set);
		array.forEach(other::insert);
	}

	public boolean hasMetatable() {
		return metatable != null;
	}

	public boolean hasMetaobject(String key) {
		return metatable != null && metatable.has(key);
	}

	public Value getMetaobject(String key) {
		return metatable != null ? metatable.get(key) : NilValue.NIL;
	}

	public Value arrayGet(int i) {
		return array.getOrDefault(i, NilValue.NIL);
	}
}
