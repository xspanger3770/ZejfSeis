package com.morce.zejfseis4.exception;

public class FatalApplicationException extends Exception implements FatalError {

	private static final long serialVersionUID = 1L;
	
	public FatalApplicationException(Exception e) {
		super(e);
	}

	@Override
	public String getUserMessage() {
		return super.getMessage();
	}

}
