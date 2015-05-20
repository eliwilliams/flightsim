import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import framework.JOGLFrame;
import framework.Pixmap;
import framework.Scene;

/**
 * Display a simple scene to demonstrate OpenGL.
 * 
 * @author Robert C. Duvall
 */
public class FlightSim extends Scene {
    private final String DEFAULT_MAP_FILE = "images/austrailia_topo.jpg";
    private static String[] TEXTURE_FILES = { "images/skybox_fr.rgb","images/skybox_lf.rgb",
            "images/skybox_rt.rgb","images/skybox_up.rgb","images/skybox_dn.rgb","images/skybox_bk.rgb"};
    private static String DEFAULT_CONTROL_POINTS = "tracks/QuadraPhase.trk";
    private final float HEIGHT_RATIO = 1.5f;
    private final int TERRAIN_ID = 1;
    private int skybox_mode;
    private final float myScale = 0.05f;
    private float resolution;
    private int myStepSize;
    private int myRenderMode;
    private boolean isCompiled;
    private boolean flat_shading;
    private boolean collision;
    private boolean spline_cam;
    private boolean spline_toggle;
    private boolean control_point_toggle;
    private Pixmap myHeightMap;
    private Mesh mesh;
    private Controller control;
    private Skybox box;
    private int LOD;

    public FlightSim (String[] args) {
        super("Flight Simulator");
        String name = (args.length > 1) ? args[0] : DEFAULT_MAP_FILE;
        try {
            myHeightMap = new Pixmap((args.length > 1) ? args[0] : DEFAULT_MAP_FILE);
        } catch (IOException e) {
            System.err.println("Unable to load texture image: " + name);
            System.exit(1);
        }
    }

    /**
     * Initialize general OpenGL values once (in place of constructor).
     */
    @Override
    public void init (GL2 gl, GLU glu, GLUT glut) {
        myStepSize = 16;
        isCompiled = false;
        flat_shading = false;
        collision = false;
        spline_toggle = false;
        spline_cam = false;
        control_point_toggle = false;
        LOD = 2;
        resolution = .1f;
        skybox_mode = 0;
        myRenderMode = GL2.GL_QUADS;
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glShadeModel(GL2.GL_SMOOTH);
        box = new Skybox(gl, glu, glut, TEXTURE_FILES, myHeightMap.getSize().width, myHeightMap.getSize().height);
//        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_DIFFUSE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        mesh = new Mesh(LOD, Math.ceil((double) myHeightMap.getSize().width / (double) myStepSize),
                Math.ceil((double) myHeightMap.getSize().height / (double) myStepSize));
        setTerrain();
        control = new Controller((int) ((float) myHeightMap.getSize().width / (2.0f / myScale)),
                (int) (((float) myHeightMap.getSize().height / (2.0f / myScale / HEIGHT_RATIO))
                        + ((float) myHeightMap.getSize().width / (2.0f / myScale)) / 24.0f), 0,
                (int) ((float) -myHeightMap.getSize().width / (2.0f / myScale)),
                0, 0, 0, 1, 0, myScale, HEIGHT_RATIO, ((float) myHeightMap.getSize().width),
                myHeightMap.getSize().height, mesh, DEFAULT_CONTROL_POINTS);
    }

    /**
     * Draw all of the objects to display.
     */
    @Override
    public void display (GL2 gl, GLU glu, GLUT glut) {
        if (!isCompiled) {
            gl.glDeleteLists(TERRAIN_ID, 1);
            gl.glNewList(TERRAIN_ID, GL2.GL_COMPILE);
            if(skybox_mode == 0)
                box.drawSides(gl, glu, glut);
            else if(skybox_mode == 1)
                box.drawWireFrame(gl, glu, glut);
            if (mesh.checkIntersect(control.currentX, control.currentY, control.currentZ)) {
                if(!spline_cam) {      // the checkIntersect method does properly check for collisions when riding the track, too
                    collision = true;
                    control.resetAll();
                }
            }
            drawTerrain(gl, glu, glut);
            if (spline_toggle) {
                gl.glColor3f(1.0f, 0.0f, 0.0f);
                control.track.draw(gl, resolution);
            }
            if (control_point_toggle) {
                gl.glColor3f(0.0f, 0.0f, 1.0f);
                gl.glPointSize(5.0f);
                control.track.drawControlPoints(gl);
            }
            gl.glEndList();
            isCompiled = true;
        }
        gl.glRotatef(control.pitch, 0.0f, 0.0f, 1.0f);
        gl.glRotatef(control.roll, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(control.yaw, 0.0f, 1.0f, 0.0f);
        gl.glTranslatef(control.movement, control.up, control.side);
        gl.glScalef(myScale, myScale * HEIGHT_RATIO, myScale);
        gl.glCallList(TERRAIN_ID);
    }

    /**
     * Animate the scene by changing its state slightly.
     */
    @Override
    public void animate (GL2 gl, GLU glu, GLUT glut) {
        if(!spline_cam) {
            control.movement += control.speed;
            control.adjustPosition(-control.speed, 0.0f, 0.0f);
        }
        else {
            control.spline_path += control.speed;
        }
        isCompiled = false;
    }

    /**
     * Set the camera's view of the scene.
     */
    @Override
    public void setCamera(GL2 gl, GLU glu, GLUT glut) {
        float fx, fy, fz, tx, ty, tz;
        if(!spline_cam) {
            fx = control.fromX;
            fy = control.fromY;
            fz = control.fromZ;
            tx = control.toX;
            ty = control.toY;
            tz = control.toZ;
        }
        else {
            float[] pos = control.track.evaluateAt(control.spline_path);
            float[] der = control.track.evaluateDerivativeAt(control.spline_path);
            control.currentX = pos[0];
            control.currentY = pos[1];
            control.currentZ = pos[2];
            fx = pos[0] * myScale;
            fy = pos[1] * myScale * HEIGHT_RATIO;
            fz = pos[2] * myScale;
            tx = fx + (der[0] * myScale);
            ty = fy + (der[1] * myScale * HEIGHT_RATIO);
            tz = fz + (der[2] * myScale);
        }
        glu.gluLookAt(fx, fy, fz, // from position
                tx, ty, tz,   // to position
                control.upX, control.upY, control.upZ);  // up direction
    }

    /**
     * Establish lights in the scene.
     */
    @Override
    public void setLighting (GL2 gl, GLU glu, GLUT glut) {
        float[] light0pos = { 0, 350, 0, 1 };
        float[] light0dir = { 0, -1, 0, 0 };
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0pos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, light0dir, 0);
//        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 20);
    }

    /**
     * Called when any key is pressed within the canvas.
     */
    @Override
    public void keyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_T:   // reset animation
                control.resetAll();
                break;
            case KeyEvent.VK_UP:   // speed up flying
                control.speed+=0.1f;
                isCompiled = false;
                break;
            case KeyEvent.VK_DOWN:   // slow down flying
                control.speed-=0.1f;
                isCompiled = false;
                break;
            case KeyEvent.VK_E:   //  move up
                control.up-=2f;
                control.adjustPosition(0.0f, 2.0f, 0.0f);
                isCompiled = false;
                break;
            case KeyEvent.VK_D:   //  move down
                control.up+=2f;
                control.adjustPosition(0.0f, -2.0f, 0.0f);
                isCompiled = false;
                break;
            case KeyEvent.VK_S:  // move left
                control.side-=0.5f;
