package framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Loads Wavefront OBJ model.
 * 
 * @author Robert C. Duvall
 */
public class OBJModel {
    private List<float[]> vertices;
    private List<float[]> vertexTexCoords;
    private List<float[]> vertexNormals;
    private List<int[]> faces;
    private List<int[]> faceNormals;
    private List<int[]> faceTexCoords;
    private Map<Integer, String> faceMats;
    private Map<String, Material> materials;
    private String myFileName;
    private int openglID;
    private float yMax;
    private float yMin;
    private float xMin;
    private float xMax;
    private float zMin;
    private float zMax;

    public OBJModel (String fileName) {
        vertices = new ArrayList<>();
        vertexTexCoords = new ArrayList<>();
        vertexNormals = new ArrayList<>();
        faces = new ArrayList<>();
        faceTexCoords = new ArrayList<>();
        faceNormals = new ArrayList<>();
        materials = new TreeMap<>();
        faceMats = new TreeMap<>();
        myFileName = fileName;
        load(fileName);
        center();
        normalize();
        openglID = -1;
    }

    public String toString () {
        return "Model " + myFileName + "\n" +
                "  # Faces " + faces.size() + "\n" +
                "  # Vertices " + vertices.size() + "\n" +
                "  # Texture Coords " + vertexTexCoords.size() + "\n" +
                "  # Normals " + vertexNormals.size() + "\n" +
                "  BBox = [" + yMax + ", " + yMin + "] [" + xMin + ", " + xMax + "] [" + zMax + ", " + zMin + "]";
    }

    public float[] getDimensions () {
        return new float[] { getXWidth(), getYHeight(), getZDepth() };
    }

    public float getXWidth () {
        return xMax - xMin;
    }

    public float getYHeight () {
        return yMax - yMin;
    }

    public float getZDepth () {
        return zMax - zMin;
    }

    public int numPolygons () {
        return faces.size();
    }

    public void renderCompiled (GL2 gl) {
        if (openglID < 0) {
            openglID = gl.glGenLists(1);
            gl.glNewList(openglID, GL2.GL_COMPILE);
            render(gl);
            gl.glEndList();
        } else {
            gl.glCallList(openglID);
        }
    }

    public void render (GL2 gl) {
        for (int f = 0; f < faces.size(); f++) {
            if (faceMats.containsKey(f) && materials.get(faceMats.get(f)) != null) {
                materials.get(faceMats.get(f)).enable(gl);
            }
            int type = (faces.get(f).length == 3) ? GL2.GL_TRIANGLES :
                           ((faces.get(f).length == 4) ? GL2.GL_QUADS : GL2.GL_POLYGON);
            gl.glBegin(type); {
                for (int v = 0; v < faces.get(f).length; v++) {
                    if (faceTexCoords.get(f)[v] != 0) {
                        float[] vt = vertexTexCoords.get(faceTexCoords.get(f)[v] - 1);
                        gl.glTexCoord3f(vt[0], 1 - vt[1], vt[2]);
                    }
                    if (faceNormals.get(f)[v] != 0) {
                        float[] vn = vertexNormals.get(faceNormals.get(f)[v] - 1);
                        gl.glNormal3f(vn[0], vn[1], vn[2]);
                    }
                    float[] vert = vertices.get(faces.get(f)[v] - 1);
                    gl.glVertex3f(vert[0], vert[1], vert[2]);
                }
            }
            gl.glEnd();
        }
    }

