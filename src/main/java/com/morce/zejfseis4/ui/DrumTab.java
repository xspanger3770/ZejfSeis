package com.morce.zejfseis4.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import com.morce.zejfseis4.data.DataRequest;
import com.morce.zejfseis4.exception.RuntimeApplicationException;
import com.morce.zejfseis4.main.Settings;
import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.utils.NamedThreadFactory;

public class DrumTab extends DataRequestPanel {

	private static final long serialVersionUID = 1L;

	private JButton btnBackM;
	private JButton btnBackS;
	private JButton btnForwardS;
	private JButton btnForwardM;

	private JPanel drumPanel;

	private BufferedImage drum;

	private Graphics2D drumGraphics;

	private boolean needsRedraw;
	private long lineID;
	private int lastDuration;

	protected int dragStartX = -1;
	protected int dragStartY = -1;
	protected int dragEndX = -1;
	protected int dragEndY = -1;

	public DrumTab() {
		setRequest(new DataRequest(ZejfSeis4.getDataManager(), "DrumTab",
				System.currentTimeMillis() - Settings.DRUM_SPACES[Settings.DRUM_SPACE_INDEX] * 60 * 1000l,
				System.currentTimeMillis()) {

			@Override
			public void onRefill(boolean realtime) {
				needsRedraw |= !realtime;
			}
		});

		setLayout(new BorderLayout(0, 0));

		JPanel panelControl = new JPanel();
		panelControl.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panelControl, BorderLayout.NORTH);
		btnBackM = new JButton("<<");
		panelControl.add(btnBackM);
		btnBackM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				incrementLineID(-10, lastDuration);
			}
		});
		btnBackS = new JButton("<");
		panelControl.add(btnBackS);

		btnBackS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				incrementLineID(-1, lastDuration);
			}
		});
		JButton btnGoto = new JButton("Goto");
		panelControl.add(btnGoto);

		btnGoto.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				gotoTime();
			}
		});

		JButton btnNow = new JButton("Now");
		panelControl.add(btnNow);

		btnNow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetLineID(lastDuration);
			}
		});

		btnForwardS = new JButton(">");
		panelControl.add(btnForwardS);

		btnForwardS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				incrementLineID(1, lastDuration);
			}
		});

		btnForwardM = new JButton(">>");
		panelControl.add(btnForwardM);
		btnForwardM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				incrementLineID(10, lastDuration);
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				needsRedraw = true;
			}
		});

		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				incrementLineID(e.getWheelRotation(), lastDuration);
			}
		});
		drumPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics gr) {
				super.paint(gr);
				Graphics2D g = (Graphics2D) (gr);
				if (drum != null) {
					g.drawImage(drum, 0, 0, null);
				}

				drawDrag(g);
			}
		};

		drumPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		drumPanel.setBackground(Color.WHITE);
		add(drumPanel, BorderLayout.CENTER);

		drumPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int x = e.getX();
					int y = e.getY();
					int w = getWidth();
					int h = getHeight();

					dragStartX = Math.max(wrx, Math.min(w, x));
					dragStartY = Math.max(0, Math.min(h, y));
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				openDataBrowser();

				dragStartX = -1;
				dragStartY = -1;
				dragEndX = -1;
				dragEndY = -1;
			}
		});

		drumPanel.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();

				int w = getWidth();
				int h = getHeight();

				dragEndX = Math.max(wrx, Math.min(w, x));
				dragEndY = Math.max(0, Math.min(h, y));

				drumPanel.repaint();
			}

		});

		lastDuration = Settings.DRUM_SPACES[Settings.DRUM_SPACE_INDEX];
		resetLineID(lastDuration);

		runThreads();
	}

	private void drawDrag(Graphics2D g) {
		if (dragStartX < 0 || dragEndX < 0) {
			return;
		}

		int w = drumPanel.getWidth();
		int h = drumPanel.getHeight();

		int line1 = (int) Math.round(((h - dragStartY) - LINE_PADDING) / (double) LINE_SPACE);
		int line2 = (int) Math.round(((h - dragEndY) - LINE_PADDING) / (double) LINE_SPACE);

		int startLine = line1 >= line2 ? line1 : line2;
		int endLine = line1 < line2 ? line1 : line2;
		int startX = line1 > line2 ? dragStartX : dragEndX;
		int endX = line1 < line2 ? dragStartX : dragEndX;

		if (line1 == line2) {
			startX = Math.min(dragStartX, dragEndX);
			endX = Math.max(dragStartX, dragEndX);
		}

		startX = Math.max(wrx, startX);
		endX = Math.max(wrx, endX);

		for (int line = startLine; line >= endLine; line--) {
			int x0 = line == startLine ? startX : wrx;
			int x1 = line == endLine ? endX : w;
			int y0 = (int) (h - LINE_PADDING - (line + 0.5) * LINE_SPACE);
			int y1 = y0 + LINE_SPACE;

			g.setColor(new Color(100, 100, 100, 100));
			g.fillRect(x0, y0, x1 - x0, y1 - y0);
		}
	}

	private void openDataBrowser() {
		if (!ZejfSeis4.getDataManager().isLoaded()) {
			return;
		}
		int w = drumPanel.getWidth();
		int h = drumPanel.getHeight();

		int line1 = (int) Math.round(((h - dragStartY) - LINE_PADDING) / (double) LINE_SPACE);
		int line2 = (int) Math.round(((h - dragEndY) - LINE_PADDING) / (double) LINE_SPACE);

		long time1 = (long) (getMillis(lineID - line1, lastDuration)
				+ ((dragStartX - wrx) / (double) (w - wrx)) * lastDuration * 60 * 1000l);
		long time2 = (long) (getMillis(lineID - line2, lastDuration)
				+ ((dragEndX - wrx) / (double) (w - wrx)) * lastDuration * 60 * 1000l);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new DataExplorer(Math.min(time1, time2), Math.max(time1, time2));
			}
		});
	}

	private void runThreads() {
		ScheduledExecutorService exec = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("Drum Tab Work"));
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					runUpdate();
					ZejfSeis4.getFrame().repaint();
				} catch (Exception e) {
					ZejfSeis4.handleException(e);
				}
			}
		}, 0, 100, TimeUnit.MILLISECONDS);
	}

	private long lastCurrentLineID = -1;

	private void runUpdate() {
		int w = drumPanel.getWidth();
		int h = drumPanel.getHeight();

		if (w <= 0 || h <= 0 || !ZejfSeis4.getDataManager().isLoaded()) {
			return;
		}

		long _lineID = lineID;
		int _duration = Settings.DRUM_SPACES[Settings.DRUM_SPACE_INDEX];
		long currentLineID = calculateCurrentLineID(_duration);
		if (_duration != lastDuration) {
			long newLineID = (long) ((_lineID) * (lastDuration / (double) _duration));
			setLineID(newLineID, _duration);
			_lineID = lineID;
		}

		if (currentLineID - lastCurrentLineID == 1 && _lineID == lastCurrentLineID) {
			resetLineID(_duration);
		}
		lastCurrentLineID = currentLineID;

		updateButtons(_lineID != currentLineID);

		double _gain = Settings.DRUM_GAIN;
		int _decimate = Settings.DECIMATE;

		boolean redraw = needsRedraw;
		needsRedraw = false;
		if (drum != null) {
			redraw |= w != drum.getWidth() || h != drum.getHeight();
			redraw |= _duration != lastDuration;
			redraw |= _decimate != lastDecimate;
			redraw |= _gain != lastGain;

			lastDuration = _duration;
			lastDecimate = _decimate;
			lastGain = _gain;
		}

		int lines = (int) Math.round((h - 2 * LINE_PADDING) / (double) LINE_SPACE + 1);

		long endTime = getMillis(_lineID + 1, _duration);
		long startTime = getMillis(_lineID - lines + 1, _duration) - FILTER_PADDING_MINUES * 60 * 1000l;
		if (endTime != this.endTime || startTime != this.startTime) {
			getDataRequest().changeTimes(startTime, endTime);
			this.endTime = endTime;
			this.startTime = startTime;
		}

		paintDrum(w, h, redraw, _lineID, lines, _duration, _decimate, _gain);

	}

	private long lastDrawLogID;

	private void paintDrum(int w, int h, boolean fullRedraw, long currentLineID, int lines, int duration, int decimate,
			double gain) {
		BufferedImage newDrum = null;
		if (drum == null || fullRedraw) {
			lastDrawLogID = -1;

			newDrum = toCompatibleImage(new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR));

			drumGraphics = newDrum.createGraphics();

			drumGraphics.setColor(Color.white);
			drumGraphics.fillRect(0, 0, w, h);

			drawBackground(w, h, currentLineID, lines, duration);
			synchronized (dataRequest.dataMutex) {
				drawForeground(w, h, currentLineID, lines, duration, decimate, gain);
			}
			drum = newDrum;
		}

		if (drumGraphics != null && lastDrawLogID != -1 && dataRequest.lastLogID >= lastDrawLogID + lastDecimate) {
			drawIt(w, h, lastDrawLogID, dataRequest.lastLogID, drumGraphics, dataRequest.lastLogID, currentLineID,
					lines, duration, decimate, gain);
		}

	}

	public static final int LINE_SPACE = 27;
	public static final int LINE_PADDING = 28;

	public static final int FILTER_PADDING_MINUES = 5;

	private int wrx;

	private long startTime;
	private long endTime;

	private double lastGain;
	private int lastDecimate;

	private void drawBackground(int w, int h, long _lineID, int lines, int duration) {
		Graphics2D g = drumGraphics;

		g.setFont(new Font("Consolas", Font.BOLD, 24));
		wrx = g.getFontMetrics().stringWidth("24") + 4;

		g.setColor(Color.black);
		g.drawRect(0, 0, w - 1, h - 1);
		g.drawRect(0, 0, wrx, h - 1);

		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < lines; i++) {
			int y = h - LINE_PADDING - i * LINE_SPACE;

			g.setColor(Color.gray);
			g.drawLine(wrx, y, w, y);

			cal.setTimeInMillis(getMillis(_lineID - i, duration));

			int minutes = cal.get(Calendar.MINUTE);
			int hour = cal.get(Calendar.HOUR_OF_DAY);

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (minutes == 0) {
				g.setFont(new Font("Consolas", Font.BOLD, 24));
				g.setColor(Color.red);
				String str = String.format("%02d", hour);
				g.drawString(str, 2, (int) (y + 7));
			} else {
				g.setFont(new Font("Consolas", Font.BOLD, 20));
				g.setColor(Color.gray);
				String str = String.format("%02d", minutes);
				g.drawString(str, 4, (int) (y + 7));
			}

			if (hour == 0 && minutes == 0) {
				g.setFont(new Font("Consolas", Font.BOLD, 16));
				g.setColor(Color.blue);
				String str = ddMMyyyy.format(cal.getTime());
				g.drawString(str, wrx + 4, (int) (y - 7));
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	private void drawForeground(int w, int h, long _lineID, int lines, int duration, int decimate, double gain) {
		Graphics2D g = drumGraphics;
		g.setColor(Color.black);
		long startLogID = ZejfSeis4.getDataManager().getLogId(getMillis(_lineID - lines + 1, duration)) - 1;
		long endLogID = ZejfSeis4.getDataManager().getLogId(getMillis(_lineID + 1, duration));
		long lastLogID = dataRequest.lastLogID;
		drawIt(w, h, startLogID, endLogID, g, lastLogID, _lineID, lines, duration, decimate, gain);

		// removed MultiThread
	}

	private void drawIt(int w, int h, long start, long end, Graphics2D g, long lastLogID, long currentLineID, int lines,
			int duration, int decimate, double gain) {
		long startLogID = ZejfSeis4.getDataManager().getLogId(getMillis(currentLineID - lines + 1, duration)) - 1;
		long LOGS_PER_LINE = ZejfSeis4.getDataManager().getLogId(duration * 60 * 1000l);
		long chuj = ZejfSeis4.getDataManager().getErrVal();
		Line2D.Double drawable = new Line2D.Double();
		int line = (int) ((start - startLogID) / LOGS_PER_LINE);
		g.setColor(Color.black);
		for (long id = start; id < end; id += decimate) {
			if (id > lastLogID) {
				break;
			}
			long nextId = id + decimate;
			double val1 = dataRequest.getFilteredValueByLogID(id);
			double val2 = dataRequest.getFilteredValueByLogID(nextId);
			if (val2 != chuj) {
				lastDrawLogID = nextId;
			}
			if (val1 != chuj && val2 != chuj) {
				long lineStartLogID = startLogID + line * LOGS_PER_LINE;
				double x1 = wrx + (((id - lineStartLogID)) / (double) LOGS_PER_LINE) * (w - wrx);
				double x2 = wrx + (((id + decimate - lineStartLogID)) / (double) LOGS_PER_LINE) * (w - wrx);
				double y1 = (h - LINE_PADDING - (lines - line - 1) * LINE_SPACE) - val1 * (gain / 10000.0);
				double y2 = (h - LINE_PADDING - (lines - line - 1) * LINE_SPACE) - val2 * (gain / 10000.0);
				drawable.setLine(x1, y1, x2, y2);
				g.draw(drawable);
			}

			int _line = (int) ((id - startLogID) / LOGS_PER_LINE);
			if (_line != line) {
				line = _line;
				id -= decimate;
			}
		}

	}

	private synchronized void setLineID(long lineID, int duration) {
		long currentLineID = calculateCurrentLineID(duration);
		this.lineID = Math.min(currentLineID, lineID);
		updateButtons(this.lineID != currentLineID);
	}

	private synchronized void incrementLineID(long amount, int duration) {
		setLineID(this.lineID + amount, duration);
	}

	private synchronized void resetLineID(int duration) {
		long currentLineID = calculateCurrentLineID(duration);
		this.lineID = currentLineID;
		updateButtons(true);
	}

	private void updateButtons(boolean enabled) {
		btnForwardM.setEnabled(enabled);
		btnForwardS.setEnabled(enabled);
	}

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
	private static SimpleDateFormat ddMMyyyy = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

	protected void gotoTime() {
		String str = JOptionPane.showInputDialog(ZejfSeis4.getFrame(), "dd.MM.yyyy [HH:mm]", "Enter time:",
				JOptionPane.QUESTION_MESSAGE);
		if (str != null && !str.isEmpty()) {
			try {
				Date date;
				try {
					date = dateFormat.parse(str);
				} catch (Exception e) {
					date = ddMMyyyy.parse(str);
				}
				Calendar c = Calendar.getInstance();
				c.setTime(date);

				setLineID(c.getTimeInMillis() / (lastDuration * 60 * 1000l), lastDuration);
			} catch (ParseException ex) {
				throw new RuntimeApplicationException("Unparseable date", ex);
			}
		}
	}

	private long calculateCurrentLineID(long duration) {
		return System.currentTimeMillis() / (duration * 60 * 1000l);
	}

	private long getMillis(long lineID, int duration) {
		return lineID * duration * 60 * 1000l;
	}

	@Override
	public void updateFilter() {
		super.updateFilter();
		needsRedraw = true;
	}

}
