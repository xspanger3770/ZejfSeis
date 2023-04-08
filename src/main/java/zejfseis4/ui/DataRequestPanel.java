package zejfseis4.ui;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import zejfseis4.data.DataRequest;
import zejfseis4.main.Settings;
import zejfseis4.main.ZejfSeis4;

public abstract class DataRequestPanel extends JPanel {

	private static final long serialVersionUID = -2607304431215829261L;
	public DataRequest dataRequest;

	public boolean mouse = false;
	public int mouseY = 0;
	public int mouseX = 0;

	public DataRequestPanel() {
		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				mouse = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouse = false;
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseY = e.getY();
				mouseX = e.getX();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseY = e.getY();
				mouseX = e.getX();
			}
		});
	}

	public void setRequest(DataRequest dataRequest) {
		this.dataRequest = dataRequest;
		ZejfSeis4.getDataManager().registerDataRequest(dataRequest);
	}

	public void updateFilter() {
		DataRequest dataRequest = getDataRequest();
		synchronized (dataRequest.dataMutex) {
			dataRequest.resetFilter();
			dataRequest.refill();
		}

	}

	public void updateDuration() {
		DataRequest dataRequest = getDataRequest();
		synchronized (dataRequest.dataMutex) {
			dataRequest.changeDuration(Settings.REALTIME_DURATION_SECONDS * 1000);
		}
	}

	public DataRequest getDataRequest() {
		return dataRequest;
	}
	
	public BufferedImage toCompatibleImage(BufferedImage image)
	{
	    // obtain the current system graphical settings
	    GraphicsConfiguration gfxConfig = GraphicsEnvironment.
	        getLocalGraphicsEnvironment().getDefaultScreenDevice().
	        getDefaultConfiguration();

	    /*
	     * if image is already compatible and optimized for current system 
	     * settings, simply return it
	     */
	    if (image.getColorModel().equals(gfxConfig.getColorModel()))
	        return image;

	    // image is not optimized, so create a new image that is
	    BufferedImage newImage = gfxConfig.createCompatibleImage(
	            image.getWidth(), image.getHeight(), image.getTransparency());

	    // get the graphics context of the new image to draw the old image on
	    Graphics2D g2d = newImage.createGraphics();

	    // actually draw the image and dispose of context no longer needed
	    g2d.drawImage(image, 0, 0, null);
	    g2d.dispose();

	    // return the new optimized image
	    return newImage; 
	}


}
