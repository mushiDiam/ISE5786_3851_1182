package primitives;

import java.util.Objects;

/**
 * Represents a ray in 3D space, which is a half-line defined by a starting point and a direction vector.
 */
public class Ray {
    /**
     * The starting point of the ray.
     */
    private final Point _head;

    /**
     * The normalized direction vector of the ray.
     */
    private final Vector _direction;

    /**
     * Constructs a ray with a given starting point and direction.
     * The direction vector is normalized before assignment.
     *
     * @param head      the starting point of the ray
     * @param direction the direction vector of the ray
     */
    public Ray(Point head, Vector direction) {
        _head = head;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _head.equals(other._head) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_head, _direction);
    }

    @Override
    public String toString() {
        return "Ray{" + "head=" + _head + ", direction=" + _direction + '}';
    }
}