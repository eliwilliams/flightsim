import framework.ImprovedNoise;
import javafx.geometry.BoundingBox;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by eli on 3/24/15.
 */
public class Mesh {

    private int LOD;
//    private Skybox myBox;
    private HashMap<Integer, Face [][]> grid;
    private float low;
    private float high;

    public Mesh(int l, double w, double h) {
        LOD = l;
        Face [][] grid0 = new Face [(int)w][(int)h];
        grid = new HashMap<>();
        grid.put(0, grid0);
        low =  Float.MAX_VALUE;
        high = Float.MIN_VALUE;
    }

    public boolean checkIntersect(float currentX, float currentY, float currentZ) {
        for(int x = 0; x < grid.get(0).length; x++) {
            for (int y = 0; y < grid.get(0)[0].length; y++) {
                if (grid.get(0)[x][y].getHitbox().contains(currentX, currentY, currentZ)) {
                    return true;
                }
            }
        }
        return false;
    }

    public float[] fixTerrainIntersect(int i, int j, float x, float y, float z, float max) {
        if (y > 0.1f && y < max && !grid.get(0)[i][j].getHitbox().contains(x, y, z)) {
            return new float[]{x, y, z};
        }
        else if(y >= max) {
            return fixTerrainIntersect(i, j, x, y - 10f, z, max);
        }
        else {
            return fixTerrainIntersect(i, j, x, y + 200f, z, max);
        }
    }

    public ArrayList<float[]> fixControlPointHeights(ArrayList<float[]> pts, float max_y) {
        ArrayList<float[]> adjusted = new ArrayList<float[]>();
        for(float[] point : pts) {
            for(int x = 0; x < grid.get(0).length; x++) {
                for (int y = 0; y < grid.get(0)[0].length; y++) {
                        if (grid.get(0)[x][y].getHitbox().contains(point[0], point[1], point[2])) {
                            float[] adjusted_point;
                            adjusted_point = fixTerrainIntersect(x, y, point[0], point[1], point[2], max_y);
                                if (!grid.get(0)[x][y].getHitbox().contains(adjusted_point[0], adjusted_point[1], adjusted_point[2])) {
                                    adjusted.add(adjusted_point);
                                }
                    }
                }
            }
        }
        return adjusted;
    }

    public void spawnChildren(int l) {
        Face [][] grid2 = new Face[grid.get(l - 1).length * 2][grid.get(l - 1)[0].length * 2];
        Face [][] grid1 = grid.get(l - 1);
        for (int x = 0; x < grid1.length; x++) {
            for (int y = 0; y < grid1[0].length; y++) {
                Face target = grid1[x][y];
                grid2 = squareDiamond(grid2, target, x*2, y*2);
            }
        }
        grid.put(l, grid2);
    }

    public Face[][] squareDiamond(Face[][] coords, Face face, int x, int y) {
        int newLOD = face.getLOD() + 1;
        Vertex vbl = new Vertex(face.getVertex(0));
        Vertex vtl = new Vertex(face.getVertex(1));
        Vertex vtr = new Vertex(face.getVertex(2));
        Vertex vbr = new Vertex(face.getVertex(3));
        Vertex midpoint = findMidPointOfFace(face);
        double noise = ImprovedNoise.noise(midpoint.getX(), midpoint.getY(), midpoint.getZ());
        midpoint.setY(midpoint.getY() + (float) noise);
        Face bl = new Face(vbl, findMidPointOfLine(vbl, vtl, newLOD), midpoint, findMidPointOfLine(vbl, vbr, newLOD), newLOD);
        Face tl = new Face(findMidPointOfLine(vbl, vtl, newLOD), vtl, findMidPointOfLine(vtl, vtr, newLOD), midpoint, newLOD);
        Face tr = new Face(midpoint, findMidPointOfLine(vtl, vtr, newLOD), vtr, findMidPointOfLine(vtr, vbr, newLOD), newLOD);
        Face br = new Face(findMidPointOfLine(vbl, vbr, newLOD), midpoint, findMidPointOfLine(vtr, vbr, newLOD), vbr, newLOD);
        coords[x][y] = bl;
        face.addChild(0, x, y);
        coords[x][y+1] = tl;
        face.addChild(1, x, y+1);
        coords[x+1][y+1] = tr;
        face.addChild(2, x+1, y+1);
        coords[x+1][y] = br;
        face.addChild(3, x+1, y);
        return coords;
    }

