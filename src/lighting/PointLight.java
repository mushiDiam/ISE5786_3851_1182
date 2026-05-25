package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a point light source that emits light in all directions
 * from a specific position, with distance attenuation.
 */
public class PointLight extends Light implements LightSource {

    /**
     * The position of the light in the scene
     */
    private final Point _position;

    /**
     * Constant attenuation factor (kC)
     */
    private double _kC = 1.0;

    /**
     * Linear attenuation factor (kL)
     */
    private double _kL = 0.0;

    /**
     * Quadratic attenuation factor (kQ)
     */
    private double _kQ = 0.0;

    /**
     * Constructs a point light with the given intensity and position.
     *
     * @param intensity the light's color/intensity
     * @param position  the position of the light in the scene
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        this._position = position;
    }

    /**
     * Sets the constant attenuation factor.
     *
     * @param kC the constant factor
     * @return this (for method chaining)
     */
    public PointLight setKc(double kC) {
        this._kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation factor.
     *
     * @param kL the linear factor
     * @return this (for method chaining)
     */
    public PointLight setKl(double kL) {
        this._kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation factor.
     *
     * @param kQ the quadratic factor
     * @return this (for method chaining)
     */
    public PointLight setKq(double kQ) {
        this._kQ = kQ;
        return this;
    }

    /**
     * Returns the normalized direction from the light position to the point.
     *
     * @param p the illuminated point
     * @return normalized direction from the light to the point
     */
    @Override
    public Vector getL(Point p) {
        // Direction from light position to the illuminated point (normalized)
        return p.subtract(_position).normalize();
    }

    /**
     * Returns the attenuated light intensity at the given point.
     *
     * @param p the illuminated point
     * @return the attenuated intensity
     */
    @Override
    public Color getIntensity(Point p) {
        double d = _position.distance(p);
        double attenuation = _kC + _kL * d + _kQ * d * d;
        return _intensity.scale(1.0 / attenuation);
    }

    /**
     * Returns the distance from the light position to the given point.
     *
     * @param point the point to measure distance to
     * @return the distance from the light source
     */
    @Override
    public double getDistance(Point point) {
        return _position.distance(point);
    }
}