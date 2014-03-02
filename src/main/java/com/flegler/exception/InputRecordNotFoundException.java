package com.flegler.exception;

public class InputRecordNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public InputRecordNotFoundException() {
		super();
	}

	public InputRecordNotFoundException(String msg) {
		super(msg);
	}

	public InputRecordNotFoundException(Exception e) {
		super(e);
	}
}
