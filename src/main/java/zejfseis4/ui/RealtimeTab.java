package zejfseis4.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class RealtimeTab extends JPanel {

	private static final long serialVersionUID = 1L;
	private RealtimeGraphPanel realtimeGraphPanel;
	private SpectrogramPanel spectrogramPanel;

	public RealtimeTab() {
		setLayout(new GridLayout(0, 1, 0, 0));
		realtimeGraphPanel = new RealtimeGraphPanel();
		realtimeGraphPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		realtimeGraphPanel.setPreferredSize(new Dimension(100, 100));
		add(realtimeGraphPanel);

		spectrogramPanel = new SpectrogramPanel();
		spectrogramPanel.setPreferredSize(new Dimension(100, 100));
		spectrogramPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(spectrogramPanel);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	public RealtimeGraphPanel getRealtimeGraphPanel() {
		return realtimeGraphPanel;
	}

	public SpectrogramPanel getSpectrogramPanel() {
		return spectrogramPanel;
	}

}
