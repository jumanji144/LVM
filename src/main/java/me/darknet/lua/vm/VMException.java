package me.darknet.lua.vm;

public class VMException extends RuntimeException{

	public String message;
	public String details;

	public VMException(String message) {
		super(message);
		this.message = message;
	}

	public VMException(String message, String details) {
		super(message);
		this.message = message;
		this.details = details;
	}

	public VMException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getDetails() {
		return details;
	}

}
