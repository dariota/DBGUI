package GUI;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import backend.ErrorHandler;

//Displays an image in its own window
@SuppressWarnings("serial")
public class ImagePanel extends JPanel {

	private Image image;
	public int h;
	public int w;

    public ImagePanel(File im) {
       try {                
          image = ImageIO.read(im);
          BufferedImage bImage = ImageIO.read(im);
          h = bImage.getHeight();
          w = bImage.getWidth();
          Rectangle monitorDimensions = GraphicsEnvironment.getLocalGraphicsEnvironment()
        		  						.getScreenDevices()[0].getDefaultConfiguration().getBounds();
          int width = (int) monitorDimensions.getWidth();
          int height = (int) monitorDimensions.getHeight();
          boolean toScale = false;
          if (h > height) {
        	  float scale = height/(float)(h-50);
    		  w *= scale;
    		  h = height - 50;
    		  toScale = true;
          }
          if (w > width) {
        	  float scale = width/(float)(w - 50);
    		  h *= scale;
    		  w = width - 50;
    		  toScale = true;
          }
          if (toScale) 
        	  image = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
       } catch (IOException | IllegalArgumentException e) {
            ErrorHandler.error(e, "IP");
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);          
    }
    
    public int getWidth() {
    	return w;
    }
    
    public int getHeight() {
    	return h;
    }

}