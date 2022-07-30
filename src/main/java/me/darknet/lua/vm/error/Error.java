package me.darknet.lua.vm.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Error {

	final String source;
	final int line;
	final String message;

	public String print() {
		return source + ":" + line + ": " + message;
	}
}
