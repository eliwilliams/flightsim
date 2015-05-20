package framework;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JFrame;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;


/**
 * Simply creates a frame for viewing OpenGL application in Java.
 *
 * This file should only need to be modified if extra user interface capabilities are required.
 *
 * @author Robert C. Duvall
 */
@SuppressWarnings("serial")
public class JOGLFrame extends JFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(600, 600);
    public static final int FPS = 25;

    public JOGLFrame (Scene scene) {
        this(scene, DEFAULT_SIZE);
    }

    public JOGLFrame (Scene scene, Dimension size) {
        // create OpenGL classes
        // if you need something specific to your platform, add it here
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        // let stencil buffer know the pixel format 
        caps.setStencilBits(8);
        // these should remain pretty much fixed for all applications
        GLJPanel canvas = new GLJPanel(caps);
        final AnimatorBase animator = new FPSAnimator(canvas, FPS);
        Listener listener = new Listener(scene, animator, size);
        // manage OpenGL canvas
        canvas.addGLEventListener(listener);
        canvas.addKeyListener(listener);
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.setPreferredSize(size);
        // create titled window to view animation
        JFrame frame = new JFrame(listener.getTitle());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent e) {
                shutDown(animator);
            }
        });
        frame.getContentPane().add(canvas);
        // set to full screen mode
        //frame.setUndecorated(true);
        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
        // start thread to drive animation
        animator.start();
        // allow for key events
        canvas.requestFocus();
    }

    private void shutDown (final AnimatorBase animator) {
        // Run this on another thread than the AWT event queue to
        // make sure the call to Animator.stop() completes before
        // exiting (otherwise causes locks on some systems).
        new Thread(new Runnable() {
            @Override
            public void run () {
                animator.stop();
                System.exit(0);
            }
        }).start();
    }
}
