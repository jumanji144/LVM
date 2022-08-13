package me.darknet.lua.vm;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.library.Libraries;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.StringValue;
import me.darknet.lua.vm.value.TableValue;

@Getter
public class VM {

	public static final long VM_BOOT_TIME = System.nanoTime();

	VMHelper helper;
	Interpreter interpreter;
	Libraries libraries;
	Table global;
	@Setter
	String[] programArguments = new String[0];

	public VM() {
		interpreter = new Interpreter();
		helper = new VMHelper(interpreter, this);
		libraries = new Libraries(this);
		global = new Table();
		global.setMetatable(new Table());
	}

	public void initialize() {
		loadLibraries();
	}

	public Table getArgTable() {
		Table argTable = new Table();
		for (int i = 0; i < programArguments.length; i++) {
			argTable.set(i, new StringValue(programArguments[i]));
		}
		return argTable;
	}

	/**
	 * Load global and libraries
	 */
	private void loadLibraries() {
		libraries.initialize();
		Library top = libraries.getTop(); // top level library (aka base)
		Table table = top.construct();
		table.set("_G", new TableValue(table));
		table.set("arg", new TableValue(getArgTable()));
		// merge table into global
		table.merge(global);
		libraries.getLibraries().forEach((name, library) -> global.set(name, new TableValue(library.construct())));
	}

}
