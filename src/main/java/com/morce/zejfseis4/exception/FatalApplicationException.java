package com.morce.zejfseis4.exception;

public class FatalApplicationException extends Exception implements FatalError {

	private static final long serialVersionUID = 1L;
	
	public FatalApplicationException(Throwable cause) {
		super(cause);
	}
	
	public FatalApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getUserMessage() {
		return super.getMessage();
	}

}
