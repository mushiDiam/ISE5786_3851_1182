package lighting;

import primitives.Color;

/**
 * Represents the ambient light in the scene.
 * The ambient light is omnidirectional and illuminates all objects equally.
 */
public class AmbientLight extends Light {

    /** A constant representing no ambient light (black color). */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Constructs an ambient light with the specified intensity.
     * @param intensity the color intensity of the ambient light
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }
}