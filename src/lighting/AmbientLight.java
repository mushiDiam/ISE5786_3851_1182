package lighting;

import primitives.Color;

/**
 * Represents the ambient light in the scene.
 * The ambient light is omnidirectional and illuminates all objects equally.
 */
public class AmbientLight {

    /**
     * A constant representing no ambient light (black color).
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * The intensity of the ambient light.
     */
    private final Color _intensity;

    /**
     * Constructs an ambient light with the specified intensity.
     *
     * @param intensity the color intensity of the ambient light
     */
    public AmbientLight(Color intensity) {
        this._intensity = intensity;
    }

    /**
     * Returns the intensity of the ambient light.
     *
     * @return the color intensity
     */
    public Color getIntensity() {
        return _intensity;
    }
}