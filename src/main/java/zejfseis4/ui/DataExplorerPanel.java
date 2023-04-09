package zejfseis4.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import zejfseis4.data.DataRequest;
import zejfseis4.events.Intensity;
import zejfseis4.main.Settings;
import zejfseis4.main.ZejfSeis4;
import zejfseis4.scale.Scales;
import zejfseis4.ui.renderer.ScaleRenderer;
import zejfseis4.utils.TravelTimeTable;

public class DataExplorerPanel extends DataRequestPanel {

	private static final long serialVersionUID = 1L;
	protected long originalStart;
	protected long originalEnd;
	protected long start;
	protected long end;

	private static final int CHART = 1;
	private static final int SPECTRO = 2;
	private static final int FFT = 3;
	private static final int HORIZONTAL_SCALE = 5;

	private static final long MAXIMUM_DURATION = 24 * 60 * 60 * 1000l;

	private int MODE = 0;

	private int lastMode;
	private int wrx;
	protected long dragStart = -1;
	protected long dragEnd;
	private DoubleFFT_1D ifft;
	protected boolean dragging;
	private boolean fullRedrawNext;
	private int lastMouseX;
	protected long pWaveTime = -1;
	protected long pkpWaveTime = -1;
	protected long sWaveTime = -1;
	public long sfcStart;
	public long sfcEnd;
	public boolean enableSelecting;
	private int lastMouseY;

	private static int statusPanelHeight = 20;

