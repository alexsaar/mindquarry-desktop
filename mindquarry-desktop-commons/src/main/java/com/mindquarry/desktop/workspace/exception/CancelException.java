package com.mindquarry.desktop.workspace.exception;

public class CancelException extends Exception {

    private static final long serialVersionUID = 7208012019092834495L;

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
