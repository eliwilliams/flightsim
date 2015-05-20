import java.util.ArrayList;

/**
 * Created by eli on 3/6/15.
 */
public class Vertex {

    private float [] points;
    private float [] normal;
    private float [] color;
    private float id;
    private int lod;
    private ArrayList<Face> neighbors;

    public Vertex(float x, float y, float z, int l) {
        points = new float[3];
        points[0] = x;
        points[1] = y;
        points[2] = z;
        normal = new float[3];
        normal[0] = 0;
        normal[1] = 0;
        normal[2] = 0;
        color = new float[3];
        lod = l;
        id = makeID();
        neighbors = new ArrayList<>();
    }

    public Vertex(Vertex v) {
        points = new float[3];
        points[0] = v.getX();
        points[1] = v.getY();
        points[2] = v.getZ();
        normal = new float[3];
        normal[0] = v.getNormX();
        normal[1] = v.getNormY();
        normal[2] = v.getNormZ();
        color = new float[3];
        lod = v.getLOD() + 1;
        id = makeID();
        neighbors = new ArrayList<>();
    }

    public int getLOD() {
        return lod;
    }

    public float makeID() {
        float i = (lod + 1) * ((2.5f * points[0] * points[0]) + (1.7f * points[1] * points[1]) + (3.1f * points[2] * points[2]));
        return i;
    }

    public float[] getAllPoints() {
        return points;
    }

    public float getID() {
        return id;
    }

    public void setID(float i) { id = i; }

    public float getX () {
        return points[0];
    }

    public float getY () {
        return points[1];
    }

    public float getZ () {
        return points[2];
    }

    public void setX (float d) {
        points[0] = d;
    }

    public void setY (float d) {
        points[1] = d;
    }

    public void setZ (float d) {
        points[2] = d;
    }

    public float getNormX () {
        return normal[0];
    }

    public float getNormY () {
        return normal[1];
    }

    public float getNormZ () {
        return normal[2];
    }

    public void setNormX (float d) {
        normal[0] = d;
    }

    public void setNormY (float d) {
        normal[1] = d;
    }

    public void setNormZ (float d) {
        normal[2] = d;
    }

    public void makeNormalFromNeighbors () {
        for(Face f: neighbors) {
            this.setNormX(this.getNormX() + f.getNormal().getX());
            this.setNormY(this.getNormY() + f.getNormal().getY());
            this.setNormZ(this.getNormZ() + f.getNormal().getZ());
        }
        normalize();
    }

    public void normalize () {
        double l = (normal[0] * normal[0]) + (normal[1] * normal[1]) + (normal[2] * normal[2]);
        float length = (float) Math.sqrt(l);
        this.setNormX(normal[0] / length);
        this.setNormY(normal[1] / length);
        this.setNormZ(normal[2] / length);
    }

    public void addNeighbor(Face f) {
        if(!neighbors.contains(f) && (f.getVertex(0).getID() == this.getID() || f.getVertex(1).getID() == this.getID() ||
                f.getVertex(2).getID() == this.getID() || f.getVertex(3).getID() == this.getID())) {
            neighbors.add(f);
        }
    }

    public void setColor(int i) {
//        if(i == 0) {
//            this.color[0] = 0.125f;
//            this.color[1] = 0.66f;
//            this.color[2] = 0.66f;
//        }
        if(i == 1) {
            this.color[0] = 0.44f;
            this.color[1] = 0.36f;
            this.color[2] = 0.24f;
        }
        if(i == 2 || i == 0) {
            this.color[0] = 1.0f;
            this.color[1] = 1.0f;
            this.color[2] = 1.0f;
        }
    }

    public float getColor(int i) {
        return color[i];
    }

    public ArrayList<Face> getNeighbors() {
        return neighbors;
    }
}