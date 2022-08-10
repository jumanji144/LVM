package me.darknet.lua.file;

import lombok.Getter;
import lombok.Setter;
import me.darknet.lua.file.function.LuaFunction;

@Getter
@Setter
public class LuaFile {

	int version;
	int format;
	int endianess;
	int intSize;
	int longSize;
	int instructionSize;
	int numberSize;
	boolean isNumberIntegral;
	LuaFunction function;

	public String getLuaVersion() {
		// version is (MAJOR * 16) + MINOR
		int major = version / 16;
		int minor = version % 16;
		return String.format("%d.%d", major, minor);
	}

	public String getSource() {
		return function.getSource();
	}

}
