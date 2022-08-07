package me.darknet.lua.vm.library;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.library.libraries.BaseLibrary;
import me.darknet.lua.vm.library.libraries.IoLibrary;
import me.darknet.lua.vm.library.libraries.MathLibrary;
import me.darknet.lua.vm.library.libraries.StringLibrary;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Libraries {

	public final Map<String, Library> libraries = new HashMap<>();
	public Library top; // top level library
	public VM vm;

	public Libraries(VM vm) {
		this.vm = vm;
	}

	public void put(Library library) {
		library.setVm(vm);
		libraries.put(library.getName(), library);
	}

	public Library get(String name) {
		return libraries.get(name);
	}

	public void remove(String name) {
		libraries.remove(name);
	}

	public void initialize() {
		//set top level library
		BaseLibrary base = new BaseLibrary();
		setTop(base);
		put(new MathLibrary());
		put(new IoLibrary());
		put(new StringLibrary());
		// collect all libraries
		base.collect();
		libraries.values().forEach(Library::collect);
	}

}
