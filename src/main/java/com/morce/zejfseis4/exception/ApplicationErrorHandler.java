package com.morce.zejfseis4.exception;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tinylog.Logger;

import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.ui.ZejfSeisFrame;
import com.morce.zejfseis4.ui.action.CloseAction;
import com.morce.zejfseis4.ui.action.IgnoreAction;
import com.morce.zejfseis4.ui.action.TerminateAction;

public class ApplicationErrorHandler implements Thread.UncaughtExceptionHandler {

	private ZejfSeisFrame frame;

	public ApplicationErrorHandler(ZejfSeisFrame frame) {
		this.frame = frame;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Logger.error("An uncaught exception has occured in thread {} : {}", t.getName(), e.getMessage());
		Logger.error(e);
		frame.dispose();
		handleException(e);
		System.exit(1);
	}

	public synchronized void handleException(Throwable e) {
		System.err.println((e instanceof RuntimeApplicationException) + ", " + e.getCause());
		if (ZejfSeis4.DEBUG && !(e instanceof RuntimeApplicationException)) {
			showDetailedError(e);
			return;
		}

		if (e instanceof FatalError ex) {
			showGeneralError(ex.getUserMessage(), true);
		} else if (e instanceof ApplicationException ex) {
			showGeneralError(ex.getUserMessage(), false);
		} else {
			showGeneralError("Oops something went wrong!", true);
		}
	}

	int i = 0;

	private void showDetailedError(Throwable e) {
		final Object[] options = getOptionsForDialog(true);
		JOptionPane.showOptionDialog(frame, createDetailedPane(e), "Fatal Error", JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, null);
		i++;
		if (i == 2) {
			System.exit(0);
		}
	}

	private Component createDetailedPane(Throwable e) {
		JTextArea textArea = new JTextArea(16, 60);
		textArea.setEditable(false);
		StringWriter stackTraceWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTraceWriter));
		textArea.append(stackTraceWriter.toString());
		JScrollPane scrollPane = new JScrollPane(textArea);
		return scrollPane;
	}

	private void showGeneralError(String message, boolean isFatal) {
		final String title = isFatal ? "Fatal Error" : "Application Error";
		final Object[] options = getOptionsForDialog(isFatal);

		JOptionPane.showOptionDialog(frame, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
				options, null);
	}

	private Component[] getOptionsForDialog(boolean isFatal) {
		if (!isFatal) {
			return null; // use default
		}

		return new Component[] { new JButton(new TerminateAction()), new JButton(new CloseAction(frame)),
				new JButton(new IgnoreAction()) };
	}
}
