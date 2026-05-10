package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

/**
 * Abstract base class for all geometric shapes.
 */
public abstract class Geometry extends Intersectable {

    /**
     * The inherent emission color of the geometry
     */
    protected Color _emission = Color.BLACK;

    /**
     * The material of the geometry
     */
    private Material _material = new Material();

    /**
     * Gets the material of the geometry.
     *
     * @return the material
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Sets the material of the geometry.
     *
     * @param material the new material
     * @return the current geometry object (for chaining)
     */
    public Geometry setMaterial(Material material) {
        this._material = material;
        return this;
    }

    /**
     * Gets the emission color of the geometry.
     *
     * @return the emission color
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Sets the emission color of the geometry.
     *
     * @param emission the new emission color
     * @return the current geometry object (for method chaining)
     */
    public Geometry setEmission(Color emission) {
        this._emission = emission;
        return this;
    }

    /**
     * Computes the normal vector to the geometry at a given point.
     *
     * @param point the point on the geometry surface
     * @return the normal vector at the given point
     */
    public abstract Vector getNormal(Point point);
}