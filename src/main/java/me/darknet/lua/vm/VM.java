package me.darknet.lua.vm;

import lombok.Getter;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.library.Libraries;
import me.darknet.lua.vm.library.Library;
import me.darknet.lua.vm.value.TableValue;

@Getter
public class VM {

	VMHelper helper;
	Interpreter interpreter;
	Libraries libraries;
	Table global;

	public VM() {
		interpreter = new Interpreter();
		helper = new VMHelper(interpreter, this);
		libraries = new Libraries();
		global = new Table();
	}

	public void initialize() {
		loadLibraries();
	}

	/**
	 * Load global and libraries
	 */
	private void loadLibraries() {
		libraries.initialize();
		Library top = libraries.getTop(); // top level library (aka base)
		Table table = top.construct();
		table.set("_G", new TableValue(table));
		// merge table into global
		table.merge(global);
		libraries.getLibraries().forEach((name, library) -> global.set(name, new TableValue(library.construct())));
	}

}
