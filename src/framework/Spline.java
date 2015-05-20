package framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.media.opengl.GL2;


/** 
 * This class represents a curve loop defined by a sequence of control points.
 * 
 * @author Robert C. Duvall
 */
public class Spline implements Iterable<float[]> {
    private List<float[]> myControlPoints = new ArrayList<>();


    /**
     * Create empty curve.
     */
    public Spline (float[] controlPoints) {
        // BUGBUG: check that it is a multiple of 3
        for (int k = 0; k < controlPoints.length; k += 3) {
            addPoint(controlPoints[k], controlPoints[k+1], controlPoints[k+2]);
        }
    }

    /**
     * Create curve from the control points listed in the given file.
     */
    @SuppressWarnings("resource")
    public Spline (String filename) {
        try {
            Scanner input = new Scanner(new File(filename));
            input.nextLine();  // read starting comment
            while (input.hasNextLine()) {
                Scanner line = new Scanner(input.nextLine());
                addPoint(line.nextFloat(), line.nextFloat(), line.nextFloat());
            }
            input.close();
        } catch (FileNotFoundException e) {
            // BUGBUG: not the best way to handle this error
            e.printStackTrace();
            System.exit(1);
        }
    }

    public ArrayList<float[]> getControlPoints(){
        return (ArrayList<float[]>) myControlPoints;
    }

    public void setControlPoints(List<float[]> new_points) {
        myControlPoints = new_points;
    }

    /**
     * Add control point
     * 
     * @return index of new control point
     */
    public int addPoint (float x, float y, float z) {
        return addPoint(new float[] { x, y, z});
    }

    /**
     * Add control point
     * 
     * @return index of new control point
     */
    public int addPoint (float[] point) {
        myControlPoints.add(point);
        return myControlPoints.size() - 1;
    }

    /**
     * Evaluate a point on the curve at a given time.
     * 
     * Note, t varies from [0 .. 1] across a set of 4 control points and 
     * each set of 4 control points influences the curve within them. 
     * Thus a time value between [0 .. 1] generates a point within the first
     * 4 control points and a value between [n-2 .. n-1] generates a point
     * within the last 4 control points.
     * 
     * A time value outside the range [0 .. n] is wrapped, modded, so it 
     * falls within the appropriate range.
     */
    public float[] evaluateAt (float t) {
        int tn = (int)Math.floor(t);
        float u = t - tn;
        float u_sq = u * u;
        float u_cube = u * u_sq;
        // evaluate basis functions at t, faster than matrix multiply
        float[] basis = {
            -u_cube + 3*u_sq - 3*u + 1,
             3*u_cube - 6*u_sq + 4,
            -3*u_cube + 3*u_sq + 3*u + 1,
             u_cube
        };
        return evaluateBasisAt(tn, basis);
    }

    /**
     * Evaluate the derivative of the curve at a given time.
     * 
     * Note, t varies from [0 .. 1] across a set of 4 control points and 
     * each set of 4 control points influences the curve within them. 
     * Thus a time value between [0 .. 1] generates a derivative within the 
     * first 4 control points and a value between [n-2 .. n-1] generates a
     * derivative within the last 4 control points.
     * 
     * A time value outside the range [0 .. n] is wrapped, modded, so it 
     * falls within the appropriate range.
     */
    public float[] evaluateDerivativeAt (float t) {
        int tn = (int)Math.floor(t);
        float u = t - tn;
        float u_sq = u * u;
        // evaluate basis functions at t, faster than matrix multiply
        float[] basis = {
            -3*u_sq + 6*u - 3,
             9*u_sq - 12*u,
            -9*u_sq + 6*u + 3,
             3*u_sq
        };
        return evaluateBasisAt(tn, basis);
    }

    /**
     * Evaluate the second derivative of the curve at a given time.
     * 
     * Note, t varies from [0 .. 1] across a set of 4 control points and 
     * each set of 4 control points influences the curve within them. 
     * Thus a time value between [0 .. 1] generates a derivative within the 
     * first 4 control points and a value between [n-2 .. n-1] generates a
     * derivative within the last 4 control points.
     * 
     * A time value outside the range [0 .. n] is wrapped, modded, so it 
     * falls within the appropriate range.
     */
    public float[] evaluateSecondDerivativeAt (float t) {
        int tn = (int)Math.floor(t);
        float u = t - tn;
        // evaluate basis functions at t, faster than matrix multiply
        float[] basis = {
            -6*u + 6,
             18*u - 12,
            -18*u + 6,
             6*u
        };
        return evaluateBasisAt(tn, basis);
    }

    /**
     * Returns total number of control points around the curve.
     */
    public int numControlPoints () {
        return myControlPoints.size();
    }

    /**
     * Draws the curve as a sequence of lines as the given resolution.
     * 
     * The higher the resolution, the more lines generated, the closer 
     * the approximation to the actual curve.  A value of 1 will look 
     * like the points a connected linearly, with no curve, with smaller 
     * values giving better approximations.
     */
    public void draw (GL2 gl, float resolution) {
        gl.glBegin(GL2.GL_LINE_STRIP); {
            for (float t = 0; t < numControlPoints(); t += resolution) {
                gl.glVertex3fv(evaluateAt(t), 0);
            }
        }
        gl.glEnd();
    }

    /**
     * Draws control points around the curve as a collection of points.
     */
    public void drawControlPoints (GL2 gl) {
        gl.glBegin(GL2.GL_POINTS); {
            for (float[] pt : myControlPoints) {
                gl.glVertex3fv(pt, 0);
            }
        }
        gl.glEnd();
    }

    /**
     * Returns an iterator over the curve's control points, allowing the
     * user to directly iterate over them using a foreach loop.
     */
    @Override
    public Iterator<float[]> iterator () {
        return Collections.unmodifiableList(myControlPoints).iterator();
    }

    /**
     * Returns a string representation of the curve's control points.
     */
    @Override
    public String toString () {
        StringBuffer result = new StringBuffer();
        for (float[] pt : myControlPoints) {
            result.append(Arrays.toString(pt));
        }
        return result.toString();
    }
    
    // use the basis functions to evaluate a specific point on the curve
    private float[] evaluateBasisAt (int t, float[] basis) {
        // sum the control points times the basis functions for each dimension
        float[] result = { 0, 0, 0 };
        for (int k = 0; k < 4; k++) {
            int index = (t + k) % numControlPoints();
            result[0] += myControlPoints.get(index)[0] * basis[k];
            result[1] += myControlPoints.get(index)[1] * basis[k];
            result[2] += myControlPoints.get(index)[2] * basis[k];
        }
        // divide through the constant factor
        for (int k = 0; k < result.length; k++) {
            result[k] /= 6.0f;
        }
        return result;
    }
}
