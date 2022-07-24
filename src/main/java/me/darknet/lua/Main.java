package me.darknet.lua;

import me.darknet.lua.file.LuaDataStream;
import me.darknet.lua.file.LuaFile;
import me.darknet.lua.file.LuaFileReader;
import me.darknet.lua.file.constant.Constant;
import me.darknet.lua.file.constant.StringConstant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\JustusGarbe\\Documents\\sandbox\\luac.out");
		LuaDataStream luaStream = new LuaDataStream(new FileInputStream(file));
		LuaFileReader reader = new LuaFileReader(luaStream);
		LuaFile luaFile = reader.read();
		System.out.println(luaFile.getSource());
		int i = 0;
		for (Constant constant : luaFile.getFunction().getConstants()) {
			System.out.print("[" + i++ + "] ");
			if(constant instanceof StringConstant st) {
				System.out.println("'" + st.getValue() + "'");
			}
		}
		return;
	}

}
