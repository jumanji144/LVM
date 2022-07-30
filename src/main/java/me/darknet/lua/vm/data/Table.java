package me.darknet.lua.vm.data;

import me.darknet.lua.vm.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lua table
 */
public class Table {

	Map<String, Value> table = new HashMap<>();
	List<Value> array = new ArrayList<>();

	Table metatable;

	public void insert(int index, Value value) {
		// insert value into array part
		array.add(index, value);
	}

	public void insert(Value value) {
		array.add(value);
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

	public Value get(String value) {
		return table.get(value);
	}

	public void merge(Table other) {
		// copy all values from this table into other
		table.forEach(other::set);
		array.forEach(other::insert);
	}
}
