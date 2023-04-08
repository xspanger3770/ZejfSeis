package zejfseis4.ui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

/**
 * Action responsible for terminating the application.
 */
public final class CloseAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
	private Window frame;

	public CloseAction(Window frame) {
        super("Close");
        this.frame = frame;
        putValue(SHORT_DESCRIPTION, "Save everything and close normally");
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.dispose();
        System.exit(0);
    }
}
