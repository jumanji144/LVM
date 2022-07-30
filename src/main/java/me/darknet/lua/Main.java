package me.darknet.lua;

import me.darknet.lua.file.LuaDataStream;
import me.darknet.lua.file.LuaFile;
import me.darknet.lua.file.LuaFileReader;
import me.darknet.lua.file.util.Disassembler;
import me.darknet.lua.vm.VM;
import me.darknet.lua.vm.data.Closure;
import me.darknet.lua.vm.data.Table;
import me.darknet.lua.vm.execution.ExecutionContext;
import me.darknet.lua.vm.value.ClosureValue;
import me.darknet.lua.vm.value.Value;

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
		vm.initalize();
		Table global = new Table();
		global.set("print", new ClosureValue(new Closure((ctx) -> {
			Value[] registers = ctx.getRegisters();
			if(registers.length == 0) throw new IllegalArgumentException("print requires at least one argument");
			for (Value register : registers) {
				System.out.print(register.asString());
				if(registers.length > 1) System.out.print("\t");
			}
			System.out.println();
		}, null)));
		System.out.println("Executing... ");
		ExecutionContext result = vm.getHelper().invoke(luaFile.getFunction(), global);
	}

}
