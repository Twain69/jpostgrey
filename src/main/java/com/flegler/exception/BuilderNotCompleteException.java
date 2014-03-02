package com.flegler.exception;

public class BuilderNotCompleteException extends Exception {
	private static final long serialVersionUID = 1L;

	public BuilderNotCompleteException() {
		super();
	}

	public BuilderNotCompleteException(String msg) {
		super(msg);
	}

	public BuilderNotCompleteException(Exception exception) {
		super(exception);
	}

}
