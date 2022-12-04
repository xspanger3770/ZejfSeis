package com.morce.zejfseis4.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.morce.zejfseis4.client.ZejfClient;
import com.morce.zejfseis4.data.DataManager;
import com.morce.zejfseis4.events.EventManager;
import com.morce.zejfseis4.ui.ZejfSeisFrame;

public class ZejfSeis4 {
	
	public static final File MAIN_FOLDER = new File("./ZejfSeis4/");
	public static final String VERSION = "4.4.4";
	public static final int COMPATIBILITY_VERSION = 4;
	
	private static ZejfSeisFrame frame;
	private static DataManager dataManager;
	private static ZejfClient client;
	private static EventManager eventManager;

	public static void init() {
		if(!MAIN_FOLDER.exists()) {
			if(!MAIN_FOLDER.mkdirs()) {
				System.err.println("Failed to create main dir");
				return;
			}
		}
		client = new ZejfClient();
		dataManager = new DataManager();
		dataManager.loadFromInfo();
		
		eventManager = new EventManager();
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				frame = new ZejfSeisFrame();
				frame.createFrame();
				frame.setVisible(true);
				
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						dataManager.exit();
						eventManager.saveAll();
					}
				});
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
	
	public static EventManager getEventManager() {
		return eventManager;
	}
	
	public static void errorDialog(Exception e) {
		JOptionPane.showMessageDialog(frame, e.getClass().getCanonicalName()+": "+e.getMessage(), "An error has occured", JOptionPane.ERROR_MESSAGE);
	}

}
