package geometries.impl;

import geometries.api.Geometry;

/**
 * Abstract class representing radial geometries (geometries with a radius).
 */
public abstract class RadialGeometry extends Geometry {
    /**
     * The radius of the geometric body.
     */
    protected final double _radius;

    /**
     * The squared radius of the geometric body (used for performance optimization).
     */
    protected final double _radiusSquared;

    /**
     * Constructs a radial geometry with a given radius.
     * Initializes both the radius and the squared radius fields.
     *
     * @param _radius the radius of the geometry
     */
    public RadialGeometry(double radius) {
        this._radius = radius;
        this._radiusSquared = radius * radius;
    }
}