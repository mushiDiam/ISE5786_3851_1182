package primitives;

import geometries.api.Intersectable.Intersection;

import java.util.List;
import java.util.Objects;

/**
 * Represents a ray in 3D space, which is a half-line defined by a starting point and a direction vector.
 */
public final class Ray {
    /**
     * The starting point of the ray.
     */
    private final Point _origin;

    /**
     * The normalized direction vector of the ray.
     */
    private final Vector _direction;

    /**
     * Constructs a ray with a given starting point and direction.
     * The direction vector is normalized before assignment.
     *
     * @param origin    the starting point of the ray
     * @param direction the direction vector of the ray
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Returns the normalized direction vector of the ray.
     *
     * @return the direction vector
     */
    public Vector direction() {
        return _direction;
    }

    /**
     * Returns the origin point of this ray.
     *
     * @return the ray origin
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Calculates a point on the ray line at a given distance from the origin.
     *
     * @param t the distance (scalar) from the origin
     * @return a new Point at distance t from the origin
     */
    public Point getPoint(double t) {
        if (primitives.Util.isZero(t)) {
            return _origin;
        }
        return _origin.add(_direction.scale(t));
    }

    /**
     * Finds the closest point to the ray's origin from a given list of points.
     * Uses a stream to wrap points into Intersections and calls findClosestIntersection.
     *
     * @param points list of intersection points
     * @return the closest point, or null if the list is empty/null
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null || points.isEmpty() ? null
                : findClosestIntersection(
                points.stream()
                        .map(point -> new Intersection(null, point))
                        .toList()
        ).point;
    }

    /**
     * Finds the closest Intersection to the ray's origin from a given list of Intersections.
     *
     * @param intersections list of Intersections
     * @return the closest Intersection, or null if the list is empty/null
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null || intersections.isEmpty()) {
            return null;
        }

        Intersection closestPoint = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for (Intersection geo : intersections) {
            // Calculate squared distance to avoid expensive square root operations
            double distance = geo.point.distanceSquared(_origin);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = geo;
            }
        }

        return closestPoint;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _origin.equals(other._origin) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }

    @Override
    public String toString() {
        return "Ray{" + "origin=" + _origin + ", direction=" + _direction + '}';
    }
}