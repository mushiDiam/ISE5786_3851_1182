package geometries.impl;

/**
 * Abstract class representing radial geometries (geometries with a radius).
 */
public abstract class RadialGeometry {
    /**
     * The radius of the geometric body.
     */
    protected final double radius;

    /**
     * The squared radius of the geometric body (used for performance optimization).
     */
    protected final double radiusSquared;

    /**
     * Constructs a radial geometry with a given radius.
     * Initializes both the radius and the squared radius fields.
     *
     * @param radius the radius of the geometry
     */
    public RadialGeometry(double _radius) {
        this.radius = _radius;
        this.radiusSquared = _radius * _radius;
    }
}