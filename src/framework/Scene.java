package framework;

import java.awt.Dimension;
import java.awt.Point;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;


/**
 * A class that highlights the most useful methods for you to write to create an OpenGL scene.
 * 
 * You should subclass this to do your actual work.
 *
 * @author Robert C. Duvall
 */
public abstract class Scene {
    // title of the scene
    private String myTitle = "";
    private Dimension mySize = new Dimension();


    /**
     * Create scene with the given title.
     *
     * @param title displayed on the top of the Window
     */
    public Scene () {
        myTitle = getClass().getSimpleName();
    }

    /**
     * Create scene with the given title.
     *
     * @param title displayed on the top of the Window
     */
    public Scene (String title) {
        myTitle = title;
    }

    /**
     * Initialize global OpenGL state.
     *
     * For example, setting lighting or texture parameters
     *
     * @param gl basic interface to OpenGL
     * @param glu basic interface to GLU
     * @param glut basic interface to GLUT
     */
    public void init (GL2 gl, GLU glu, GLUT glut) {
        // by default, do nothing
    }

    /**
     * Display complete scene.
     *
     * This is called whenever the contents of the window need to be redrawn.
     *
     * @param gl basic interface to OpenGL
     * @param glu basic interface to GLU
     * @param glut basic interface to GLUT
     */
    public abstract void display (GL2 gl, GLU glu, GLUT glut);

    /**
     * Establish camera's view of the scene.
     *
     * This is called to get the current camera's position.
     *
     * @param gl basic interface to OpenGL
     * @param glu basic interface to GLU
     * @param glut basic interface to GLUT
     */
    public abstract void setCamera (GL2 gl, GLU glu, GLUT glut);

    /**
     * Establish the lights in the scene.
     * 
     * Note, there can only be 8 lights in addition to the ambient light.
     * 
     * @param gl basic interface to OpenGL
     * @param glu basic interface to GLU
     * @param glut basic interface to GLUT
     */
    public void setLighting (GL2 gl, GLU glu, GLUT glut) {
        // by default, do nothing
    }

    /**
     * Animate scene by making small changes to its state.
     *
     * For example, changing the absolute position or rotation angle of an object.
     *
     * @param gl basic interface to OpenGL
     * @param glu basic interface to GLU
     * @param glut basic interface to GLUT
     */
    public void animate (GL2 gl, GLU glu, GLUT glut) {
        // by default, do nothing
    }

    /**
     * Get the title of the scene.
     *
     * @return title of scene
     */
    public String getTitle () {
        return myTitle;
    }

    /**
     * Get the size of the scene's window.
     *
     * @return size of scene's window
     */
    public Dimension getWindowSize () {
        return mySize;
    }

    /**
     * Called when the mouse is pressed within the canvas and it hits something.
     */
    public void selectObject (GL2 gl, GLU glu, GLUT glut, int numSelected, int[] selectInfo) {
        // by default, do nothing
    }

    /**
     * Respond to the press of a key.
     *
     * @param keyCode Java code representing pressed key
     */
    public void keyPressed (int keyCode) {
        // by default, do nothing
    }

    /**
     * Respond to the release of a key.
     *
     * @param keyCode Java code representing released key
     */
    public void keyReleased (int keyCode) {
        // by default, do nothing
    }

    /**
     * Respond to the press and release of an alphanumeric key.
     *
     * @param key text representing typed key
     */
    public void keyTyped (int keyCode) {
        // by default, do nothing
    }

    /**
     * Respond to the press and release of the mouse.
     *
     * @param pt current position of the mouse
     * @param button mouse button that was clicked
     */
    public void mouseClicked (Point pt, int button) {
        // by default, do nothing
    }

    /**
     * Respond to the press of the mouse.
     *
     * @param pt current position of the mouse
     * @param button mouse button that was pressed
     */
    public void mousePressed (Point pt, int button) {
        // by default, do nothing
    }

    /**
     * Respond to the release of the mouse.
     *
     * @param pt current position of the mouse
     * @param button mouse button that was released
     */
    public void mouseReleased (Point pt, int button) {
        // by default, do nothing
    }

    /**
     * Respond to the mouse being moved while the button is pressed.
     *
     * @param pt current position of the mouse
     * @param button mouse button that is being held down
     */
    public void mouseDragged (Point pt, int button) {
        // by default, do nothing
    }

    /**
     * Respond to the mouse being moved in the canvas.
     *
     * @param pt current position of the mouse
     */
    public void mouseMoved (Point pt) {
        // by default, do nothing
    }

    /**
     * Set the size of the scene's window.
     */
    void setWindowSize (int width, int height) {
        mySize.setSize(width, height);
    }
}
