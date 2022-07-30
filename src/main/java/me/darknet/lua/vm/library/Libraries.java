package me.darknet.lua.vm.library;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.vm.library.libraries.BaseLibrary;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Libraries {

	public final Map<String, Library> libraries = new HashMap<>();
	public Library top; // top level library

	public void put(Library library) {
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
		// collect all libraries
		base.collect();
		libraries.values().forEach(Library::collect);
	}

}
