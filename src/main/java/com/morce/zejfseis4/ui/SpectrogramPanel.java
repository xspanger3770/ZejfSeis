package com.morce.zejfseis4.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.morce.zejfseis4.data.DataRequest;
import com.morce.zejfseis4.main.Settings;
import com.morce.zejfseis4.main.ZejfSeis4;
import com.morce.zejfseis4.utils.NamedThreadFactory;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SpectrogramPanel extends DataRequestPanel {

	private static final long serialVersionUID = 1L;
	private BufferedImage spectro;
	protected boolean resized;

	public static Color[] scale;
	public static DoubleFFT_1D ifft;

	public boolean fullRedrawNext;

	static {
		try {
			BufferedImage img = ImageIO.read(SpectrogramPanel.class.getResource("/scale/scale2.png"));
			scale = new Color[img.getHeight()];
			for (int i = 0; i < img.getHeight(); i++) {
				scale[i] = new Color(img.getRGB(0, i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SpectrogramPanel() {
		setRequest(new DataRequest(ZejfSeis4.getDataManager(), "Spectro", Settings.REALTIME_DURATION_SECONDS * 1000) {
			@Override
			public void onRefill(boolean realtime) {
				if (!realtime) {
					fullRedrawNext = true;
				}
			}
		});
		// TODO EVENTS

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				resized = true;
			}
		});
		threads();
	}

	private void threads() {
		ScheduledExecutorService exec = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("Spectrogram Graph Panel Work"));
		ScheduledExecutorService exec2 = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("resize"));
		exec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					repaint();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
		exec2.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (resized) {
						resized = false;
						fullRedrawNext = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 400, TimeUnit.MILLISECONDS);
	}

	@Override
	public void paint(Graphics g) {
		maxF = Settings.SPECTRO_MAX_FREQUENCY;
		super.paint(g);
		g.setColor(scale[0]);
		g.fillRect(0, 0, getWidth(), getHeight());
		synchronized (dataRequest.dataMutex) {
			drawSpectroNew();
			if (spectro != null)
				g.drawImage(spectro, 0, 0, null);
			drawScale((Graphics2D) g);
		}
		Graphics2D graphics = (Graphics2D) g;
		if (mouse) {
			int w = getWidth();
			int h = getHeight();
			graphics.setColor(Color.black);
			graphics.setStroke(dashed);
			graphics.drawLine(wrx, mouseY, w, mouseY);
			graphics.drawLine(mouseX, 0, mouseX, h);

			double max = maxF;
			double v = (-mouseY + h) / (double) h * max;

			int r = 8;
			Rectangle2D.Double rect = new Rectangle2D.Double(0, mouseY - r, wrx, 2 * r);
			graphics.setStroke(new BasicStroke(1f));
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.fill(rect);
			graphics.setColor(Color.BLACK);
			graphics.draw(rect);

			graphics.setColor(Color.black);
			String str = String.format("%.1fHz", v);
			int width = graphics.getFontMetrics().stringWidth(str);
			graphics.drawString(str, wrx - width - 2, mouseY + 5);
			
			long t = (long) ((mouseX / (double)w-1) *  (Settings.REALTIME_DURATION_SECONDS * 1000.0) + System.currentTimeMillis());
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
	}

	private static final Font calibri12 = new Font("Calibri", Font.BOLD, 12);
	private int[] ones = new int[] { 1, 2, 5, 10 };
	public static BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
			new float[] { 4 }, 0);

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);
	
	double maxF = 20;
	private int wrx = -1;

	private void drawScale(Graphics2D drumGraphics) {
		int sampleRate = ZejfSeis4.getDataManager().getSampleRate();

		drumGraphics.setFont(calibri12);
		wrx = drumGraphics.getFontMetrics().stringWidth((int) (sampleRate / 2 - 1) + ".0Hz") + 8;
		drumGraphics.setColor(Color.LIGHT_GRAY);
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, wrx, getHeight());
		drumGraphics.fill(rect);
		drumGraphics.setColor(Color.black);
		drumGraphics.draw(rect);

		double one = 2.0;
		double max = maxF;
		int h = getHeight();
		int w = getWidth();
		mainLoop: for (int n = 0; n < 10; n++) {
			for (int i = 0; i < ones.length; i++) {
				one = Math.pow(10, n) * ones[i];
				if (h * (one / max) > 20) {
					break mainLoop;
				}
			}
		}
		for (double v = one; v <= one * (int) (max / one); v += one) {
			if (v == max) {
				break;
			}
			double y1 = h - (h) * (v / (double) max);
			drumGraphics.setColor(Color.black);
			String str = (int) v + "Hz";
			int width = drumGraphics.getFontMetrics().stringWidth(str);
			drumGraphics.drawString(str, wrx - width - 2, (int) y1 + 4);
			drumGraphics.setColor(v == 0 ? Color.black : Color.LIGHT_GRAY);
			drumGraphics.setStroke(dashed);
			drumGraphics.drawLine(wrx + 1, (int) y1, w - 2, (int) y1);
		}
	}

	double[] magnitude;
	double[] valuesBuffer;

	long lastFFTLogID = -1;
	private Graphics2D spectroGraphics;

	int lastX = 0;
	long lastPosition = -1;
	private int lastWindow;
	private double[] fft;

	private void drawSpectroNew() {
		if (!ZejfSeis4.getDataManager().isLoaded()) {
			return;
		}
		long time = System.currentTimeMillis();
		long currentLogID = ZejfSeis4.getDataManager().getLogId(time);
		long firstLogID = currentLogID - dataRequest.getLogCount();
		long lastLogID = dataRequest.lastLogID;
		double GAIN = Settings.SPECTRO_GAIN;
		double sampleRate = ZejfSeis4.getDataManager().getSampleRate();

		int WINDOW = Settings.WINDOW;
		if (ifft == null || WINDOW != lastWindow) {
			ifft = new DoubleFFT_1D(WINDOW);
			fullRedrawNext = true;
		}
		
		lastWindow = WINDOW;

		if (fullRedrawNext || spectro == null) {
			lastX = -1;
			lastPosition = -1;
			lastFFTLogID = -1;
			magnitude = null;
			valuesBuffer = null;
			spectro = toCompatibleImage(new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR));
			spectroGraphics = spectro.createGraphics();
			spectroGraphics.setColor(scale[0]);
			spectroGraphics.fillRect(0, 0, getWidth(), getHeight());
			fullRedrawNext = false;
		}
		int h = spectro.getHeight();
		int w = spectro.getWidth();

		long position = ((time * w) / (ZejfSeis4.getDataManager().getMillis(dataRequest.getLogCount())));
		if (lastPosition != -1 && position > lastPosition) {
			int move = (int) Math.min(w, position - lastPosition);
			spectroGraphics.drawImage(spectro, -move, 0, null);
			lastX -= move;
			for (int x = Math.max(0, lastX + 1); x < w; x++) {
				long _logID = (long) (firstLogID + (currentLogID - firstLogID) * (x / (w - 1.0)));
				double val = dataRequest.getFilteredValue(ZejfSeis4.getDataManager().getMillis(_logID));
				if (val == ZejfSeis4.getDataManager().getErrVal()) {
					spectroGraphics.setColor(scale[0]);
					spectroGraphics.drawLine(x, 0, x, h);
				}
			}
		}

		if (valuesBuffer == null || valuesBuffer.length != WINDOW) {
			valuesBuffer = new double[WINDOW];
		}
		if (magnitude == null || magnitude.length != WINDOW / 2) {
			magnitude = new double[WINDOW / 2];
		}
		for (int x = Math.max(0, lastX+1); x < w; x++) {
			long _logID = (long) (firstLogID + (currentLogID - firstLogID) * (x / (w - 1.0)));
			if (_logID > lastLogID) {
				break;
			}
			boolean valuesChanged = false;
			for (long id = Math.max(_logID - WINDOW + 1, lastFFTLogID + 1); id <= _logID; id++) {
				double val = dataRequest.getFilteredValue(ZejfSeis4.getDataManager().getMillis(id));
				if (val == ZejfSeis4.getDataManager().getErrVal()) {
					clean(valuesBuffer);
					clean(magnitude);
				} else {
					shift(valuesBuffer, val);
					valuesChanged = true;
				}
				lastFFTLogID = id;

			}

			if (valuesChanged) {
				if (fft == null || fft.length != WINDOW * 2) {
					fft = new double[WINDOW * 2];
				} else {
					clean(fft);
				}
				for (int i = WINDOW - 1; i >= 0; i--) {
					fft[2 * i] = valuesBuffer[i];
					fft[2 * i + 1] = 0;
				}

				ifft.complexForward(fft);

				int mxx = WINDOW / 2 - 1;
				for (int i = 0; i < mxx; i++) {
					double re = fft[2 * i];
					double im = fft[2 * i + 1];
					magnitude[i] = Math.sqrt(re * re + im * im);
				}
			}

			for (int y = 0; y < h; y++) {
				Color color;
				double index =  (((h - y) / (double) h) * (magnitude.length - 1) * ((maxF * 2.0) / sampleRate));
				if (index < magnitude.length-1 && index >= 0) {
					double pct = index % 1.0;
					double v2 = y == 0 ? magnitude[magnitude.length - 1]
							: y == h - 1 ? magnitude[0]
									: magnitude[(int) index] * (1.0 - pct) + magnitude[(int) (index + 1)] * (pct);
					int col = (int) (Math.pow(v2 * 0.2, 1 / 2.0) * GAIN);
					color = SpectrogramPanel.scale[Math.max(0, Math.min(SpectrogramPanel.scale.length - 1, col))];
				} else {
					color = scale[0];
				}
				try {
					spectro.setRGB(x, y, color.getRGB());
				} catch (Exception ex) {
					System.err.println(x + ", " + (y));
				}
			}

			lastX = x;
		}

		lastPosition = position;
	}

	private void shift(double[] d, double val) {
		for (int i = 0; i < d.length - 1; i += 1) {
			d[i] = d[i + 1];
		}
		d[d.length - 1] = val;
	}

	private void clean(double[] d) {
		for (int i = 0; i < d.length; i += 1) {
			d[i] = 0.0;
		}
	}

	@Override
	public void updateDuration() {
		super.updateDuration();
		fullRedrawNext = true;
	}
	
}
