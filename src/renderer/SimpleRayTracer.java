package renderer;

import geometries.api.Intersectable.GeoPoint;
import primitives.Color;
import primitives.Ray;
import scene.Scene;

import java.util.List;

/**
 * A simple implementation of a ray tracer.
 * Calculates the color of a pixel by finding the closest intersection point of a ray
 * and using the scene's ambient light combined with the object's emission color.
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Constructs a SimpleRayTracer with a given scene.
     *
     * @param scene the scene to be rendered
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    @Override
    Color traceRay(Ray ray) {
        // Find all intersections using the new NVI method returning GeoPoints
        List<GeoPoint> intersections = _scene.geometries.findGeoIntersections(ray);

        // If the ray does not intersect any geometry, return the scene background color
        if (intersections == null || intersections.isEmpty()) {
            return _scene.background;
        }

        // Find the closest intersection GeoPoint to the ray origin
        GeoPoint closestPoint = ray.findClosestGeoPoint(intersections);

        // Calculate and return the color at that specific point
        return calcColor(closestPoint, ray);
    }

    /**
     * Calculates the color at a specific intersection point.
     * Combines the ambient light of the scene scaled by the material's kA,
     * with the emission color of the geometry.
     *
     * @param intersection the GeoPoint of intersection (includes geometry and point)
     * @param ray          the ray that caused the intersection
     * @return the calculated color at the given point
     */
    private Color calcColor(GeoPoint intersection, Ray ray) {
        return _scene.ambientLight.getIntensity()
                // Scale the ambient light by the material's kA factor
                .scale(intersection.geometry.getMaterial().kA)
                // Add the geometry's own emission color
                .add(intersection.geometry.getEmission());
    }
}