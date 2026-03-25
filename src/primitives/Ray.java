package primitives;

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