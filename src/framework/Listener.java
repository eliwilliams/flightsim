package framework;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.common.nio.Buffers;


/**
 * This class serves as mediator between your scene code and the boilerplate OpenGL code that needs
 * to be written. It calls your scene methods at the appropriate time.
 *
 * It may be updated from time to time to add functionality.
 * 
 * Based on this excellent tutorial:
 *   https://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html
 *
 * @author Robert C. Duvall
 */
public class Listener implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {
    // constants
    public static long ONE_SECOND = 1000;

    // user's scene to animate and display
    private Scene myScene;
    // animation state
    private AnimatorBase myAnimator;
    private int myFrameCount;
    private long myLastFrameTime;
    private double myFPS;
    private boolean isRunning;
    private boolean showFPS;
    // interaction state
    private Point myMousePoint;
    private float myPixelFactor;
    // cache creation of these objects
    private static GLU glu = new GLU();
    private static GLUT glut = new GLUT();


    /**
     * Create this listener with the arguments given on the command-line and the animation thread.
     *
     * @param args command-line arguments
     * @param animator animation thread
     */
    public Listener (Scene scene, AnimatorBase animator, Dimension size) {
        myScene = scene;
        myScene.setWindowSize(size.width, size.height);
        myAnimator = animator;
        isRunning = true;
        myFrameCount = 0;
        myLastFrameTime = System.currentTimeMillis();
        myFPS = 0;
        showFPS = false;
        myPixelFactor = 1;
    }

    /**
     * Get the title of the scene.
     *
     * @return title of scene
     */
    public String getTitle () {
        return myScene.getTitle();
    }

    // //////////////////////////////////////////////////////////
    // GLEventListener methods
    /**
     * Called once immediately after the OpenGL context is initialized.
     *
     * @see GLEventListener#init(GLAutoDrawable)
     */
    @Override
    public void init (GLAutoDrawable drawable) {
        // get graphics context
        GL2 gl = drawable.getGL().getGL2();
        // is this a hi-res screen?
        myPixelFactor = (float)myScene.getWindowSize().width / drawable.getSurfaceWidth();
        // interesting?
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
        System.err.println("GL_CLASS: " + gl.getClass().getName());
        // set to draw in window based on depth
        gl.glEnable(GL2.GL_DEPTH_TEST);
        // start scene
        myScene.init(gl, glu, glut);
    }

    /**
     * Called repeatedly to render the OpenGL scene.
     *
     * @see GLEventListener#display(GLAutoDrawable)
     */
    @Override
    public void display (GLAutoDrawable drawable) {
        // get graphics context
        GL2 gl = drawable.getGL().getGL2();
        // check for interaction
        if (myMousePoint != null) {
            selectObject(gl, glu, glut, myMousePoint);
            myMousePoint = null;
        }
        // update scene for this time step
        myScene.animate(gl, glu, glut);
        // clear the drawing surface
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        // display model
        gl.glPushMatrix(); {
            myScene.setCamera(gl, glu, glut);
            myScene.setLighting(gl, glu, glut);
            myScene.display(gl, glu, glut);
        }
        gl.glPopMatrix();
        // display frame rate
        computeFPS();
    }

    /**
     * Called immediately after the component has been resized
     *
     * @see GLEventListener#reshape(GLAutoDrawable, int, int, int, int)
     */
    @Override
    public void reshape (GLAutoDrawable drawable, int x, int y, int width, int height) {
        myScene.setWindowSize((int)(width * myPixelFactor), (int)(height * myPixelFactor));
        // reset camera based on new viewport
        setPerspective(drawable.getGL().getGL2(), glu, GL2.GL_RENDER, null);
    }

    /**
     * Called when the display mode or the display device has changed.
     *
     * @see GLEventListener#displayChanged(GLAutoDrawable, boolean, boolean)
     */
    public void displayChanged (GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        // not generally used
    }

    /**
     * Called when the display is closed.
     *
     * @see GLEventListener#dispose(GLAutoDrawable)
     */
    @Override
    public void dispose (GLAutoDrawable drawable) {
        // not generally used
    }

    // //////////////////////////////////////////////////////////
    // KeyListener methods
    /**
     * Called when any key is pressed within the canvas.
     */
    @Override
    public void keyPressed (KeyEvent e) {
        // pass event onto user's code
        myScene.keyPressed(e.getKeyCode());
    }

