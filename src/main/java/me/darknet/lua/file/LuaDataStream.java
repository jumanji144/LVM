package me.darknet.lua.file;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LuaDataStream{

	private int intSize;
	private int numberSize;
	private int sizeSize;
	InputStream stream;
	private boolean LE;
	private boolean byteSize;

	public LuaDataStream(InputStream stream) {
		this.stream = stream;
		this.intSize = 4;
		this.numberSize = 8;
		this.sizeSize = 4;
		this.LE = true;
	}

	public int readByte() throws IOException {
		return stream.read();
	}

	public int readShort() throws IOException {
		// little endian
		int b1 = readByte();
		int b2 = readByte();
		if(LE) return (b1 << 8) | b2;
		return (b2 << 8) | b1;
	}

	public int readInteger() throws IOException {
		// little endian
		int a1 = readShort();
		int a2 = readShort();
		if(LE) return (a1 << 16) | a2;
		return (a2 << 16) | a1;
	}

	public long readLong() throws IOException {
		// we need to use byte buffer to read long
		byte[] a = new byte[8];
		stream.read(a);
		ByteBuffer b = ByteBuffer.wrap(a).order(!LE ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		return b.getLong();
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInteger());
	}

	public double readDouble() throws IOException {
		long l = readLong();
		return Double.longBitsToDouble(l);
	}

	public long readSize() throws IOException {
		return switch (sizeSize) {
			case 2 -> readShort();
			case 4 -> readInteger();
			case 8 -> readLong();
			default -> throw new IllegalStateException("Unexpected value: " + sizeSize);
		};
	}

	public long readInt() throws IOException {
		return switch (intSize) {
			case 1 -> readByte();
			case 2 -> readShort();
			case 4 -> readInteger();
			case 8 -> readLong();
			default -> throw new IllegalStateException("Unexpected value: " + intSize);
		};
	}

	public double readNumber() throws IOException {
		return switch (numberSize) {
			case 4 -> readFloat();
			case 8 -> readDouble();
			default -> throw new IllegalStateException("Unexpected value: " + numberSize);
		};
	}

	public String readString() throws IOException {
		int size;
		if(byteSize) {
			size = readByte();
			if(size == 0xFF) {
				size = (int) readSize();
			}
		}else size = (int) this.readSize();
		if(size == 0) return "";
		byte[] bytes = new byte[size];
		this.readFully(bytes);
		// remove last byte because java doesn't expect \0 at the end of a string
		return new String(bytes, 0, size - 1);
	}

	public void readFully(byte[] verify) throws IOException {
		stream.read(verify);
	}

	public void setIntSize(int intSize) {
		this.intSize = intSize;
	}

	public void setSizeSize(int sizeSize) {
		this.sizeSize = sizeSize;
	}

	public void setNumberSize(int numberSize) {
		this.numberSize = numberSize;
	}

	public void setEndianess(boolean LE) {
		this.LE = LE;
	}

	public void setByteSize(boolean byteSize) {
		this.byteSize = byteSize;
	}

}
