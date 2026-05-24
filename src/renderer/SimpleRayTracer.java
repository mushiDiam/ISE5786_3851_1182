package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * A simple ray tracer implementing the Phong reflection model.
 * Now includes support for hard shadows (Part 1 of Stage 8).
 */
class SimpleRayTracer extends RayTracerBase {

    private static final double DELTA = 0.1;

    /**
     * Constructs a SimpleRayTracer for the given scene.
     *
     * @param scene the scene to render
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Ray tracing entry point
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    Color traceRay(Ray ray) {
        var intersections = _scene.geometries.calcIntersections(ray);
        if (intersections == null) return _scene.background;

        Intersection closest = ray.findClosestIntersection(intersections);
        return calcColor(closest, ray);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Color computation
    // ──────────────────────────────────────────────────────────────────────────

    private Color calcColor(Intersection gp, Ray ray) {
        Color base = _scene.ambientLight.getIntensity()
                .scale(gp.material.kA)
                .add(gp.geometry.getEmission());

        // If the ray grazes the surface (n·v == 0), skip local lighting
        if (!preprocessIntersection(gp, ray.direction())) return base;

        return base.add(calcColorLocalEffects(gp));
    }

    private Color calcColorLocalEffects(Intersection intersection) {
        Color color = Color.BLACK;

        for (LightSource lightSource : _scene.lights) {
            // Skip this light if it is on the opposite side of the surface
            if (!preprocessLightSource(intersection, lightSource)) continue;

            // Add the light only if the point is unshaded by this light source
            if (unshaded(intersection, lightSource)) {
                color = color.add(
                        intersection.iL.scale(
                                calcDiffuse(intersection).add(calcSpecular(intersection))
                        )
                );
            }
        }
        return color;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Phong components
    // ──────────────────────────────────────────────────────────────────────────

    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.nl));
    }

    private Double3 calcSpecular(Intersection intersection) {
        var r = intersection.l.subtract(intersection.n.scale(2.0 * intersection.nl));
        double vr = -intersection.v.dotProduct(r);
        if (vr <= 0) return Double3.ZERO;
        return intersection.material.kS.scale(Math.pow(vr, intersection.material.nShininess));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Shadows (Part 1)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Checks if a specific intersection point is unshaded by a given light source.
     *
     * @param intersection the intersection point to check
     * @param lightSource  the light source illuminating the point
     * @return true if there is a clear line of sight to the light, false if shaded
     */
    private boolean unshaded(Intersection intersection, LightSource lightSource) {
        Vector lightDirection = lightSource.getL(intersection.point).scale(-1);

        Vector delta = intersection.n.scale(intersection.n.dotProduct(lightDirection) > 0 ? DELTA : -DELTA);
        Point head = intersection.point.add(delta);
        Ray lightRay = new Ray(head, lightDirection);

        // Get the distance to the light source
        double maxDistance = lightSource.getDistance(intersection.point);

        // Pass the maxDistance directly to the geometry engine
        var intersections = _scene.geometries.calcIntersections(lightRay, maxDistance);

        return intersections == null || intersections.isEmpty();
    }
}