package me.darknet.lua.file;

import me.darknet.lua.file.constant.*;
import me.darknet.lua.file.function.AbsoluteLineInfo;
import me.darknet.lua.file.function.Local;
import me.darknet.lua.file.function.LuaFunction;
import me.darknet.lua.file.function.Upvalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LuaFileReader implements ConstantTypes {

	private static final Logger logger = LoggerFactory.getLogger(LuaFileReader.class);
	private static final byte[] verifyData = {
		0x19, (byte) 0x93, '\r', '\n', 0x1a, '\n'
	};

	private static final int LUA_VERIFY_INT = 0x5678;
	private static final double LUA_VERIFY_NUM = 370.5;

	LuaDataStream stream;

	public LuaFileReader(LuaDataStream stream) {
		this.stream = stream;
	}

	LuaFunction readFunction(LuaFile file) throws IOException {
		LuaFunction function = new LuaFunction();
		if(file.version >= 83 || file.version == 81) {
			function.setSource(stream.readString());
		}
		// generic function reading code (is the same again)
		function.setLineDefined(stream.readInteger());
		function.setLastLineDefined(stream.readInteger());
		if(file.version == 81) function.setNumUps(stream.readByte());
		function.setNumParams(stream.readByte());
		function.setIsVararg(stream.readByte());
		function.setMaxStackSize(stream.readByte());

		int codeSize = stream.readInteger();
		int[] code = new int[codeSize];
		for(int i = 0; i < codeSize; i++) {
			code[i] = stream.readInteger();
		}
		function.setCode(code);

		List<Constant> constants = new ArrayList<>();
		int constantCount = (int) stream.readInteger();
		for(int i = 0; i < constantCount; i++) {
			int type = stream.readByte();
			constants.add(switch (type) {
				case TNIL -> new NilConstant();
				case TBOOLEAN -> new BooleanConstant(file.version >= 83 && stream.readByte() != 0);
				case VTRUE -> new BooleanConstant(true);
				case TNUMBER -> {
					if(file.version >= 83) {
						yield new IntConstant(stream.readInteger());
					} else {
						yield new NumberConstant(stream.readNumber());
					}
				}
				case TSTRING, VLNGSTR -> new StringConstant(stream.readString());
				default -> throw new IllegalStateException("Unexpected value: " + type);
			});
		}

		function.setConstants(constants);

		if(file.version >= 80 && file.version < 83) {
			// read protos
			int protoCount = stream.readInteger();
			for(int i = 0; i < protoCount; i++) {
				function.addPrototype(readFunction(file));
			}
		}

		if(file.version != 81) {
			// read upvalues
			int upvalueCount = stream.readInteger();
			for(int i = 0; i < upvalueCount; i++) {
				function.addUpvalue(new Upvalue(null, stream.readByte() != 0, stream.readByte(), file.version >= 83 ? stream.readByte() : 0));
			}
		}

		if(file.version >= 83) {
			int protoCount = stream.readInteger();
			for(int i = 0; i < protoCount; i++) {
				function.addPrototype(readFunction(file));
			}
		}

		// read debug
		if(file.version == 82) function.setSource(stream.readString());
		int n = stream.readInteger();
		for(int i = 0; i < n; i++) {
			function.addLine(stream.readInteger());
		}

		if(file.version > 83) { // 5.3+
			int absLineCount = stream.readInteger();
			for(int i = 0; i < absLineCount; i++) {
				function.addAbsoluteLine(new AbsoluteLineInfo(stream.readInteger(), stream.readInteger()));
			}
		}

		int localCount = stream.readInteger();
		for(int i = 0; i < localCount; i++) {
			Local local = new Local(stream.readString(), stream.readInteger(), stream.readInteger());
			function.addLocal(local);
		}

		int upvalueCount = stream.readInteger();
		for(int i = 0; i < upvalueCount; i++) {
			if(file.version == 81)
				function.addUpvalue(new Upvalue(stream.readString(), stream.readByte() != 0, stream.readByte(), 0));
			else
				function.getUpvalue(i).setName(stream.readString());
		}

		return function;
	}

	public LuaFile read() {

		try {

			if(stream.readInteger() != 0x1b4c7561) {
				throw new IllegalStateException("Invalid magic number");
			}

			LuaFile file = new LuaFile();
			file.version = stream.readByte();
			file.format = stream.readByte();

			if(file.format != 0) logger.warn("Unsupported format: {}", file.format);
			if(file.version >= (83)) { // data format is completely different in 5.3+
				byte[] verify = new byte[verifyData.length];
				stream.readFully(verify);
				if (!Arrays.equals(verify, verifyData)) {
					throw new IllegalStateException("Invalid verify data");
				}
				file.instructionSize = stream.readByte();
				file.intSize = stream.readByte();
				file.numberSize = stream.readByte();

				stream.setNumberSize(file.numberSize);
				stream.setIntSize(file.intSize);
				stream.setSizeSize(8);

				// verify data is now verified
				if(stream.readInteger() != LUA_VERIFY_INT) {
					throw new IllegalStateException("Invalid verify data");
				}
				if(stream.readNumber() != LUA_VERIFY_NUM) {
					throw new IllegalStateException("Invalid verify data");
				}
			} else {
				// endianess
				file.endianess = stream.readByte();
				file.intSize = stream.readByte();
				file.longSize = stream.readByte();
				file.instructionSize = stream.readByte();
				file.numberSize = stream.readByte();
				file.isNumberIntegral = stream.readByte() != 0;

				if(file.version == 82) {
					byte[] verify = new byte[verifyData.length];
					stream.readFully(verify);
					if (!Arrays.equals(verify, verifyData)) {
						throw new IllegalStateException("Invalid verify data");
					}
				}

				// update stream
				stream.setIntSize(file.intSize);
				stream.setNumberSize(file.numberSize);
				stream.setSizeSize(file.longSize);

			}

			stream.setEndianess(file.endianess != 1);
			file.function = readFunction(file);

			return file;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

}