    private void load (String fileName) {
        int lineCount = 0;
        try {
            yMax = Float.MIN_VALUE;
            yMin = Float.MAX_VALUE;
            xMax = Float.MIN_VALUE;
            xMin = Float.MAX_VALUE;
            zMax = Float.MIN_VALUE;
            zMin = Float.MAX_VALUE;

            int faceCount = 0;
            int vCount = 0;
            int vtCount = 0;
            int vnCount = 0;

            String line;
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            while ((line = input.readLine()) != null) {
                lineCount++;
                line = line.trim();
                if (line.length() > 0) {
                    String[] tokens = line.split("\\s+");
                    String type = tokens[0];
                    if (type.equals("v")) {
                        float[] coords = loadCoords(tokens, 4);
                        vertices.add(coords);
                        vCount++;
                        xMax = Math.max(coords[0], xMax);
                        xMin = Math.min(coords[0], xMin);
                        yMax = Math.max(coords[1], yMax);
                        yMin = Math.min(coords[1], yMin);
                        zMax = Math.max(coords[2], zMax);
                        zMin = Math.min(coords[2], zMin);
                    } else if (type.equals("vt")) {
                        vertexTexCoords.add(loadCoords(tokens, 4));
                        vtCount++;
                    } else if (type.equals("vn")) {
                        vertexNormals.add(loadCoords(tokens, 4));
                        vnCount++;
                    } else if (type.equals("f")) {
                        int count = tokens.length - 1;
                        int[] v = new int[count];
                        int[] vt = new int[count];
                        int[] vn = new int[count];
                        for (int k = 0; k < tokens.length - 1; k++) {
                            String[] face = tokens[k + 1].split("/");
                            v[k] = Integer.parseInt(face[0]);
                            vt[k] = (face.length > 1 && face[1].length() > 0) ? Integer.parseInt(face[1]) : 0;
                            vn[k] = (face.length > 2 && face[2].length() > 0) ? Integer.parseInt(face[2]) : 0;
                            if (v[k] < 0)
                                v[k] = vCount + v[k] + 1;
                            if (vt[k] < 0)
                                vt[k] = vtCount + vt[k] + 1;
                            if (vn[k] < 0)
                                vn[k] = vnCount + vn[k] + 1;
                        }
                        faces.add(v);
                        faceTexCoords.add(vt);
                        faceNormals.add(vn);
                        faceCount++;
                    } else if (type.equals("mtllib")) {
                        String filePath = fileName.substring(0, fileName.lastIndexOf(File.separator) + 1);
                        for (int k = 1; k < tokens.length; k++) {
                            loadMaterial(filePath, tokens[k]);
                        }
                    } else if (type.equals("usemtl")) {
                        faceMats.put(faceCount, tokens[1]);
                    }
                }
            }
            input.close();
        } catch (IOException e) {
            throw new OBJException(e, "Failed to read OBJ file: %s", fileName);
        } catch (NumberFormatException e) {
            throw new OBJException(e, "Malformed OBJ file %s (on line %d)", fileName, lineCount);
        }
    }

    private void loadMaterial (String filePath, String fileName) {
        fileName = filePath + fileName;
        int lineCount = 0;
        try {
            Material mat = new Material();
            String line;
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            while (((line = input.readLine()) != null)) {
                lineCount++;
                line = line.trim();
                if (line.length() > 0) {
                    String[] tokens = line.split("\\s+");
                    String type = tokens[0];
                    if (type.equals("newmtl")) {
                        mat = new Material();
                        if (!materials.containsKey(tokens[1])) {
                            materials.put(tokens[1], mat);
                        }
                    } else if (type.equals("map_Kd")) {
                        // System.out.println(" loading texture " + (filePath + tokens[1]));
                        mat.texture = TextureIO.newTexture(new File(filePath + tokens[1]), false);
                    } else if (type.equals("Ka")) {
                        mat.ka = loadCoords(tokens, 3);
                    } else if (type.equals("Kd")) {
                        mat.kd = loadCoords(tokens, 3);
                    } else if (type.equals("Ks")) {
                        mat.ks = loadCoords(tokens, 3);
                    } else if (type.equals("d")) {
                        mat.d = loadCoords(tokens, 1);
                    } else if (type.equals("Ns")) {
                        mat.ns = loadCoords(tokens, 1);
                    } else if (type.equals("illum")) {
                        mat.illum = loadCoords(tokens, 1);
                    }
                }
            }
            input.close();
        } catch (IOException e) {
            throw new OBJException(e, "Failed to read MTL file: %s", fileName);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new OBJException(e, "Malformed MTL file %s (on line %d)", fileName, lineCount);
        }
    }

    private float[] loadCoords (String[] tokens, int maxCount) {
        float[] coords = new float[maxCount];
        for (int k = 0; k < tokens.length - 1 && k < coords.length; k++) {
            coords[k] = Float.parseFloat(tokens[k + 1]);
        }
        return coords;
    }

    private void center () {
        float xshift = xMin + (xMax - xMin) / 2;
        float yshift = yMin + (yMax - yMin) / 2;
        float zshift = zMin + (zMax - zMin) / 2;
        for (int k = 0; k < vertices.size(); k++) {
            float[] coords = vertices.get(k);
            coords[0] -= xshift;
            coords[1] -= yshift;
            coords[2] -= zshift;
        }
    }

    private void normalize () {
        float scale = Math.max(getXWidth(), Math.max(getYHeight(), getZDepth()));
        for (int k = 0; k < vertices.size(); k++) {
            float[] coords = vertices.get(k);
            coords[0] /= scale;
            coords[1] /= scale;
            coords[2] /= scale;
        }
        xMin /= scale;
        xMax /= scale;
        yMin /= scale;
        yMax /= scale;
        zMin /= scale;
        zMax /= scale;
    }

    private static class Material {
        public float[] d = { 1 };
        public float[] ns = { 1 };
        public float[] illum = { 1 };
        public float[] ka = { 0, 0, 0 };
        public float[] kd = { 0, 0, 0 };
        public float[] ks = { 0, 0, 0 };
        public Texture texture = null;

        private void enable (GL2 gl) {
            if (texture == null) {
                gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ka, 0);
                gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, kd, 0);
                gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, ks, 0);
                gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, d[0]);
            } else {
                texture.enable(gl);
                texture.bind(gl);
            }
        }
    }
}
