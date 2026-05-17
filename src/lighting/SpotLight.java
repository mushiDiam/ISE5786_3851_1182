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

    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    @Override
    public SpotLight setKq(double kQ) {
        return (SpotLight) super.setKq(kQ);
    }

    @Override
    public Color getIntensity(Point p) {
        double dirL = alignZero(_direction.dotProduct(getL(p)));
        if (dirL <= 0) return Color.BLACK;
        double beamFactor = (_narrowBeam == 1) ? dirL : Math.pow(dirL, _narrowBeam);
        return super.getIntensity(p).scale(beamFactor);
    }
}