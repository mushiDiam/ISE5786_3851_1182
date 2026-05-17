package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Represents an infinite tube in 3D space.
 */
public class Tube extends RadialGeometry {
    /**
     * The central axis ray of the tube.
     */
    protected final Ray _axis;

    /**
     * Constructs a tube with a given radius and axis ray.
     *
     * @param radius the radius of the tube
     * @param axis   the central axis ray
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        // Vector from the start of the ray to the given point
        Vector p0ToPoint = point.subtract(p0);

        // The scalar projection of p0ToPoint on the direction vector
        double t = v.dotProduct(p0ToPoint);

        // Calculate the closest point on the axis to the given point
        Point o = p0;

        // We use isZero to avoid scaling by exactly 0, which would result in adding a null vector
        if (!primitives.Util.isZero(t)) {
            o = p0.add(v.scale(t));
        }

        // The normal is the normalized vector from the axis point 'o' to the given point
        return point.subtract(o).normalize();
    }

    /**
     * Finds intersections between a ray and the infinite tube.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) { // Renamed method here
        Point p0 = ray.origin();
        Vector v = ray.direction();
        Point pa = _axis.origin();
        Vector va = _axis.direction();

        Vector deltaP = null;
        try {
            deltaP = p0.subtract(pa);
        } catch (IllegalArgumentException e) {
            // p0 == pa (deltaP would be the zero vector)
        }

        double vDotVa = primitives.Util.alignZero(v.dotProduct(va));
        Vector vMinusVaVdotVa = v;

        // Handle orthogonal and parallel vectors properly without crashing
        if (vDotVa != 0) {
            try {
                vMinusVaVdotVa = v.subtract(va.scale(vDotVa));
            } catch (IllegalArgumentException e) {
                // v is parallel to va (A = 0). Ray is parallel to tube's axis.
                return null;
            }
        }

        double a = primitives.Util.alignZero(vMinusVaVdotVa.lengthSquared());
        double b = 0;
        double c = -_radiusSquared;

        if (deltaP != null) {
            double dpDotVa = primitives.Util.alignZero(deltaP.dotProduct(va));
            Vector dpMinusVaDpDotVa = deltaP;

            if (dpDotVa != 0) {
                try {
                    dpMinusVaDpDotVa = deltaP.subtract(va.scale(dpDotVa));
                } catch (IllegalArgumentException e) {
                    dpMinusVaDpDotVa = null; // deltaP is parallel to va
                }
            }

            if (dpMinusVaDpDotVa != null) {
                b = primitives.Util.alignZero(2 * vMinusVaVdotVa.dotProduct(dpMinusVaDpDotVa));
                c += primitives.Util.alignZero(dpMinusVaDpDotVa.lengthSquared());
            }
        }

        double discriminant = primitives.Util.alignZero(b * b - 4 * a * c);

        // No intersection or tangent (tangents are not included per PDF instructions)
        if (discriminant <= 0) {
            return null;
        }

        double sqrtDiscr = Math.sqrt(discriminant);
        double t1 = primitives.Util.alignZero((-b - sqrtDiscr) / (2 * a));
        double t2 = primitives.Util.alignZero((-b + sqrtDiscr) / (2 * a));

        if (t1 > 0 && t2 > 0) {
            return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        }
        if (t1 > 0) {
            return List.of(new Intersection(this, ray.getPoint(t1)));
        }
        if (t2 > 0) {
            return List.of(new Intersection(this, ray.getPoint(t2)));
        }

        return null;
    }
}