    public Vertex findMidPointOfFace(Face f) {
        Vertex vbl = new Vertex(f.getVertex(0));
        Vertex vtl = new Vertex(f.getVertex(1));
        Vertex vtr = new Vertex(f.getVertex(2));
        Vertex vbr = new Vertex(f.getVertex(3));
        return findMidPointOfLine(findMidPointOfLine(vbl, vtr, f.getLOD() + 1), findMidPointOfLine(vtl, vbr, f.getLOD() + 1), f.getLOD() + 1);
    }

    public Vertex findMidPointOfLine(Vertex a, Vertex b, int l) {
        return new Vertex((a.getX() + b.getX()) / 2.0f, (a.getY() + b.getY()) / 2.0f, (a.getZ() + b.getZ()) / 2.0f, l);
    }

    public void addFace(int index, Face toAdd, int x, int y) {
        grid.get(index)[x][y] = toAdd;
    }

    public Face getFace(int index, int x, int y) {
        return grid.get(index)[x][y];
    }

    public void assignColors(int index) {
        findExtremes(index);
        for(int x = 0; x < grid.get(index).length; x++) {
            for(int y = 0; y < grid.get(index)[0].length; y++) {
                Face f = grid.get(index)[x][y];
                for(int d = 0; d < 4; d++) {
                    Vertex target = f.getVertex(d);
                    if(target.getY() == low) {
                        target.setColor(0);
                    }
                    if(target.getY() >= low && target.getY() < high * 0.75f) {
                        target.setColor(1);
                    }
                    if(target.getY() >= high * 0.75f) {
                        target.setColor(2);
                    }
                }
            }
        }
    }

    public void findExtremes(int index) {
        for(int x = 0; x < grid.get(index).length; x++) {
            for(int y = 0; y < grid.get(index)[0].length; y++) {
                Face f = grid.get(index)[x][y];
                for(int d = 0; d < 4; d++) {
                    Vertex target = f.getVertex(d);
                    float height = target.getY();
                    if(height > high) {
                        high = height;
                    }
                    if(height < low) {
                        low = height;
                    }
                }
            }
        }
    }

    public void makeAllMidpoints(int index) {
        for(int x = 0; x < grid.get(index).length; x++) {
            for(int y = 0; y < grid.get(index)[0].length; y++) {
                grid.get(index)[x][y].makeMidpoint();
            }
        }
    }

    public void findFaceNeighbors(int index) {
        for (int x = 0; x < grid.get(index).length; x++) {
            for (int y = 0; y < grid.get(index)[0].length; y++) {
                Face target = grid.get(index)[x][y];

                int X = 0;
                int Y = 0;
                if(x == 0) {
                    X = grid.get(index).length - 1;
                }
                if(y == 0) {
                    Y = grid.get(index)[0].length - 1;
                }
                if(x == grid.get(index).length - 1) {
                    X = 0;
                }
                if(y == grid.get(index)[0].length - 1) {
                    Y = 0;
                }

                // southwest face
                if (x > 0 && y > 0) {
                    target.addNeighbor(grid.get(index)[x-1][y-1]);
                }
                else if (x == 0 && y > 0) {
                    target.addNeighbor(grid.get(index)[X][y-1]);
                }
                else if (x > 0 && y == 0) {
                    target.addNeighbor(grid.get(index)[x-1][Y]);
                }
                else {
                    target.addNeighbor(grid.get(index)[X][Y]);
                }

                // west face
                if (x > 0) {
                    target.addNeighbor(grid.get(index)[x-1][y]);
                }
                else {
                    target.addNeighbor(grid.get(index)[X][y]);
                }

                // northwest face
                if (x > 0 && y < grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[x-1][y+1]);
                }
                else if (x > 0 && y == grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[x-1][Y]);
                }
                else if (x == 0 && y < grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[X][y+1]);
                }
                else {
                    target.addNeighbor(grid.get(index)[X][Y]);
                }

