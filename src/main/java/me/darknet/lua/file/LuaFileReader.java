package me.darknet.lua.file;

import me.darknet.lua.file.constant.ConstantTypes;
import me.darknet.lua.file.function.LuaFunctionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class LuaFileReader implements ConstantTypes {

	private static final Logger logger = LoggerFactory.getLogger(LuaFileReader.class);
	private static final byte[] verifyData = {
		0x19, (byte) 0x93, '\r', '\n', 0x1a, '\n'
	};

	private static final int LUA_VERIFY_INT = 0x5678;
	private static final double LUA_VERIFY_NUM = 370.5;
	private static final double LUA_5_0_VERIFY_NUM = 3.14159265358979323846E7;

	LuaDataStream stream;

	public LuaFileReader(LuaDataStream stream) {
		this.stream = stream;
	}

	public LuaFile read() {

		try {

			if(stream.readInteger() != 0x1b4c7561) {
				throw new IllegalStateException("Invalid magic number");
			}

			LuaFile file = new LuaFile();
			file.version = stream.readByte();

			if(file.version < 80 || file.version > 83) throw new IllegalStateException("Unsupported version: " + file.getLuaVersion());

			if(file.version > 80) file.format = stream.readByte();

			if(file.format != 0) logger.warn("Unsupported format: {}", file.format);
			if(file.version == (83)) { // data format is completely different in 5.3+
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
				stream.setByteSize(true);
				stream.setEndianess(false);

				// verify data is now verified
				if(stream.readInt() != LUA_VERIFY_INT) {
					throw new IllegalStateException("Invalid verify data");
				}
				double verifyNum = stream.readNumber();
				if(verifyNum != LUA_VERIFY_NUM) {
					throw new IllegalStateException("Invalid verify data");
				}
			} else {
				// endianess
				file.endianess = stream.readByte();
				stream.setIntSize(file.intSize = stream.readByte());
				stream.setSizeSize(file.longSize = stream.readByte());
				file.instructionSize = stream.readByte();
				if(file.version == 80) {
					stream.readByte(); // SIZE_OP
					stream.readByte(); // SIZE_A
					stream.readByte(); // SIZE_B
					stream.readByte(); // SIZE_C
				}
				stream.setNumberSize(file.numberSize = stream.readByte());
				stream.setEndianess(file.endianess != 1);
				if(file.version > 80) file.isNumberIntegral = stream.readByte() != 0;
				else {
					double number = stream.readNumber();
					// cast to long without conversion
					if (number != LUA_5_0_VERIFY_NUM)
						throw new IllegalStateException("Invalid verify data");
				}

				if(file.version == 82) {
					byte[] verify = new byte[verifyData.length];
					stream.readFully(verify);
					if (!Arrays.equals(verify, verifyData)) {
						throw new IllegalStateException("Invalid verify data");
					}
				}
			}

			LuaFunctionReader functionReader = new LuaFunctionReader(file, stream);
			file.function = functionReader.readFunction();

			return file;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

}
