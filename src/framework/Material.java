package framework;

import javax.media.opengl.GL2;
import com.jogamp.common.nio.Buffers;


/**
 * Some definitions for common materials
 *
 * Shamelessly ripped off from OpenGL: A Primer by Edward Angel
 *
 * @author Robert C. Duvall
 */
public enum Material {
    BRASS(0.33f, 0.22f, 0.03f, 1.0f,
          0.78f, 0.57f, 0.11f, 1.0f,
          0.99f, 0.91f, 0.81f, 1.0f,
          27.8f),
    BRONZE(0.21f, 0.13f, 0.05f, 1.0f,
           0.71f, 0.43f, 0.18f, 1.0f,
           0.39f, 0.27f, 0.17f, 1.0f,
           25.6f),
    CHROME(0.25f, 0.25f, 0.25f, 1.0f,
           0.40f, 0.40f, 0.40f, 1.0f,
           0.77f, 0.77f, 0.77f, 1.0f,
           76.8f),
    PEARL(0.25f, 0.21f, 0.21f, 1.0f,
          1.00f, 0.83f, 0.83f, 1.0f,
          0.30f, 0.30f, 0.30f, 1.0f,
          11.3f),
    SILVER(0.19f, 0.19f, 0.19f, 1.0f,
           0.51f, 0.51f, 0.51f, 1.0f,
           0.51f, 0.51f, 0.51f, 1.0f,
           51.2f),
    JADE(0.14f, 0.22f, 0.16f, 1.0f,
         0.54f, 0.89f, 0.63f, 1.0f,
         0.32f, 0.32f, 0.32f, 1.0f,
         12.8f),
    COPPER(0.19f, 0.07f, 0.02f, 1.0f,
           0.70f, 0.27f, 0.08f, 1.0f,
           0.26f, 0.14f, 0.09f, 1.0f,
           12.8f),
    GOLD(0.25f, 0.20f, 0.07f, 1.0f,
         0.75f, 0.61f, 0.23f, 1.0f,
         0.63f, 0.56f, 0.37f, 1.0f,
         51.2f),
    PEWTER(0.11f, 0.06f, 0.11f, 1.0f,
           0.43f, 0.47f, 0.54f, 1.0f,
           0.33f, 0.33f, 0.52f, 1.0f,
           9.85f),
    EMERALD(0.02f, 0.17f, 0.02f, 0.55f,
            0.08f, 0.61f, 0.08f, 0.55f,
            0.63f, 0.73f, 0.63f, 0.55f,
            76.8f),
    RUBY(0.17f, 0.01f, 0.01f, 0.55f,
         0.61f, 0.04f, 0.04f, 0.55f,
         0.73f, 0.63f, 0.63f, 0.55f,
         76.8f),
    TURQUOISE(0.10f, 0.19f, 0.17f, 0.8f,
              0.40f, 0.74f, 0.69f, 0.8f,
              0.30f, 0.31f, 0.31f, 0.8f,
              12.8f),
    BLUE_METAL(0.03f, 0.03f, 0.33f, 1.0f,
               0.11f, 0.11f, 0.78f, 1.0f,
               0.81f, 0.81f, 0.99f, 1.0f,
               27.8f),
    RED_METAL(0.33f, 0.03f, 0.03f, 1.0f,
              0.78f, 0.11f, 0.11f, 1.0f,
              0.99f, 0.81f, 0.81f, 1.0f,
              27.8f),
    PINK_PLASTIC(0.3f, 0.0f, 0.0f, 1.0f,
                 0.9f, 0.7f, 0.7f, 1.0f,
                 0.8f, 0.6f, 0.6f, 1.0f,
                 32.0f),
    GREEN_PLASTIC(0.0f, 0.3f, 0.0f, 1.0f,
                  0.5f, 0.6f, 0.5f, 1.0f,
                  0.6f, 0.8f, 0.6f, 1.0f,
                  32.0f),
    DULL_PINK(1.0f, 0.7f, 0.7f, 1.0f,
              1.0f, 0.7f, 0.7f, 1.0f,
              0.3f, 0.1f, 0.1f, 1.0f,
              32.0f),
    DULL_GREEN(0.7f, 1.0f, 0.7f, 1.0f,
               0.7f, 1.0f, 0.7f, 1.0f,
               0.1f, 0.3f, 0.1f, 1.0f,
               32.0f);

    // values
    private float[] ambient;
    private float[] diffuse;
    private float[] specular;
    private float shininess;

    /**
     * Construct a material from its component properties.
     */
    Material (float ambientR, float ambientG, float ambientB, float ambientA,
              float diffuseR, float diffuseG, float diffuseB, float diffuseA,
              float specularR, float specularG, float specularB, float specularA,
              float shininess) {
        ambient = new float[] { ambientR, ambientG, ambientB, ambientA };
        diffuse = new float[] { diffuseR, diffuseG, diffuseB, diffuseA };
        specular = new float[] { specularR, specularG, specularB, specularA };
        this.shininess = shininess;
    }

    /**
     * Set the material properties of an object.
     */
    public void set (GL2 gl) {
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
    }

    /**
     * Set the material properties of an object as a simple color (e.g., if no light).
     */
    public void setAsColor (GL2 gl) {
        gl.glColor4fv(Buffers.newDirectFloatBuffer(diffuse));
    }
}
