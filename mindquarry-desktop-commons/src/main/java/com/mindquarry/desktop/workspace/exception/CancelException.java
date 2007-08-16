package com.mindquarry.desktop.workspace.exception;

public class CancelException extends Exception {
	public CancelException() {
	}

	public CancelException(String message) {
		super(message);
	}

	public CancelException(Throwable cause) {
		super(cause);
	}

	public CancelException(String message, Throwable cause) {
		super(message, cause);
	}
}
