package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Represents a spotlight: a point light with a specific direction and beam.
 * Intensity depends on both distance and the angle to the spotlight direction.
 */
public class SpotLight extends PointLight {

    /** The direction of the spotlight beam (normalized) */
    private final Vector _direction;

    /** Narrow beam exponent (1 = regular spot, higher = narrower beam) */
    private int _narrowBeam = 1;

    /**
     * Constructs a spotlight with the given intensity, position and direction.
     * @param intensity  the light's color/intensity
     * @param position   the position of the light
     * @param direction  the direction of the spotlight beam (will be normalized)
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        this._direction = direction.normalize();
    }

    /**
     * Sets the narrow beam exponent. Higher values produce a tighter cone.
     * @param narrowBeam the exponent
     * @return this (for method chaining)
     */
    public SpotLight setNarrowBeam(int narrowBeam) {
        this._narrowBeam = narrowBeam;
        return this;
    }

    /**
     * Sets the constant attenuation factor while preserving the fluent SpotLight type.
     *
     * @param kC the constant attenuation factor
     * @return this spotlight instance
     */
    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    /**
     * Sets the linear attenuation factor while preserving the fluent SpotLight type.
     *
     * @param kL the linear attenuation factor
     * @return this spotlight instance
     */
    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    /**
     * Sets the quadratic attenuation factor while preserving the fluent SpotLight type.
     *
     * @param kQ the quadratic attenuation factor
     * @return this spotlight instance
     */
    @Override
    public SpotLight setKq(double kQ) {
        return (SpotLight) super.setKq(kQ);
    }

    /**
     * Returns the spotlight intensity at the given point, including beam falloff.
     *
     * @param p the illuminated point
     * @return the attenuated and beam-scaled intensity, or black if the point is outside the beam
     */
    @Override
    public Color getIntensity(Point p) {
        double dirL = alignZero(_direction.dotProduct(getL(p)));
        if (dirL <= 0) return Color.BLACK;
        double beamFactor = (_narrowBeam == 1) ? dirL : Math.pow(dirL, _narrowBeam);
        return super.getIntensity(p).scale(beamFactor);
    }
}