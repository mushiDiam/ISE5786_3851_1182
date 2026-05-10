package geometries.api;

import primitives.Point;
import primitives.Ray;

import java.util.List;
import java.util.Objects;

/**
 * Abstract class defining the ability to find intersections with a ray.
 */
public abstract class Intersectable {

    /**
     * Static inner class representing a point on a specific geometry.
     */
    public static class GeoPoint {
        public Geometry geometry;
        public Point point;

        /**
         * Constructor for GeoPoint.
         *
         * @param geometry the geometry the point belongs to
         * @param point    the point on the geometry
         */
        public GeoPoint(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GeoPoint geoPoint = (GeoPoint) o;
            return Objects.equals(geometry, geoPoint.geometry) &&
                    Objects.equals(point, geoPoint.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(geometry, point);
        }

        @Override
        public String toString() {
            return "GeoPoint{" + "geometry=" + geometry + ", point=" + point + '}';
        }
    }

    /**
     * Finds intersections between a ray and the geometry and returns them as Points.
     * This is the public NVI method.
     *
     * @param ray the intersecting ray
     * @return list of intersection points
     */
    public final List<Point> findIntersections(Ray ray) {
        var geoList = findGeoIntersections(ray);
        return geoList == null ? null : geoList.stream().map(gp -> gp.point).toList();
    }

    /**
     * Finds intersections between a ray and the geometry as GeoPoints.
     *
     * @param ray the intersecting ray
     * @return list of intersection GeoPoints
     */
    public final List<GeoPoint> findGeoIntersections(Ray ray) {
        return findGeoIntersectionsHelper(ray);
    }

    /**
     * Abstract helper method for finding intersections.
     * Each geometry must implement this to return GeoPoints.
     *
     * @param ray the intersecting ray
     * @return list of intersection GeoPoints
     */
    protected abstract List<GeoPoint> findGeoIntersectionsHelper(Ray ray);
}