package com.morce.zejfseis4.main;

import com.morce.zejfseis4.exception.ApplicationErrorHandler;

public class Main {

	public static void main(String[] args) {
		var errorHandler = new ApplicationErrorHandler();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);
		ZejfSeis4.init();
	}

}
