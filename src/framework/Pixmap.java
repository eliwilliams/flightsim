package framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


/**
 * Convenience class for dealing with images.
 *
 * Creating a pixmap requires a filename that should be a gif, png, or jpg image.
 *
 * @author Robert C. Duvall
 * @author Owen Astrachan
 * @author Syam Gadde
 */
public class Pixmap {
    public static final Dimension DEFAULT_SIZE = new Dimension(300, 300);
    public static final Color DEFAULT_COLOR = Color.BLACK;
    public static final String DEFAULT_NAME = "Default";

    private String myFileName;
    private BufferedImage myImage;
    private Dimension mySize;


    /**
     * Create a pixmap with given width and height and filled with given initial color
     */
    public Pixmap (int width, int height, Color color) {
        createImage(width, height, color);
    }

    /**
     * Create a pixmap from the given local file
     */
    public Pixmap (String fileName) throws IOException {
        if (fileName == null) {
            createImage(DEFAULT_SIZE.width, DEFAULT_SIZE.height, DEFAULT_COLOR);
        } else {
            read(fileName);
        }
    }

    /**
     * Returns the file name of this Pixmap (if it exists)
     */
    public String getName () {
        int index = myFileName.lastIndexOf(File.separator);
        if (index >= 0) {
            return myFileName.substring(index + 1);
        } else {
            return myFileName;
        }
    }

    /**
     * Returns the dimensions of the Pixmap.
     */
    public Dimension getSize () {
        return new Dimension(mySize);
    }

    /**
     * Returns the color of the pixel at the given point in the pixmap.
     */
    public Color getColor (int x, int y) {
        if (isInBounds(x, y)) {
            return new Color(myImage.getRGB(x, y));
        } else {
            return DEFAULT_COLOR;
        }
    }

    /**
     * Paint the image on the canvas
     */
    public void paint (Graphics pen) {
        pen.drawImage(myImage, 0, 0, mySize.width, mySize.height, null);
    }

    // returns true if the given point is a valid Pixmap value.
    private boolean isInBounds (int x, int y) {
        return (0 <= x && x < mySize.width) && (0 <= y && y < mySize.height);
    }

    // Read the pixmap from the given file.
    private void read (String fileName) throws IOException {
        myFileName = fileName;
        myImage = ImageIO.read(new File(myFileName));
        mySize = new Dimension(myImage.getWidth(), myImage.getHeight());
    }

    // convenience function
    private void createImage (int width, int height, Color color) {
        myFileName = color.toString();
        myImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        mySize = new Dimension(width, height);
    }
}
