package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;
import java.util.Objects;

/**
 * Abstract class defining the ability to find intersections with a ray.
 * Implements the NVI (Non-Virtual Interface) pattern:
 * public callers use {@link #calcIntersections(Ray)}, which delegates to the
 * protected abstract {@link #calcIntersectionsHelper(Ray)}.
 */
public abstract class Intersectable {

    /**
     * Default constructor for Intersectable.
     */
    public Intersectable() {
    }

    /**
     * Represents a single intersection between a ray and a geometry.
     * <p>
     * Acts as a cache for all per-intersection and per-light-source values
     * computed by the ray tracer, so that each value is computed once and
     * reused throughout the Phong shading calculation.
     */
    public static final class Intersection {

        // ── permanent data ────────────────────────────────────────────────

        /**
         * The geometry that was intersected.
         */
        public final Geometry geometry;

        /**
         * The world-space point of the intersection.
         */
        public final Point point;

        /**
         * The material of the intersected geometry.
         */
        public Material material;

        // ── per-intersection cache (filled by preprocessIntersection) ─────

        /**
         * Surface normal at the intersection point.
         */
        public Vector n;

        /**
         * The view direction: the direction of the camera ray,
         * pointing from the camera toward the scene.
         */
        public Vector v;

        /**
         * Dot product {@code n · v}.
         */
        public double vn;

        // ── per-light-source cache (filled by preprocessLightSource) ──────

        /**
         * Normalized direction from the light source to the intersection point.
         */
        public Vector l;

        /**
         * Dot product {@code n · l}.
         */
        public double nl;

        /**
         * Light intensity arriving at the intersection point from the current source.
         */
        public Color iL;

        // ─────────────────────────────────────────────────────────────────

        /**
         * Constructs an Intersection.
         *
         * @param geometry the intersected geometry (may be {@code null} when
         *                 wrapping a plain {@link Point} for closest-point lookup)
         * @param point    the intersection point in world space
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            this.material = geometry == null ? new Material() : geometry.getMaterial();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Intersection that = (Intersection) o;
            return this.geometry == that.geometry &&
                    Objects.equals(this.point, that.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(geometry, point);
        }

        @Override
        public String toString() {
            return "Intersection{geometry=" + geometry + ", point=" + point + '}';
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  NVI public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns all intersections with the given ray as plain {@link Point} objects.
     *
     * @param ray the intersecting ray
     * @return list of intersection points, or {@code null} if there are none
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null
                : intersections.stream()
                .map(i -> i.point)
                .toList();
    }

    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersections(ray, Double.POSITIVE_INFINITY);
    }

    public final List<Intersection> calcIntersections(Ray ray, double maxDistance) {
        return calcIntersectionsHelper(ray, maxDistance);
    }

    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance);

}