package scene;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import lighting.LightSource;
import primitives.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scene containing all the geometric objects, lights, and background.
 * This class uses a Passive Data Structure (PDS) architecture, hence fields are public.
 */
public class Scene {

    /**
     * The name of the scene.
     */
    public String name;

    /**
     * The background color of the scene. Default is black.
     */
    public Color background = Color.BLACK;

    /**
     * The ambient light of the scene. Default is no light.
     */
    public AmbientLight ambientLight = AmbientLight.NONE;

    /**
     * The 3D geometries present in the scene. Default is an empty collection.
     */
    public Geometries geometries = new Geometries();

    /**
     * List of all external light sources in the scene
     */
    public List<LightSource> lights = new ArrayList<>();

    /**
     * Constructs a new scene with the specified name.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param background the background color
     * @return the current Scene object (for method chaining)
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     *
     * @param ambientLight the ambient light
     * @return the current Scene object (for method chaining)
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometries of the scene.
     *
     * @param geometries the collection of geometric bodies
     * @return the current Scene object (for method chaining)
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }
}