//                control.adjustPosition(0.3f, 0.0f, 0.0f);
                control.adjustPosition(0.0f, 0.0f, 0.5f);
                isCompiled = false;
                break;
            case KeyEvent.VK_F:  // move right
                control.side+=0.5f;
//                control.adjustPosition(-0.3f, 0.0f, 0.0f);
                control.adjustPosition(0.0f, 0.0f, -0.5f);
                isCompiled = false;
                break;
            case KeyEvent.VK_R:   //  pitch down
                control.pitch-=1.5f;
                break;
            case KeyEvent.VK_W:   //  pitch up
                control.pitch+=1.5f;
                break;
            case KeyEvent.VK_RIGHT:  // yaw right
                control.yaw-=1.5f;
                break;
            case KeyEvent.VK_LEFT:   // yaw left
                control.yaw+=1.5f;
                break;
            case KeyEvent.VK_G:   // roll right
                control.roll+=1.5f;
                break;
            case KeyEvent.VK_A:   // roll left
                control.roll-=1.5f;
                break;
            case KeyEvent.VK_C:   // change rendering mode
                myRenderMode = ((myRenderMode == GL2.GL_QUADS) ? GL2.GL_LINES : GL2.GL_QUADS);
                isCompiled = false;
                break;
            case KeyEvent.VK_V:   // change shading mode
                flat_shading = !flat_shading;
                isCompiled = false;
                break;
            case KeyEvent.VK_X:   // toggle skybox mode (normal, wireframe, hidden)
                skybox_mode++;
                skybox_mode %= 3;
                isCompiled = false;
                break;
            case KeyEvent.VK_M:  // toggle drawing of spline path
                spline_toggle = !spline_toggle;
                isCompiled = false;
                break;
            case KeyEvent.VK_N:  // toggle following of spline path
                control.resetAll();
                spline_cam = !spline_cam;
                isCompiled = false;
                break;
            case KeyEvent.VK_B:  // toggle drawing control points
                control_point_toggle = !control_point_toggle;
                isCompiled = false;
                break;
            case KeyEvent.VK_OPEN_BRACKET:
                if (myStepSize > 4)
                    myStepSize /= 2;
                isCompiled = false;
                break;
            case KeyEvent.VK_CLOSE_BRACKET:
                if (myStepSize < (myHeightMap.getSize().width / 2))
                    myStepSize *= 2;
                isCompiled = false;
                break;
        }
    }

    private void setTerrain () {
        int width = myHeightMap.getSize().width;
        int height = myHeightMap.getSize().height;
        for (int X = 0; X < width; X += myStepSize) {
            for (int Y = 0; Y < height; Y += myStepSize) {
                    // set (x, y, z) value for bottom left vertex
                    float x0 = X - width / 2.0f;
                    float y0 = myHeightMap.getColor(X, Y).getRed();
                    float z0 = Y - height / 2.0f;
                    Vertex bl = new Vertex(x0, y0, z0, 0);
                    // set (x, y, z) value for top left vertex
                    float x1 = x0;
                    float y1 = myHeightMap.getColor(X, Y + myStepSize).getRed();
                    float z1 = z0 + myStepSize;
                    Vertex tl = new Vertex(x1, y1, z1, 0);
                    // set (x, y, z) value for top right vertex
                    float x2 = x0 + myStepSize;
                    float y2 = myHeightMap.getColor(X + myStepSize, Y + myStepSize).getRed();
                    float z2 = z0 + myStepSize;
                    Vertex tr = new Vertex(x2, y2, z2, 0);
                    // set (x, y, z) value for bottom right vertex
                    float x3 = x0 + myStepSize;
                    float y3 = myHeightMap.getColor(X + myStepSize, Y).getRed();
                    float z3 = z0;
                    Vertex br = new Vertex(x3, y3, z3, 0);
                    // set normal vector for face
                    float nx = (y0 - y1) * (z0 + z1) + (y1 - y2) * (z1 + z2) + (y2 - y3) * (z2 + z3) + (y3 - y0) * (z3 + z0);
                    float ny = (z0 - z1) * (x0 + x1) + (z1 - z2) * (x1 + x2) + (z2 - z3) * (x2 + x3) + (z3 - z0) * (x3 + x0);
                    float nz = (x0 - x1) * (y0 + y1) + (x1 - x2) * (y1 + y2) + (x2 - x3) * (y2 + y3) + (x3 - x0) * (y3 + y0);
                    Vertex norm = new Vertex(nx, ny, nz, 0);

                    Face f = new Face();
                    f.addVertex(0, bl);
                    f.addVertex(1, tl);
                    f.addVertex(2, tr);
                    f.addVertex(3, br);
                    f.makeHitbox(myStepSize);
                    f.setNormal(norm);
                    mesh.addFace(0, f, X / myStepSize, Y / myStepSize);
            }
        }

        for(int i = 0; i <= LOD; i++) {
            if(i == 0) {
                mesh.findFaceNeighbors(i);
                mesh.makeVertexNeighbors(i);
                mesh.makeAllMidpoints(i);
                mesh.assignColors(i);
                i++;
            }
            mesh.spawnChildren(i);
            mesh.findFaceNeighbors(i);
            mesh.makeVertexNeighbors(i);
            mesh.assignColors(i);
        }
    }

    private void drawTerrain (GL2 gl, GLU glu, GLUT glut) {
        gl.glBegin(myRenderMode);
        {
            for (int x = 0; x < mesh.getGridWidth(0); x++) {
                for (int y = 0; y < mesh.getGridHeight(0); y++) {
                    Face display = mesh.getFace(0, x, y);
//                    if(display.getDistance(control.currentX, control.currentY, control.currentZ) >= 0.0f &&
//                            display.getDistance(control.currentX, control.currentY, control.currentZ) < 500.0f) {
//                        recursivelyDrawChildren(gl, 3, display);
//                    }
//                    else
                    if(display.getDistance(control.currentX, control.currentY, control.currentZ) >= 0.0f &&
                            display.getDistance(control.currentX, control.currentY, control.currentZ) < 1000.0f) {
                        recursivelyDrawChildren(gl, 2, display);
                    }
                    else if(display.getDistance(control.currentX, control.currentY, control.currentZ) >= 1000.0f &&
                            display.getDistance(control.currentX, control.currentY, control.currentZ) < 2000.0f) {
                        recursivelyDrawChildren(gl, 1, display);
                    }
                    else {
                        recursivelyDrawChildren(gl, 0, display);
                    }
                }
            }
        }
        gl.glEnd();
    }

    private void recursivelyDrawChildren(GL2 gl, int LOD, Face display) {
        if(display.getLOD() == LOD) {
            drawFace(gl, display);
            return;
        }
        else {
            for (int i = 0; i < 4; i++) {
                int[] coords = display.getChild(i);
                Face child = mesh.getFace(display.getLOD() + 1, coords[0], coords[1]);
                recursivelyDrawChildren(gl, LOD, child);
            }
        }
    }

    private void drawFace (GL2 gl, Face display) {
        gl.glBegin(myRenderMode);
        {
            for(int d = 0; d < 4; d++) {
                Vertex v = display.getVertex(d);
//                if(!collision) {
                    gl.glColor3f(v.getColor(0), v.getColor(1), v.getColor(2));
//                }
//                else {
//                    gl.glColor3f(1.0f, 0.0f, 0.0f);
//                }
                if(!flat_shading) {
                    gl.glNormal3f(v.getNormX(), v.getNormY(), v.getNormZ());
                }
                else if(d == 0) {
                    gl.glNormal3f(display.getNormal().getX(), display.getNormal().getY(), display.getNormal().getZ());
                }
                gl.glVertex3f(v.getX(), v.getY(), v.getZ());
            }
        }
        gl.glEnd();
    }

    // allow program to be run from here
    public static void main (String[] args) {
        new JOGLFrame(new FlightSim(args));
    }
}