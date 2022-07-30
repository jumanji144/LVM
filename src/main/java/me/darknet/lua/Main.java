package me.darknet.lua;

import me.darknet.lua.file.LuaDataStream;
import me.darknet.lua.file.LuaFile;
import me.darknet.lua.file.LuaFileReader;
import me.darknet.lua.file.util.Disassembler;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.execution.ExecutionContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\JustusGarbe\\Documents\\sandbox\\luac.out");
		LuaDataStream luaStream = new LuaDataStream(new FileInputStream(file));
		LuaFileReader reader = new LuaFileReader(luaStream);
		LuaFile luaFile = reader.read();
		Disassembler disassembler = new Disassembler(luaFile.getFunction(), System.out);
		disassembler.disassemble();
		VM vm = new VM();
		vm.initialize();
		System.out.println("Executing... ");
		ExecutionContext result = vm.getHelper().invoke(luaFile.getFunction(), vm.getGlobal());
	}

}
