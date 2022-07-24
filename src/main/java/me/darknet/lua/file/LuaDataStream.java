package me.darknet.lua.file;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LuaDataStream{

	private int intSize;
	private int numberSize;
	private int sizeSize;
	InputStream stream;
	private boolean LE;

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
		// little endian
		long a1 = readInteger();
		long a2 = readInteger();
		if(LE) return (a1 << 32) | a2;
		return (a2 << 32) | a1;
	}

	public long readSize() throws IOException {
		return switch (sizeSize) {
			case 2 -> readShort();
			case 4 -> readInteger();
			case 8 -> readLong();
			default -> throw new IllegalStateException("Unexpected value: " + sizeSize);
		};
	}

	public double readNumber() throws IOException {
		return switch (numberSize) {
			//case 4 -> readFloat();
			//case 8 -> readDouble();
			case 8 -> 0f;
			default -> throw new IllegalStateException("Unexpected value: " + numberSize);
		};
	}

	public String readString() throws IOException {
		int size = this.readInteger();
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
}