                // north face
                if (y < grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[x][y+1]);
                }
                else {
                    target.addNeighbor(grid.get(index)[x][Y]);
                }

                // northeast face
                if (x < grid.get(index).length - 1 && y < grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[x+1][y+1]);
                }
                else if (x == grid.get(index).length - 1 && y < grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[X][y]);
                }
                else if (x < grid.get(index).length - 1 && y == grid.get(index)[0].length - 1) {
                    target.addNeighbor(grid.get(index)[x][Y]);
                }
                else {
                    target.addNeighbor(grid.get(index)[X][Y]);
                }

                // east face
                if (x < grid.get(index).length - 1) {
                    target.addNeighbor(grid.get(index)[x+1][y]);
                }
                else {
                    target.addNeighbor(grid.get(index)[X][y]);
                }

                // southeast face
                if (x < grid.get(index).length - 1 && y > 0) {
                    target.addNeighbor(grid.get(index)[x+1][y-1]);
                }
                else if (x == grid.get(index).length - 1 && y > 0) {
                    target.addNeighbor(grid.get(index)[X][y]);
                }
                else if (x < grid.get(index).length - 1 && y == 0) {
                    target.addNeighbor(grid.get(index)[x][Y]);
                }
                else {
                    target.addNeighbor(grid.get(index)[X][Y]);
                }

                // south face
                if (y > 0) {
                    target.addNeighbor(grid.get(index)[x][y-1]);
                }
                else {
                    target.addNeighbor(grid.get(index)[x][Y]);
                }
            }
        }
    }

    public void makeVertexNeighbors(int index) {
        for (int x = 0; x < grid.get(index).length; x++) {
            for (int y = 0; y < grid.get(index)[0].length; y++) {
                Face f = grid.get(index)[x][y];
                for (int d = 0; d < 4; d++) {
                    Vertex target = f.getVertex(d);
                    addVertexNeighbors(d, index, target, x, y);
                    target.makeNormalFromNeighbors();
                }
            }
        }
    }

    public void addVertexNeighbors (int vert, int index, Vertex target, int x, int y) {

        // bottom left
        if (vert == 0) {
            if (x > 0 && y > 0) {
                target.addNeighbor(grid.get(index)[x-1][y-1]);
            }
            if (x > 0) {
                target.addNeighbor(grid.get(index)[x - 1][y]);
            }
            target.addNeighbor(grid.get(index)[x][y]);
            if (y > 0) {
                target.addNeighbor(grid.get(index)[x][y-1]);
            }
        }

        // top left
        if (vert == 1) {
            if (x > 0) {
                target.addNeighbor(grid.get(index)[x-1][y]);
            }
            if (x > 0 && y < grid.get(index)[0].length - 1) {
                target.addNeighbor(grid.get(index)[x-1][y+1]);
            }
            if (y < grid.get(index)[0].length - 1) {
                target.addNeighbor(grid.get(index)[x][y+1]);
            }
            target.addNeighbor(grid.get(index)[x][y]);
        }

        // top right
        if (vert == 2) {
            target.addNeighbor(grid.get(index)[x][y]);
            if (y < grid.get(index)[0].length - 1) {
                target.addNeighbor(grid.get(index)[x][y+1]);
            }
            if (x < grid.get(index).length - 1 && y < grid.get(index)[0].length - 1) {
                target.addNeighbor(grid.get(index)[x+1][y+1]);
            }
            if (x < grid.get(index).length - 1) {
                target.addNeighbor(grid.get(index)[x+1][y]);
            }
        }

        // bottom right
        if (vert == 3) {
            if (y > 0) {
                target.addNeighbor(grid.get(index)[x][y-1]);
            }
            target.addNeighbor(grid.get(index)[x][y]);
            if (x < grid.get(index).length - 1) {
                target.addNeighbor(grid.get(index)[x+1][y]);
            }
            if (x < grid.get(index).length - 1 && y > 0) {
                target.addNeighbor(grid.get(index)[x+1][y-1]);
            }
        }
    }

    public int getGridWidth(int index) {
        return grid.get(index).length;
    }

    public int getGridHeight(int index) {
        return grid.get(index)[0].length;
    }
}