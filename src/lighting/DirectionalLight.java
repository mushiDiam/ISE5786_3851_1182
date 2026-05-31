package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a directional light source (like the sun).
 * Direction and intensity are constant everywhere in the scene.
 */
public class DirectionalLight extends Light implements LightSource {

    /**
     * The fixed direction of the light (normalized)
     */
    private final Vector _direction;

    /**
     * Constructs a directional light with the given intensity and direction.
     *
     * @param intensity the light's color/intensity
     * @param direction the direction of the light (will be normalized)
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        this._direction = direction.normalize();
    }

    /**
     * Returns the constant normalized light direction.
     *
     * @param p the illuminated point (unused)
     * @return the light direction vector
     */
    @Override
    public Vector getL(Point p) {
        // For directional light, the direction is always constant
        return _direction;
    }

    /**
     * Returns the constant light intensity.
     *
     * @param p the illuminated point (unused)
     * @return the light intensity
     */
    @Override
    public Color getIntensity(Point p) {
        // For directional light, intensity is constant (no distance attenuation)
        return _intensity;
    }

    /**
     * Directional lights are effectively at an infinite distance.
     *
     * @param unused the point to measure distance to (unused)
     * @return positive infinity
     */
    @Override
    public double getDistance(Point unused) {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Directional lights have no size.
     *
     * @return 0 (no size)
     */
    @Override
    public double getSize() {
        return 0;
    }
}