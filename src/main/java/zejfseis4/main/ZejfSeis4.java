package zejfseis4.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.SwingUtilities;

import org.tinylog.Logger;

import zejfseis4.client.ZejfClient;
import zejfseis4.data.DataManager;
import zejfseis4.events.EventManager;
import zejfseis4.exception.ApplicationErrorHandler;
import zejfseis4.exception.FatalIOException;
import zejfseis4.exception.RuntimeApplicationException;
import zejfseis4.scale.Scales;
import zejfseis4.serial.ZejfSerialReader;
import zejfseis4.ui.ZejfSeisFrame;

public class ZejfSeis4 {

	public static final File MAIN_FOLDER = new File("./ZejfSeis4/");
	public static final String VERSION = "4.7.0";
	public static final int COMPATIBILITY_VERSION = 4;

	public static boolean DEBUG = true; // always debug :)

	private static ZejfSeisFrame frame;
	private static DataManager dataManager;
	private static ZejfClient client;
	private static EventManager eventManager;
	private static ApplicationErrorHandler errorHandler;
	private static ZejfSerialReader serialReader;

	public static void init() {
		frame = new ZejfSeisFrame();
		errorHandler = new ApplicationErrorHandler(frame);
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);
		if (!MAIN_FOLDER.exists()) {
			if (!MAIN_FOLDER.mkdirs()) {
				System.err.println("Failed to create main dir");
				return;
			}
		}

		try {
			Settings.loadProperties();
			Scales.loadScales();
		} catch (FatalIOException e3) {
			handleException(e3);
		}

		client = new ZejfClient();
		serialReader = new ZejfSerialReader();
		dataManager = new DataManager();
		try {
			dataManager.loadFromInfo();
		} catch (FatalIOException e2) {
			handleException(e2);
		}

		eventManager = new EventManager();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				frame.createFrame();
				frame.setVisible(true);

				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						windowClosed(e);
					}
					@Override
					public void windowClosed(WindowEvent e) {
						try {
							dataManager.exit();
						} catch (FatalIOException e1) {
							handleException(e1);
						}
						try {
							eventManager.saveAll();
						} catch (FatalIOException e1) {
							handleException(e1);
						}
					}
				});
				
				getFrame().setStatus("Idle");
			}
		});

	}

	public static ZejfSeisFrame getFrame() {
		return frame;
	}

	public static DataManager getDataManager() {
		return dataManager;
	}

	public static ZejfClient getClient() {
		return client;
	}
	
	public static ZejfSerialReader getSerialReader() {
		return serialReader;
	}

	public static EventManager getEventManager() {
		return eventManager;
	}

	public static void handleException(Throwable e) {
		if (!(e instanceof RuntimeApplicationException)) {
			Logger.error("Caught exception : {}", e.getMessage());
			Logger.error(e);
		}
		errorHandler.handleException(e);
	}

}
