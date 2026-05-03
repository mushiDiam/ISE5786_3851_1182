package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

import java.util.List;

/**
 * A simple implementation of a ray tracer.
 * Calculates the color of a pixel by finding the closest intersection point of a ray and using the scene's ambient light.
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
        List<Point> intersections = _scene.geometries.findIntersections(ray);

        // If the ray does not intersect any geometry, return the scene background color
        if (intersections == null || intersections.isEmpty()) {
            return _scene.background;
        }

        // Find the closest intersection point to the ray origin and calculate its color
        Point closestPoint = ray.findClosestPoint(intersections);
        return calcColor(closestPoint);
    }

    /**
     * Calculates the color at a specific intersection point.
     * Currently, this only returns the ambient light intensity.
     *
     * @param intersection the point of intersection on a geometry
     * @return the calculated color at the given point
     */
    private Color calcColor(Point intersection) {
        // For now, return only the ambient light intensity at the intersection point
        return _scene.ambientLight.getIntensity();
    }
}