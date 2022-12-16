package com.morce.zejfseis4.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.morce.zejfseis4.data.DataRequest;
import com.morce.zejfseis4.events.Intensity;
import com.morce.zejfseis4.main.Settings;
import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.utils.NamedThreadFactory;

public class RealtimeGraphPanel extends DataRequestPanel {

	private static final long serialVersionUID = 1L;
	private static RenderingHints defaultHints;
	
	public RealtimeGraphPanel() {
		setRequest(new DataRequest(ZejfSeis4.getDataManager(), "RealtimeGraph", Settings.REALTIME_DURATION_SECONDS * 1000));
		setLayout(null);
		
		threads();
	}

	private void threads() {
		ScheduledExecutorService exec = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("Realtime Graph Panel Work"));
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					repaint();
				} catch (Exception e) {
					ZejfSeis4.handleException(e); // uncaught
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
	}

	static {
		BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = img.createGraphics();
		defaultHints = g.getRenderingHints();
	}

	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHints(defaultHints);
		synchronized (dataRequest.dataMutex) {
			updateDrum(g);
		}
	}

	public static BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
			new float[] { 3 }, 0);
	private static final Font calibri12 = new Font("Calibri", Font.BOLD, 12);
	private Double line1;
	private int[] ones = new int[] { 1, 2, 5, 10 };
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
	

	public void updateDrum(Graphics2D graphics) {
		if(!ZejfSeis4.getDataManager().isLoaded()) {
			return;
		}
		int w = getWidth();
		int h = getHeight();
		if (w == 0 || h == 0) {
			return;
		}

		int mils = ZejfSeis4.getDataManager().getSampleTime();
		long graphTime = (System.currentTimeMillis() / mils) * mils;
		long graphStart = graphTime - Settings.REALTIME_DURATION_SECONDS * 1000;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				Settings.ANTIALIAS ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		int max = 10;

		for (long time = (long) (graphStart + (graphTime-graphStart)*0.05); time <= graphTime; time += mils) {
			double val = dataRequest.getFilteredValue(time);
			if (val != ZejfSeis4.getDataManager().getErrVal()) {
				double x = (w) * ((time - graphStart) / (Settings.REALTIME_DURATION_SECONDS * 1000.0));
				double v = Math.abs(val) * 1.25;
				if (x >= 16 && v > max) {
					max = (int) v;
				}
			}
		}

		graphics.setFont(calibri12);
		int extraWrx = 14;
		int wrx = graphics.getFontMetrics().stringWidth(String.format("%,d", -max)) + 6 + extraWrx;
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, w, h);

		// draw intensity levels
		graphics.setColor(Color.lightGray);
		graphics.fillRect(0, 0, wrx, h - 1);

		for (int i = Intensity.values().length - 1; i >= 0; i--) {
			double intensity = Intensity.values()[i].getIntensity();
			double y0 = h * 0.5 - (h * 0.5) * (intensity / (double) max);
			double y1 = h * 0.5 - (h * 0.5) * (-intensity / (double) max);
			Rectangle2D rect = new Rectangle2D.Double(1, y0, extraWrx - 1, y1 - y0);
			graphics.setColor(Intensity.values()[i].getColor());
			graphics.fill(rect);
			graphics.setColor(Color.black);
			graphics.draw(rect);
		}

		graphics.setColor(Color.black);
		graphics.drawRect(0, 0, extraWrx, h);

		graphics.setColor(Color.black);
		graphics.drawRect(0, 0, w - 1, h - 1);
		graphics.drawRect(0, 0, wrx, h - 1);

		double one = 1;
		mainLoop: for (int n = 0; n < 10; n++) {
			for (int i = 0; i < ones.length; i++) {
				one = Math.pow(10, n) * ones[i];
				if (h * (one / max) > 40) {
					break mainLoop;
				}
			}
		}
		for (double v = -one * (int) (max / one); v <= one * (int) (max / one); v += one) {
			double y1 = h * 0.5 - (h * 0.5) * (v / (double) max);
			graphics.setColor(Color.black);
			graphics.setFont(calibri12);
			String str = String.format("%,d", (int) v);
			int width = graphics.getFontMetrics().stringWidth(str);
			graphics.drawString(str, wrx - width - 2, (int) y1 + 4);
			graphics.setColor(v == 0 ? Color.black : Color.LIGHT_GRAY);
			graphics.setStroke(v == 0 ? new BasicStroke(1f) : dashed);
			graphics.drawLine(wrx + 1, (int) y1, w - 2, (int) y1);
		}

		graphics.setStroke(new BasicStroke(1));

		double lastV = 0;
		long lastT = -1;

		for (long t = graphStart; t <= graphTime; t += mils) {
			if(ZejfSeis4.getDataManager().getLogId(t) > dataRequest.lastLogID) {
				break;
			}
			double v = dataRequest.getFilteredValue(t);
			if (lastT == -1 || t - lastT > ((2 * 1000) / ZejfSeis4.getDataManager().getSampleRate())) {
				lastT = t;
				continue;
			}

			if (v == ZejfSeis4.getDataManager().getErrVal() || lastV == ZejfSeis4.getDataManager().getErrVal()) {
				lastT = t;
				lastV = v;
				continue;
			}

			double x1 = (w) * ((lastT - graphStart) / (Settings.REALTIME_DURATION_SECONDS * 1000.0));
			double x2 = (w) * ((t - graphStart) / (Settings.REALTIME_DURATION_SECONDS * 1000.0));
			double y1 = h * 0.5 - (h * 0.5) * (lastV / (double) max);
			double y2 = h * 0.5 - (h * 0.5) * (v / (double) max);
			if (x1 > wrx && x2 > wrx) {
				graphics.setColor(new Color(0, 0, 160));
				graphics.setStroke(new BasicStroke(1f));
				if (line1 == null) {
					line1 = new Line2D.Double(x1, y1, x2, y2);
				} else {
					line1.setLine(x1, y1, x2, y2);
				}
				graphics.draw(line1);
			}
			lastT = t;
			lastV = v;
		}

		if (mouse) {
			graphics.setColor(Color.black);
			graphics.setStroke(dashed);
			graphics.drawLine(wrx, mouseY, w, mouseY);
			graphics.drawLine(mouseX, 0, mouseX, h);

			int v = (int) ((h * 0.5 - mouseY) / (h * 0.5) * (double) max);
			int r = 8;
			Rectangle2D rect = new Rectangle2D.Double(extraWrx + 1, mouseY - r, wrx - extraWrx - 2, r * 2);
			graphics.setStroke(new BasicStroke(1f));
			graphics.setColor(Intensity.get(v).getColor());
			graphics.fill(rect);
			graphics.setColor(Color.black);
			graphics.draw(rect);

			graphics.setFont(calibri12);
			String str = String.format("%,d", (int) v);
			int width = graphics.getFontMetrics().stringWidth(str);
			graphics.drawString(str, wrx - width - 2, (int) mouseY + 5);
			
			long t = (long) ((mouseX / (double)w) *  (Settings.REALTIME_DURATION_SECONDS * 1000.0) + graphStart);
			str = dateFormat.format(new Date(t));
			int size = 16;
			int y = h - size;
			graphics.setFont(calibri12);
			int wi = graphics.getFontMetrics().stringWidth(str) + 10;
			rect = new Rectangle2D.Double(wrx, y, wi, size);
			graphics.setColor(Color.lightGray);
			graphics.fill(rect);
			graphics.setColor(Color.black);
			graphics.draw(rect);
			graphics.drawString(str, wrx + 5, y + 5 + size / 2);
		}

		graphics.setColor(Color.black);
		graphics.drawRect(0, 0, w - 1, h - 1);

	}

}
