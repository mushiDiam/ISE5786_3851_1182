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

    @Override
    public Vector getL(Point p) {
        // For directional light, the direction is always constant
        return _direction;
    }

    @Override
    public Color getIntensity(Point p) {
        // For directional light, intensity is constant (no distance attenuation)
        return _intensity;
    }

    @Override
    public double getDistance(Point point) {
        return Double.POSITIVE_INFINITY;
    }
}