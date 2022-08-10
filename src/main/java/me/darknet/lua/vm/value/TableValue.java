package me.darknet.lua.vm.value;

import lombok.Getter;
import me.darknet.lua.vm.data.Table;

@Getter
public class TableValue implements Value {

	Table table;

	public TableValue(Table table) {
		this.table = table;
	}

	@Override
	public double asNumber() {
		throw new UnsupportedOperationException("TableValue cannot be converted to a number");
	}

	@Override
	public String asString() {
		return "table " + Integer.toHexString(table.hashCode());
	}

	@Override
	public boolean asBoolean() {
		return true;
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.TABLE;
	}
}
