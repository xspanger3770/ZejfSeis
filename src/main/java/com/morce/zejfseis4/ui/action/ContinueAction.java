package com.morce.zejfseis4.ui.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

public class ContinueAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	public ContinueAction() {
		super("Continue");
		putValue(SHORT_DESCRIPTION, "Continue (further errors may occur)");
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());

		if (w != null) {
			w.setVisible(false);
		}
	}

}
