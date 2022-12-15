package com.morce.zejfseis4.exception;

import org.tinylog.Logger;

import com.morce.zejfseis4.ui.action.GenericQuitAction;

import javax.swing.*;
import java.awt.*;

public class ApplicationErrorHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Logger.error("Application error handler: {}", e.getMessage());
		Logger.error(e);

		if (e instanceof FatalError ex) {
			showGeneralError(ex.getUserMessage(), true);
		} else if (e instanceof ApplicationException ex) {
			showGeneralError(ex.getUserMessage(), false);
		} else {
			showGeneralError("Oops something went wrong!", true);
		}
	}

	private static void showGeneralError(String message, boolean isFatal) {
		final String title = isFatal ? "Fatal Application Error" : "Application Error";
		final Object[] options = getOptionsForDialog(isFatal);

		EventQueue.invokeLater(() -> JOptionPane.showOptionDialog(null, message, title, JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, null));
	}

	private static Object[] getOptionsForDialog(boolean isFatal) {
        if (!isFatal) {
            return null; // use default
        }

        return new Object[]{
                new GenericQuitAction()
        };
    }
}
