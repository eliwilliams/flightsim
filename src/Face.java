import framework.ImprovedNoise;
import javafx.geometry.BoundingBox;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by eli on 3/6/15.
 */
public class Face {

    private BoundingBox hitbox;
    public ArrayList<Face> neighbors;
    private Vertex[] vertices;
    private int[][] children;
//    private Face[] neighbors;
    private Vertex normal;
    private int lod;
    private Vertex midpoint;


    public Face() {
        lod = 0;
        neighbors = new ArrayList<Face>();
        vertices = new Vertex[4];
        children = new int[4][2];
       // neighbors = new Face[8];
        midpoint = null;
    }

    public Face(Vertex bl, Vertex tl, Vertex tr, Vertex br, int l) {
        lod = l;
        neighbors = new ArrayList<Face>();
        vertices = new Vertex[4];
        children = new int[4][2];
        vertices[0] = bl;
        vertices[1] = tl;
        vertices[2] = tr;
        vertices[3] = br;
        this.normal = makeNormal();
        //neighbors = new Face[8];
        makeMidpoint();
    }

    public void makeHitbox(double step) {
        hitbox = new BoundingBox(findExtreme(0, false), -1, findExtreme(2, false), step, findExtreme(1, true), step);
    }

    public double findExtreme(int index, boolean find_max) {
        double h;
        if(find_max) {
            h = Float.MIN_VALUE;
            for(int i = 0; i < 4; i++) {
                h = Math.max(h, this.vertices[i].getAllPoints()[index]);
            }
        }
        else {
            h = Float.MAX_VALUE;
            for(int i = 0; i < 4; i++) {
                h = Math.min(h, this.vertices[i].getAllPoints()[index]);
            }
        }
        return h;
    }

    public void makeMidpoint() {
        this.midpoint = new Vertex((this.vertices[0].getX() + this.vertices[1].getX() + this.vertices[2].getX() + this.vertices[3].getX()) / 4.0f,
                (this.vertices[0].getY() + this.vertices[1].getY() + this.vertices[2].getY() + this.vertices[3].getY()) / 4.0f,
                (this.vertices[0].getZ() + this.vertices[1].getZ() + this.vertices[2].getZ() + this.vertices[3].getZ()) / 4.0f, this.lod);
    }

    public void addChild(int index, int x, int y) {
        children[index][0] = x;
        children[index][1] = y;
    }

    public int[] getChild(int index) {
        return children[index];
    }

    public void addNeighbor(Face f) {
        neighbors.add(f);
    }

    public void addVertex(int index, Vertex v) {
        vertices[index] = v;
    }

    public Vertex getVertex(int index) {
        return vertices[index];
    }

    public int getLOD() {
        return this.lod;
    }

    public float getDistance(float x, float y, float z) {
        return (float) Math.sqrt(Math.pow(midpoint.getX() - x, 2) + Math.pow(midpoint.getY() - y, 2) + Math.pow(midpoint.getZ() - z, 2));
    }

    public BoundingBox getHitbox(){
        return hitbox;
    }

    public Vertex makeNormal() {
        Vertex bl = this.vertices[0];
        Vertex tl = this.vertices[1];
        Vertex tr = this.vertices[2];
        Vertex br = this.vertices[3];
        float x0 = bl.getX();
        float x1 = tl.getX();
        float x2 = tr.getX();
        float x3 = br.getX();
        float y0 = bl.getY();
        float y1 = tl.getY();
        float y2 = tr.getY();
        float y3 = br.getY();
        float z0 = bl.getZ();
        float z1 = tl.getZ();
        float z2 = tr.getZ();
        float z3 = br.getZ();
        float nx = (y0 - y1) * (z0 + z1) + (y1 - y2) * (z1 + z2) + (y2 - y3) * (z2 + z3) + (y3 - y0) * (z3 + z0);
        float ny = (z0 - z1) * (x0 + x1) + (z1 - z2) * (x1 + x2) + (z2 - z3) * (x2 + x3) + (z3 - z0) * (x3 + x0);
        float nz = (x0 - x1) * (y0 + y1) + (x1 - x2) * (y1 + y2) + (x2 - x3) * (y2 + y3) + (x3 - x0) * (y3 + y0);
        return new Vertex(nx, ny, nz, lod);
    }

    public void setNormal(Vertex n) {
        normal = n;
    }

    public Vertex getNormal() {
        return normal;
    }
}