	public DataExplorerPanel(long start, long end) {
		if (start >= end) {
			long a = start;
			start = end;
			end = a;
		}

		if (end - start >= MAXIMUM_DURATION) {
			throw new IllegalArgumentException("Too long!");
		}

		setRequest(new DataRequest(ZejfSeis4.getDataManager(), "DataExplorer", start, end) {
			@Override
			public void onRefill(boolean isRealtime) {
				if (!isRealtime) {
					generateResults();
					fullRedrawNext = true;
					repaint();
				}
			}
		});

		this.originalStart = start;
		this.originalEnd = end;
		this.start = start;
		this.end = end;
		this.MODE = CHART;
		setLayout(null);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && lastMode != FFT) {
					dragging = true;
					int x = e.getX();
					int width = getWidth();
					long _start = getStart();
					long _end = getEnd();
					long time = (long) (_start + (_end - _start) * ((x - wrx) / (double) (width - wrx)));
					dragStart = time;
					dragEnd = time;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					MODE++;
					if (MODE > FFT) {
						MODE = CHART;
					}
					fullRedrawNext = true;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && lastMode != FFT) {
					dragging = false;
					zoom();
					dragStart = -1;
					fullRedrawNext = true;
					repaint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && lastMode != FFT && enableSelecting) {
					long _start = getStart();
					long _end = getEnd();
					int x = e.getX();
					int width = getWidth();
					long clickedTime = (long) (_start + (_end - _start) * ((x - wrx) / (double) (width - wrx)));
					if (pWaveTime == -1) {
						pWaveTime = clickedTime;
					} else if (sWaveTime == -1 && clickedTime > pWaveTime) {
						sWaveTime = clickedTime;
					} else {
						pWaveTime = -1;
						sWaveTime = -1;
					}
					wavesSelected();
				}
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				lastMouseX = e.getX();
				lastMouseY = e.getY();
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				lastMouseX = e.getX();
				lastMouseY = e.getY();
				if (dragging) {
					int x = e.getX();
					int width = getWidth();
					long _start = getStart();
					long _end = getEnd();
					long time = (long) (_start + (_end - _start) * ((x - wrx) / (double) (width - wrx)));
					dragEnd = time - 1;
					repaint();
				}
			}
		});

	}

	public void wavesSelected() {

	}

	@Override
	public void paintComponent(Graphics g) {
		drawExplorer((Graphics2D) g);
	}

	class Result {
		double[] magnitudes;
		long windowID;
		boolean broken;

		public double getMag(double freqD) {
			freqD = 1 - freqD;
			if (freqD >= 1) {
				return magnitudes[magnitudes.length - 1];
			} else if (freqD <= 0) {
				return magnitudes[0];
			}
			double _i = (freqD) * (magnitudes.length - 1);
			double a = magnitudes[(int) _i];
			double b = magnitudes[(int) (_i + 1)];
			return a * (1 - _i % 1) + b * (_i % 1);
		}
	}

	private ArrayList<Result> results;

	private double lastGain;
	private int lastWindow;
	private int max;

	protected synchronized void generateResults() {
		if (results == null) {
			results = new ArrayList<DataExplorerPanel.Result>();
		}
		int CURRENT_WINDOW = Settings.WINDOW;
		int CURRENT_WINDOW_SCALED = Settings.WINDOW / HORIZONTAL_SCALE;

		double GAIN = Settings.SPECTRO_GAIN;

		int sampleRate = ZejfSeis4.getDataManager().getSampleRate();
		int err = ZejfSeis4.getDataManager().getErrVal();

		long startLogID = (getStart() * sampleRate) / 1000;
		long startWindowIDScaled = startLogID / CURRENT_WINDOW_SCALED;

		long endLogID = (getEnd() * sampleRate) / 1000;
		long endWindowIDScaled = endLogID / CURRENT_WINDOW_SCALED;
		boolean fullRedraw = true;

		if (fullRedraw) {
			System.out.println("RESET");
			results.clear();
		}

		if (CURRENT_WINDOW != lastWindow) {
			ifft = new DoubleFFT_1D(CURRENT_WINDOW);
		}

		for (long windowID = startWindowIDScaled; windowID <= endWindowIDScaled; windowID++) {
			long firstID = (windowID + 1 - HORIZONTAL_SCALE) * CURRENT_WINDOW_SCALED;
			long lastID = (windowID + 1) * CURRENT_WINDOW_SCALED - 1;
			double[] values = new double[CURRENT_WINDOW];
			double[] magnitude = new double[values.length];
			boolean broken = false;
			for (long logID = firstID; logID <= lastID; logID++) {
				double v = dataRequest.getFilteredValue((logID * 1000) / sampleRate);
				if (v == err) {
					broken = true;
					break;
				} else {
					for (int i = 0; i < values.length - 1; i++) {
						values[i] = values[i + 1];
					}
					values[values.length - 1] = v;
				}
			}
			double[] fft = new double[CURRENT_WINDOW * 2];
			magnitude = new double[CURRENT_WINDOW / 2];
			for (int i = CURRENT_WINDOW - 1; i >= 0; i--) {
				fft[2 * i] = values[i];
				fft[2 * i + 1] = 0;
			}

			ifft.complexForward(fft);

			int mxx = CURRENT_WINDOW / 2 - 1;
			for (int i = 0; i < mxx; i++) {
				double re = fft[2 * i];
				double im = fft[2 * i + 1];
				magnitude[i] = Math.sqrt(re * re + im * im);
			}

			Result res = new Result();
			res.broken = broken;
			res.windowID = windowID;
			res.magnitudes = magnitude;
			results.add(res);
		}
		System.err.println(results.size() + " RESULTS");
		lastWindow = CURRENT_WINDOW;
		lastGain = GAIN;
		if (fullRedrawNext) {
			fullRedrawNext = false;
		}
	}

	@Deprecated
	public double getMagOld(long time, double freqD) {
		if (results == null || results.isEmpty()) {
			return 0;
		}
		// int CURRENT_WINDOW_SCALED = Settings.WINDOW / HORIZONTAL_SCALE;
		int secs = 1000 / ZejfSeis4.getDataManager().getSampleRate();
		long startTime = (getStart() / secs) * secs;
		long endTime = (getEnd() / secs) * secs;

		double _i = ((time - startTime) / (double) (endTime - startTime)) * (results.size() - 1);

		if (_i <= 0) {
			return results.get(0).getMag(freqD);
		} else if (_i >= 1) {
			return results.get(results.size() - 1).getMag(freqD);
		}

		Result a = results.get((int) _i);
		Result b = results.get((int) (_i + 1));
		double vA = a.getMag(freqD);
		double vB = b.getMag(freqD);
		return vA * (1 - _i % 1) + vB * (_i % 1);
	}

	protected void zoom() {
		if (dragStart > dragEnd) {
			start = originalStart;
			end = originalEnd;
			repaint();
		} else if (dragEnd - dragStart > 100) {
			dragStart = Math.max(dragStart, originalStart);
			dragEnd = Math.min(dragEnd, originalEnd);
			start = dragStart;
			end = dragEnd;
			repaint();
		}
	}

	private void drawExplorer(Graphics2D g) {
		int width = getWidth();
		int height = getHeight();
		int thisMode = MODE;
		if (width == 0) {
			System.err.println("w=0");
			return;
		}

		switch (thisMode) {
		case CHART:
			drawChart(width, height);
			break;
		case SPECTRO:
			drawSpectro(width, height);
			break;
		case FFT:
			drawFFT(width, height);
			break;
		default:
			break;
		}
		if (explorer != null) {
			g.drawImage(explorer, 0, 0, null);
			drawStatusPanel(width, height, g);
		}

		if (thisMode == CHART) {
			drawIntensity(width, height, g);
		}

		if (dragStart != -1) {
			if (dragStart < dragEnd) {
				double x1 = wrx + (width - wrx) * ((dragStart - start) / (double) (end - start));
				double x2 = wrx + (width - wrx) * ((dragEnd - start) / (double) (end - start));
				g.setColor(new Color(127, 127, 127, 127));
				g.fill(new Rectangle2D.Double(x1, 0, x2 - x1, height));
			} else {
				double x1 = wrx + (width - wrx) * ((dragStart - start) / (double) (end - start));
				double x2 = wrx + (width - wrx) * ((dragEnd - start) / (double) (end - start));
				g.setColor(new Color(255, 0, 0, 70));
				g.fill(new Rectangle2D.Double(x2, 0, x1 - x2, height));
			}
		}
		lastMode = thisMode;
	}

	private static SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.ENGLISH);
	@SuppressWarnings("unused")
	private static SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH);

	private void drawStatusPanel(int width, int height, Graphics2D graphics) {
		Rectangle2D.Double rect = new Rectangle2D.Double(0, height - statusPanelHeight, width, statusPanelHeight);
		graphics.setStroke(new BasicStroke(1f));
		graphics.setColor(new Color(230, 230, 230));
		graphics.fill(rect);
		graphics.setColor(Color.black);
		graphics.draw(rect);
		if (MODE != FFT) {

			long mouseTime = (long) (start + (end - start) * ((lastMouseX - wrx) / (double) (width - wrx)));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(mouseTime);

			graphics.setColor(Color.black);
			graphics.setFont(calibri12);
			graphics.drawString(dateFormat1.format(c.getTime()), 5, height - 5);

			graphics.setColor(Color.black);
			graphics.setStroke(dashed);
			graphics.drawLine(Math.max(wrx, lastMouseX), 1, Math.max(wrx, lastMouseX), height - statusPanelHeight);
			graphics.drawLine(wrx, lastMouseY, width, lastMouseY);

			if (pWaveTime > 0) {
				graphics.setStroke(new BasicStroke(2f));
				graphics.setColor(Color.BLACK);
				int px = wrx + (int) ((width - wrx) * ((pWaveTime - start) / (double) (end - start)));
				graphics.drawLine(px, 2, px, height - statusPanelHeight);
				graphics.setFont(new Font("Calibri", Font.BOLD, 18));
				graphics.drawString("P", px + 3, 16);
				// graphics.setFont(new Font("Calibri", Font.BOLD, 14));
				// graphics.drawString(dateFormat2.format(pWaveTime), px + 3, height -
				// statusPanelHeight - 6);
			}
			if (sWaveTime > 0) {
				graphics.setStroke(new BasicStroke(2f));
				graphics.setColor(Color.RED);
				int sx = wrx + (int) ((width - wrx) * ((sWaveTime - start) / (double) (end - start)));
				graphics.drawLine(sx, 2, sx, height - statusPanelHeight);
				graphics.setFont(new Font("Calibri", Font.BOLD, 18));
				graphics.drawString("S", sx + 3, 16);
				// graphics.setFont(new Font("Calibri", Font.BOLD, 14));
				// graphics.drawString(dateFormat2.format(sWaveTime), sx + 3, height -
				// statusPanelHeight - 6);
			}
			if (pkpWaveTime > 0) {
				graphics.setStroke(new BasicStroke(2f));
				graphics.setColor(new Color(0, 200, 0));
				int px = wrx + (int) ((width - wrx) * ((pkpWaveTime - start) / (double) (end - start)));
				graphics.drawLine(px, 2, px, height - statusPanelHeight);
				graphics.setFont(new Font("Calibri", Font.BOLD, 18));
				graphics.drawString("PKP", px + 3, 16);
				// graphics.setFont(new Font("Calibri", Font.BOLD, 14));
				// graphics.drawString(dateFormat2.format(pkpWaveTime), px + 3, height -
				// statusPanelHeight - 6);
			}

			if (pWaveTime > 0 && sWaveTime > 0) {
				graphics.setColor(Color.black);
				graphics.setFont(calibri12);
				String str = "dT = " + dt(sWaveTime - pWaveTime) + ", d = "
						+ f3d.format(TravelTimeTable.getEpicenterDistance(0, (sWaveTime - pWaveTime) / 1000.0)) + "km";
				graphics.drawString(str, width - 6 - graphics.getFontMetrics().stringWidth(str), height - 5);
			}

			if (sfcEnd > 0 && sfcStart > 0 && MODE == CHART) {
				int x1 = wrx + (int) ((width - wrx) * ((sfcStart - start) / (double) (end - start)));
				int x2 = wrx + (int) ((width - wrx) * ((sfcEnd - start) / (double) (end - start)));
				graphics.setStroke(new BasicStroke(1f));
				graphics.setColor(new Color(255, 0, 255, 50));
				graphics.fillRect(x1, 1, x2 - x1, height - statusPanelHeight);
			}

		}
	}

	private static DecimalFormat f3d = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.ENGLISH));

	private static String dt(long l) {
		long min = (l / (1000 * 60));
		String str = "";
		if (min > 0) {
			str = min + " min, ";
		}
		return str + f3d.format((l % (1000 * 60)) / 1000.0) + "s";
	}

	int extraWrx = 14;

	private void drawIntensity(int w, int h, Graphics2D g) {
		if (mouse) {
			int intensity = (int) (((h * 0.5 - statusPanelHeight * 0.5) - mouseY) / (h * 0.5 - statusPanelHeight * 0.5)
					* max);

			int r = 8;
			Rectangle2D rect = new Rectangle2D.Double(extraWrx + 1, mouseY - r, wrx - extraWrx - 2, r * 2);
			g.setStroke(new BasicStroke(1f));
			Color intensityColor;
			g.setColor(intensityColor = Intensity.get(intensity).getColor());
			g.fill(rect);
			g.setColor(Color.black);
			g.draw(rect);
			g.setColor(ScaleRenderer.foregroundColor(intensityColor));
			g.setFont(calibri12);
			String str = String.format("%,d", (int) intensity);
			int width = g.getFontMetrics().stringWidth(str);
			g.drawString(str, wrx - width - 2, (int) mouseY + 5);
		}
	}

	private void drawChart(int w, int h) {
		if (w == 0 || h == 0) {
			return;
		}

		boolean fullRedraw = fullRedrawNext || explorer == null || explorer.getWidth() != w
				|| explorer.getHeight() != h;

		if (fullRedraw) {
			if (fullRedrawNext) {
				fullRedrawNext = false;
			}
			if (explorerGraphics != null) {
				explorerGraphics.dispose();
			}
			explorer = null;
			explorer = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			explorerGraphics = explorer.createGraphics();
			explorerGraphics.setColor(Color.white);
			explorerGraphics.fillRect(0, 0, w, h);
		} else {
			return;
		}

		int secs = ZejfSeis4.getDataManager().getSampleTime();
		int err = ZejfSeis4.getDataManager().getErrVal();
		int sampleRate = ZejfSeis4.getDataManager().getSampleRate();
		long graphEnd = getEnd();
		long graphStart = getStart();
		// System.out.println(new Date(graphStart)+", "+new Date(graphEnd));
		explorerGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				Settings.ANTIALIAS ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		int max = 12;

		for (long time = graphStart; time <= graphEnd; time += secs) {
			double val = dataRequest.getFilteredValue(time);
			if (val != err) {
				double x = (w) * ((time - graphStart) / (double) (graphEnd - graphStart));
				double v = Math.abs(val) * 1.25;
				if (x >= 16 && v > max) {
					max = (int) v;
				}
			}
		}

		System.out.println("MAX=" + max);

		int wrx = explorerGraphics.getFontMetrics().stringWidth(String.format("%,d", -max)) + 6 + extraWrx;
		this.max = max;
		explorerGraphics.setFont(calibri12);
		this.wrx = wrx;

		explorerGraphics.setColor(Color.white);
		explorerGraphics.fillRect(0, 0, w, h);
		explorerGraphics.setColor(Color.lightGray);
		explorerGraphics.fillRect(0, 0, wrx, h - 1);
		for (int i = Intensity.values().length - 1; i >= 0; i--) {
			double intensity = Intensity.getIntensity(i);
			double y0 = (h * 0.5 - statusPanelHeight * 0.5)
					- (h * 0.5 - statusPanelHeight * 0.5) * (intensity / (double) max);
			double y1 = (h * 0.5 - statusPanelHeight * 0.5)
					- (h * 0.5 - statusPanelHeight * 0.5) * (-intensity / (double) max);
			Color color = Intensity.values()[i].getColor();
			Rectangle2D rect = new Rectangle2D.Double(1, y0, extraWrx - 1, y1 - y0);
			explorerGraphics.setColor(color);
			explorerGraphics.fill(rect);
			explorerGraphics.setColor(Color.black);
			explorerGraphics.draw(rect);
		}
		explorerGraphics.setColor(Color.black);
		explorerGraphics.drawRect(0, 0, extraWrx, h);
		explorerGraphics.setColor(Color.black);
		explorerGraphics.drawRect(0, 0, w - 1, h - 1);
		explorerGraphics.drawRect(0, 0, wrx, h - 1);

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
			double y1 = (h * 0.5 - statusPanelHeight * 0.5) - (h * 0.5 - statusPanelHeight * 0.5) * (v / (double) max);
			explorerGraphics.setColor(Color.black);
			explorerGraphics.setFont(calibri12);
			String str = String.format("%,d", (int) v);
			int width = explorerGraphics.getFontMetrics().stringWidth(str);
			explorerGraphics.drawString(str, wrx - width - 2, (int) y1 + 4);
			explorerGraphics.setColor(v == 0 ? Color.black : Color.LIGHT_GRAY);
			explorerGraphics.drawLine(wrx + 1, (int) y1, w - 2, (int) y1);

		}

		// drumGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		double lastV = 0;
		long lastT = -1;
		// System.out.println(dataRequest.lastLogTime);
		for (long t = graphStart; t <= graphEnd; t += secs) {
			double v = dataRequest.getFilteredValue(t);
			if (lastT == -1 || t - lastT > ((2 * 1000) / sampleRate)) {
				lastT = t;
				continue;
			}

			if (v == err || lastV == err) {
				lastT = t;
				lastV = v;
				continue;
			}

			double x1 = wrx + (w - wrx) * ((lastT - graphStart) / (double) (graphEnd - graphStart));
			double x2 = wrx + (w - wrx) * ((t - graphStart) / (double) (graphEnd - graphStart));
			double y1 = (h * 0.5 - statusPanelHeight * 0.5)
					- (h * 0.5 - statusPanelHeight * 0.5) * (lastV / (double) max);
			double y2 = (h * 0.5 - statusPanelHeight * 0.5) - (h * 0.5 - statusPanelHeight * 0.5) * (v / (double) max);
			if (x1 > wrx && x2 > wrx) {
				explorerGraphics.setColor(Color.blue);
				if (line1 == null) {
					line1 = new Line2D.Double(x1, y1, x2, y2);
				} else {
					line1.setLine(x1, y1, x2, y2);
				}
				explorerGraphics.draw(line1);
			}
			lastT = t;
			lastV = v;
		}

		// System.out.println(max+", "+k);
		explorerGraphics.setColor(Color.black);
		explorerGraphics.drawRect(0, 0, w - 1, h - 1);

	}

	private synchronized void drawSpectro(int w, int h) {
		boolean fullRedraw = fullRedrawNext || explorer == null || explorer.getWidth() != w
				|| explorer.getHeight() != h;

		if (fullRedraw) {
			if (fullRedrawNext) {
				fullRedrawNext = false;
			}
			if (explorerGraphics != null) {
				explorerGraphics.dispose();
			}
			explorer = null;
			explorer = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			explorerGraphics = explorer.createGraphics();
			explorerGraphics.setColor(Color.white);
			explorerGraphics.fillRect(0, 0, w, h);

		} else {
			return;
		}
		explorerGraphics.setColor(Scales.MAG.getColor(0));
		explorerGraphics.fillRect(0, 0, w, h);
		long begin = getStart();
		int sampleRate = ZejfSeis4.getDataManager().getSampleRate();
		if (results == null) {
			return;
		}

		explorerGraphics.setFont(calibri12);
		int wrx = explorerGraphics.getFontMetrics().stringWidth("20Hz") + 8;
		this.wrx = wrx;

		for (Result res : results) {
			if (res.broken) {
				continue;
			}
			long startTime = ((res.windowID * lastWindow / HORIZONTAL_SCALE) * 1000) / sampleRate;
			long endTime = (((res.windowID + 1) * lastWindow / HORIZONTAL_SCALE/*-1*/) * 1000) / sampleRate;
			// no wrx because is is behind
			double startX = wrx + (startTime - begin) / (double) (getEnd() - getStart()) * (w - wrx - 1);
			double endX = wrx + (endTime - begin) / (double) (getEnd() - getStart()) * (w - wrx - 1);
			for (int i = 0; i < res.magnitudes.length; i++) {
				double startY = (h - statusPanelHeight) * (i / (double) res.magnitudes.length);
				double endY = (h - statusPanelHeight) * ((i + 1) / (double) res.magnitudes.length);
				double mag = res.magnitudes[res.magnitudes.length - 1 - i];
				double col = Math.pow(mag * 0.2, 1 / 2.0);
				Color color = Scales.SPECTRO.getColor(col * lastGain);
				Rectangle2D rect = new Rectangle2D.Double(startX, startY, endX - startX, endY - startY);
				explorerGraphics.setColor(color);
				explorerGraphics.fill(rect);
			}
		}
		drawScale(explorerGraphics, w, h);

	}

	private void drawScale(Graphics2D drumGraphics, int w, int h) {
		explorerGraphics.setFont(calibri12);
		drumGraphics.setColor(Color.LIGHT_GRAY);
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, wrx, getHeight());
		drumGraphics.fill(rect);
		drumGraphics.setColor(Color.black);
		drumGraphics.draw(rect);

		int sampleRate = ZejfSeis4.getDataManager().getSampleRate();

		double one = 2.0;
		double max = sampleRate / 2.0;
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
			double y1 = (h - statusPanelHeight) - (h - statusPanelHeight) * (v / (double) max);
			drumGraphics.setColor(Color.black);
			String str = (int) v + "Hz";
			int width = drumGraphics.getFontMetrics().stringWidth(str);
			drumGraphics.drawString(str, wrx - width - 2, (int) y1 + 4);
			drumGraphics.setColor(v == 0 ? Color.black : Color.LIGHT_GRAY);
			drumGraphics.setStroke(dashed);
			drumGraphics.drawLine(wrx + 1, (int) y1, w - 2, (int) y1);
		}
	}

	public static BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
			new float[] { 3 }, 0);

	private void drawFFT(int w, int h) {
		boolean fullRedraw = fullRedrawNext || explorer == null || explorer.getWidth() != w
				|| explorer.getHeight() != h;

		if (fullRedraw) {
			if (fullRedrawNext) {
				fullRedrawNext = false;
			}
			if (explorerGraphics != null) {
				explorerGraphics.dispose();
			}
			explorer = null;
			explorer = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			explorerGraphics = explorer.createGraphics();
			explorerGraphics.setColor(Color.white);
			explorerGraphics.fillRect(0, 0, w, h);
		} else {
			return;
		}
		int wry = 20;
		int wrx;

		int secs = ZejfSeis4.getDataManager().getSampleTime();
		int err = ZejfSeis4.getDataManager().getErrVal();
		int sampleRate = ZejfSeis4.getDataManager().getSampleRate();

		explorerGraphics.setFont(new Font("Calibri", Font.BOLD, 14));
		wrx = explorerGraphics.getFontMetrics().stringWidth("1e-1") + 6;
		explorerGraphics.setColor(Color.white);
		explorerGraphics.fillRect(0, 0, w, h);
		explorerGraphics.setColor(Color.black);
		explorerGraphics.drawRect(0, 0, w - 1, h - 1);
		explorerGraphics.drawRect(wrx, 0, w - wrx, h - wry - statusPanelHeight);
		for (int p = -2; p <= 1; p++) {
			for (int n = 1; n < 10; n++) {
				double freq = n * Math.pow(10, p);
				double x = wrx + (Math.log10(freq * 100) / 4) * (w - wrx);
				explorerGraphics.setColor(n == 1 ? Color.black : Color.blue);
				explorerGraphics.setStroke(n == 1 ? new BasicStroke(2) : dashed);
				explorerGraphics.draw(new Line2D.Double(x, 0, x, h - wry - statusPanelHeight));
				if (n == 1) {
					explorerGraphics.setColor(Color.black);
					explorerGraphics.setFont(calibri14);
					String str = freq + " Hz";
					int width = explorerGraphics.getFontMetrics().stringWidth(str);
					explorerGraphics.drawString(str, (int) (x - width * 0.5), h - wry - statusPanelHeight + 15);
				}
			}
		}

		int SIZE = (int) ((getEnd() - getStart()) / (secs));
		double[] data = new double[SIZE];
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getStart());
		double[] magnitude = new double[SIZE / 2];
		int i = 0;
		while (true) {// LOAD DATA
			double v1 = dataRequest.getFilteredValue(c.getTimeInMillis());
			if (v1 == err) {
				v1 = 0;
			}
			data[i] = v1;
			i++;
			c.add(Calendar.MILLISECOND, secs);
			if (i == SIZE) {
				break;
			}
		}
		double[] fft = new double[SIZE * 2];
		for (i = 0; i <= SIZE - 1; i++) {
			fft[2 * i] = data[i];
			fft[2 * i + 1] = 0;
		}

		DoubleFFT_1D ifft = new DoubleFFT_1D(SIZE);
		ifft.complexForward(fft);
		double maxMag = 0;

		for (i = 0; i < SIZE / 2 - 1; i++) {
			double re = fft[2 * i];
			double im = fft[2 * i + 1];
			double mag = Math.sqrt(re * re + im * im);
			magnitude[i] = mag;
			if (mag > maxMag) {
				maxMag = mag;
			}
		}

		int maxP = (int) Math.ceil(Math.log10(maxMag));
		for (int p = -1; p <= maxP; p++) {
			for (int n = 1; n < 10; n++) {
				double mag = n * Math.pow(10, p);
				double y = (h - wry - statusPanelHeight)
						- (h - wry - statusPanelHeight) * (Math.log10(mag * 10) / (maxP + 1));
				explorerGraphics.setColor(n == 1 ? Color.black : Color.blue);
				explorerGraphics.setStroke(n == 1 ? new BasicStroke(2) : dashed);
				explorerGraphics.draw(new Line2D.Double(wrx, y, w, y));
				if (n == 1) {
					explorerGraphics.setColor(Color.black);
					explorerGraphics.setFont(new Font("Calibri", Font.BOLD, 14));
					String str = 1 + "e" + p;
					int width = explorerGraphics.getFontMetrics().stringWidth(str);
					explorerGraphics.drawString(str, (int) (wrx - width - 3), (int) (y + 4));
				}
			}
		}
		explorerGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int j = 0; j < SIZE / 2 - 1; j++) {
			double mag1 = magnitude[j];
			double mag2 = magnitude[j + 1];
			double freq1 = sampleRate * 0.5 * (j / (SIZE / 2.0));
			double freq2 = sampleRate * 0.5 * ((j + 1) / (SIZE / 2.0));
			if (freq1 < 0.01) {
				continue;
			}
			double x1 = j == 0 ? wrx : wrx + (Math.log10(freq1 * 100) / 4) * (w - wrx);
			double x2 = wrx + (Math.log10(freq2 * 100) / 4) * (w - wrx);
			double y1 = (h - wry - statusPanelHeight)
					- (h - wry - statusPanelHeight) * (Math.log10(mag1 * 10) / (maxP + 1));
			double y2 = (h - wry - statusPanelHeight)
					- (h - wry - statusPanelHeight) * (Math.log10(mag2 * 10) / (maxP + 1));
			explorerGraphics.setColor(Color.RED);
			explorerGraphics.setStroke(new BasicStroke(2f));
			explorerGraphics.draw(new Line2D.Double(x1, y1, x2, y2));
		}
		System.out.println("Computed " + SIZE + ", " + maxMag);
	}

	private int[] ones = new int[] { 1, 2, 5, 10 };

	private static final Font calibri12 = new Font("Calibri", Font.BOLD, 12);
	private static final Font calibri14 = new Font("Calibri", Font.BOLD, 14);
	private Double line1;
	private Graphics2D explorerGraphics;
	private BufferedImage explorer;

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public void close() {
		ZejfSeis4.getDataManager().unregisterDataRequest(dataRequest);
	}

}
