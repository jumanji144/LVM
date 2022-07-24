package me.darknet.lua.file.constant;

public interface ConstantTypes {

	static int makeVariant(int t, int v) {
		return (t) | (v << 4);
	}

	int TNIL = 0;
	int TBOOLEAN = 1;
	int VFALSE = TBOOLEAN;
	int VTRUE = TBOOLEAN | 1 << 4;
	int TNUMBER = 3;
	int VNUMINT = TNUMBER;
	int VNUMFLT = TNUMBER | 1 << 4;
	int TSTRING = 4;
	int VSHRSTR = TSTRING;
	int VLNGSTR = TSTRING | 1 << 4;

}
