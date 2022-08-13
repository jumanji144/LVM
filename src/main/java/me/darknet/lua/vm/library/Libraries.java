package me.darknet.lua.vm.library;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.library.libraries.*;

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
		put(new OsLibrary());
		// collect all libraries
		base.collect();
		libraries.values().forEach(Library::collect);
	}

}
