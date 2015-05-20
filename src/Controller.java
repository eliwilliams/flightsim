import com.jogamp.opengl.util.gl2.GLUT;
import framework.Spline;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import java.util.ArrayList;

/**
 * Created by eli on 3/27/15.
 */
public class Controller {

    public Mesh grid;
    public Spline track;
    public int fromX;
    public int fromY;
    public int fromZ;
    public int toX;
    public int toY;
    public int toZ;
    public int upX;
    public int upY;
    public int upZ;
    public float spline_path;
    public float speed;
    public float roll;
    public float yaw;
    public float pitch;
    public float up;
    public float side;
    public float currentX;
    public float currentY;
    public float currentZ;
    public float scale;
    public float height_ratio;
    public float movement;
    public float min_x;
    public float min_y;
    public float min_z;
    public float max_y;
    public float max_z;

    public Controller(int fx, int fy, int fz, int tx, int ty, int tz,
                      int ux, int uy, int uz, float s, float hr, float width, float height, Mesh m, String path) {
        grid = m;
        track = new Spline(path);
        scale = 1.0f / s;
        height_ratio = 1.0f / hr;
        fromX = fx;
        fromY = fy;
        fromZ = fz;
        toX = tx;
        toY = ty;
        toZ = tz;
        upX = ux;
        upY = uy;
        upZ = uz;
        spline_path = 0.00f;
        speed = 0.05f;
        roll = 0.0f;
        yaw = 0.0f;
        pitch = 0.0f;
        up = 0.0f;
        side = 0.0f;
        movement = 0.0f;
        currentX = scale * fx;
        currentY = scale * height_ratio * fy;
        currentZ = scale * fz;
        min_x = -(width / 2);
        min_y = -1;
        min_z = -(height / 2);
        max_z = (height / 2);
        max_y = (-(min_x) + max_z) / 2;
//        max_y = 500f;
        trimControlPoints(scaleControlPoints(track.getControlPoints()), max_y - 5f);
    }

    public void adjustPosition(float x, float y, float z) {
        currentX += (scale * x);
        currentY += (scale * height_ratio * y);
        currentZ += (scale * z);
        checkDetection(currentX, currentY, currentZ);
    }

    public void trimControlPoints(ArrayList<float[]> trim, float y) {  // adjusts height for terrain collisions in mesh class
        track.setControlPoints(grid.fixControlPointHeights(trim, y));
    }

    public ArrayList<float[]> scaleControlPoints(ArrayList<float[]> pts) {
        float xscale = findScale(0, pts);
        float yscale = findScale(1, pts);
        float zscale = findScale(2, pts);
        ArrayList<float[]> scaled = new ArrayList<>();
        for(float[] p : pts) {
            scaled.add(new float[]{p[0] * xscale, p[1], p[2] * zscale});
        }
        return scaled;
    }

    public float findScale(int index, ArrayList<float[]> points) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for(float[] pt : points) {
            min = Math.min(min, pt[index]);
            max = Math.max(max, pt[index]);
        }
        float abs_max = (float) Math.max(max, Math.abs((double) min));
        float numerator;
        if(index == 0)
            numerator = -min_x;
        else if (index == 1)
            numerator = max_y;
        else
            numerator = max_z;
        return Math.abs(numerator / abs_max);
    }

    public void resetAll() {
        spline_path = 0.00f;
        speed = 0.05f;
        roll = 0.0f;
        yaw = 0.0f;
        pitch = 0.0f;
        movement = 0.0f;
        up = 0.0f;
        side = 0.0f;
        currentX = scale * fromX;
        currentY = scale * height_ratio * fromY;
        currentZ = scale * fromZ;
    }

    public void checkDetection(float x, float y, float z) {
        if(x < min_x || y < min_y || y > max_y || z < min_z || z > max_z) {
            resetAll();
        }
    }
}