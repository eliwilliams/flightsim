import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Created by eli on 3/22/15.
 */
public class Skybox {

    private Texture [] skybox;
    private String [] filenames;
    private float[][] coords;
    private float x;
    private float y;
    private float z;

    public Skybox(GL2 gl, GLU glu, GLUT glut, String[] names, float w, float h) {
        filenames = names;
        skybox = new Texture[6];
        this.coords = new float[8][3];
        x = w / 2.0f;
        z = h / 2.0f;
        y = (x + z) / 2;
//        y = 500f;
        loadTextures(gl);
        makeCoords();
        drawSides(gl, glu, glut);
    }

    public void loadTextures(GL2 gl) {
        skybox = new Texture[6];
        for(int i = 0; i < filenames.length; i++) {
            skybox[i] = makeTexture(gl, filenames[i]);
        }
    }

    private Texture makeTexture(GL2 gl, String name) {
        try {
            File f = new File(name);
            Texture result = TextureIO.newTexture(new FileInputStream(f), false, TextureIO.SGI_RGB);
            return result;
        } catch (IOException e) {
            System.err.println("Unable to load texture image: " + name);
            System.exit(1);
            return null;
        }
    }

    public void makeCoords() {
        this.coords = new float[][]{{-x, 0.1f, z}, {-x, y, z}, {-x, y, -z}, {-x, 0.1f, -z}, {x, 0.1f, z},
                                 {x, y, z}, {x, y, -z}, {x, 0.1f, -z}};
    }

    public void drawSides(GL2 gl, GLU glu, GLUT glut) {
        bindTextures(gl, glu, glut, coords[0], coords[3], coords[2], coords[1], skybox[0]);  // front
        bindTextures(gl, glu, glut, coords[4], coords[0], coords[1], coords[5], skybox[1]);  // left
        bindTextures(gl, glu, glut, coords[3], coords[7], coords[6], coords[2], skybox[2]);  // right
        bindTextures(gl, glu, glut, coords[1], coords[2], coords[6], coords[5], skybox[3]);  // up
        bindTextures(gl, glu, glut, coords[4], coords[7], coords[3], coords[0], skybox[4]);  // down
        bindTextures(gl, glu, glut, coords[4], coords[7], coords[6], coords[5], skybox[5]);  // back
    }

    public void drawWireFrame(GL2 gl, GLU glu, GLUT glut) {
        drawCube(gl, glu, glut, coords[0], coords[3], coords[2], coords[1]);  // front
        drawCube(gl, glu, glut, coords[4], coords[0], coords[1], coords[5]);  // left
        drawCube(gl, glu, glut, coords[3], coords[7], coords[6], coords[2]);  // right
        drawCube(gl, glu, glut, coords[1], coords[2], coords[6], coords[5]);  // up
        drawCube(gl, glu, glut, coords[4], coords[7], coords[3], coords[0]);  // down
        drawCube(gl, glu, glut, coords[4], coords[7], coords[6], coords[5]);  // back
    }

    public void drawCube(GL2 gl, GLU glu, GLUT glut, float[] bl, float[] br, float[] tr, float[] tl) {
        gl.glBegin(GL2.GL_LINES);
        {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
            gl.glVertex3f(bl[0], bl[1], bl[2]);
            gl.glVertex3f(br[0], br[1], br[2]);
            gl.glVertex3f(tr[0], tr[1], tr[2]);
            gl.glVertex3f(tl[0], tl[1], tl[2]);
        }
        gl.glEnd();
    }

    public void bindTextures(GL2 gl, GLU glu, GLUT glut, float[] bl, float[] br, float[] tr, float[] tl, Texture t) {
            t.enable(gl);
            t.bind(gl);

            gl.glBegin(GL2.GL_QUADS);
            {
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glTexCoord2f(0, 0);
                gl.glVertex3f(bl[0], bl[1], bl[2]);
                gl.glTexCoord2f(1, 0);
                gl.glVertex3f(br[0], br[1], br[2]);
                gl.glTexCoord2f(1, 1);
                gl.glVertex3f(tr[0], tr[1], tr[2]);
                gl.glTexCoord2f(0, 1);
                gl.glVertex3f(tl[0], tl[1], tl[2]);
            }
            gl.glEnd();
    }
}