    /**
     * Called when any key is released within the canvas.
     */
    @Override
    public void keyReleased (KeyEvent e) {
        switch (e.getKeyCode()) {
          // toggle animation running
          case KeyEvent.VK_Z:
            showFPS = !showFPS;
            break;
          // toggle animation running
          case KeyEvent.VK_P:
            isRunning = !isRunning;
            if (isRunning) {
                myAnimator.start();
            } else {
                myAnimator.stop();
            }
            break;
          // quit the program
          case KeyEvent.VK_ESCAPE:
          case KeyEvent.VK_Q:
            myAnimator.stop();
            System.exit(0);
            break;
          // pass event onto user's code
          default:
            myScene.keyReleased(e.getKeyCode());
        }
    }

    /**
     * Called when standard alphanumeric keys are pressed and released within the canvas.
     */
    @Override
    public void keyTyped (KeyEvent e) {
        // by default, do nothing
    }

    // //////////////////////////////////////////////////////////
    // MouseListener methods
    @Override
    public void mouseClicked (MouseEvent e) {
        myMousePoint = e.getPoint();
    }

    @Override
    public void mouseEntered (MouseEvent e) {
        // by default, do nothing
    }

    @Override
    public void mouseExited (MouseEvent e) {
        // by default, do nothing
    }

    @Override
    public void mousePressed (MouseEvent e) {
        myScene.mousePressed(e.getPoint(), e.getButton());
    }

    @Override
    public void mouseReleased (MouseEvent e) {
        myScene.mouseReleased(e.getPoint(), e.getButton());
    }

    @Override
    public void mouseDragged (MouseEvent e) {
        myScene.mouseDragged(e.getPoint(), e.getButton());
    }

    @Override
    public void mouseMoved (MouseEvent e) {
        myScene.mouseMoved(e.getPoint());
    }

    // //////////////////////////////////////////////////////////
    // helper methods
    /**
     * Reset perspective matrix based on size of viewport.
     */
    private void setPerspective (GL2 gl, GLU glu, int mode, Point pt) {
        // get info about viewport (x, y, w, h)
        int[] viewport = new int[4];
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
        // scale for hi-res displays
        viewport[2] = (int)(viewport[2] * myPixelFactor);
        viewport[3] = (int)(viewport[3] * myPixelFactor);
        // set camera to view viewport area
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // check for selection
        if (mode == GL2.GL_SELECT) {
            // create 5x5 pixel picking region near cursor location
            glu.gluPickMatrix(pt.x, viewport[3]-pt.y, 5.0f, 5.0f, viewport, 0);
        }
        // view scene in perspective
        glu.gluPerspective(45.0f, (float)viewport[2]/viewport[3], 0.1f, 500.0f);
        // prepare to work with model again
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    /**
     * Determine which objects have been selected by pressing the mouse
     */
    private void selectObject (GL2 gl, GLU glu, GLUT glut, Point mousePt) {
        final int BUFFER_SIZE = 256;
        IntBuffer selectionBuffer = Buffers.newDirectIntBuffer(BUFFER_SIZE);
        gl.glSelectBuffer(selectionBuffer.capacity(), selectionBuffer);
        // prepare for selection by initializing name info (0 represents a miss)
        gl.glRenderMode(GL2.GL_SELECT);
        gl.glInitNames();
        gl.glPushName(0);
        // render to select buffer instead of color buffer
        gl.glPushMatrix(); {
            setPerspective(gl, glu, GL2.GL_SELECT, mousePt);
            myScene.setCamera(gl, glu, glut);
            myScene.display(gl, glu, glut);
        }
        gl.glPopMatrix();

        // if object hit, react
        int numHits = gl.glRenderMode(GL2.GL_RENDER);
        if (numHits > 0) {
            int[] buffer = new int[BUFFER_SIZE];
            selectionBuffer.get(buffer);
            myScene.selectObject(gl, glu, glut, numHits, buffer);
        }
        // reset camera for viewing
        setPerspective(gl, glu, GL2.GL_RENDER, null);
    }

    /*
     * Compute and print frames per second of animation
     */
    private double computeFPS () {
        myFrameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - myLastFrameTime > ONE_SECOND) {
            myFPS = myFrameCount * ONE_SECOND / (double) (currentTime - myLastFrameTime);
            myLastFrameTime = currentTime;
            if (showFPS) {
                System.out.printf("%3.2f\n", myFPS);
            }
            myFrameCount = 0;
        }
        return myFPS;
    }
}
