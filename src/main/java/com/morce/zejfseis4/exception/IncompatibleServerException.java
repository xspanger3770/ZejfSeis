package com.morce.zejfseis4.exception;

public class IncompatibleServerException extends RuntimeApplicationException {

	private static final long serialVersionUID = 337581990828507402L;
	
	public IncompatibleServerException() {
		super("Server not compatible with client!");
	}

}
