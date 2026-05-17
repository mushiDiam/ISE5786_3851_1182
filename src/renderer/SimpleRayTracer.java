package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import scene.Scene;

/**
 * A simple ray tracer implementing the Phong reflection model.
 * <p>
 * For each primary ray:
 * <ol>
 *   <li>Find the closest intersection with any geometry in the scene.</li>
 *   <li>Compute ambient light (scaled by the material's kA) plus emission.</li>
 *   <li>For each light source, add diffuse and specular contributions
 *       (only when the light and camera are on the same side of the surface).</li>
 * </ol>
 */
class SimpleRayTracer extends RayTracerBase {

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

    /**
     * Computes the full color at an intersection point using the Phong model.
     * Color = (kA × ambient) + emission + localEffects
     *
     * @param gp  the closest intersection
     * @param ray the primary camera ray
     * @return the computed color at this point
     */
    private Color calcColor(Intersection gp, Ray ray) {
        Color base = _scene.ambientLight.getIntensity()
                .scale(gp.material.kA)
                .add(gp.geometry.getEmission());

        // If the ray grazes the surface (n·v == 0), skip local lighting
        if (!preprocessIntersection(gp, ray.direction())) return base;

        return base.add(calcColorLocalEffects(gp));
    }

    /**
     * Computes the sum of all local light-source contributions (diffuse + specular)
     * at the given intersection using the Phong reflection model.
     *
     * @param intersection the pre-processed intersection (n, v, vn must be set)
     * @return the total local color contribution
     */
    private Color calcColorLocalEffects(Intersection intersection) {
        Color color = Color.BLACK;

        for (LightSource lightSource : _scene.lights) {
            // Skip this light if it is on the opposite side of the surface
            if (!preprocessLightSource(intersection, lightSource)) continue;

            color = color.add(
                    intersection.iL.scale(
                            calcDiffuse(intersection).add(calcSpecular(intersection))
                    )
            );
        }
        return color;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Phong components
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes the diffuse component: {@code kD × |n · l|}.
     *
     * @param intersection the pre-processed intersection (nl, material must be set)
     * @return the diffuse attenuation factor
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.nl));
    }

    /**
     * Computes the specular component: {@code kS × max(0, -v · r)^nShininess},
     * where {@code r = l - 2(n · l)n} is the mirror-reflection vector.
     *
     * @param intersection the pre-processed intersection (l, nl, n, v, material must be set)
     * @return the specular attenuation factor
     */
    private Double3 calcSpecular(Intersection intersection) {
        // Reflection of l about n:  r = l - 2(n·l)·n
        var r = intersection.l.subtract(intersection.n.scale(2.0 * intersection.nl));
        double vr = -intersection.v.dotProduct(r);   // alignZero not needed: Math.pow handles near-0
        if (vr <= 0) return Double3.ZERO;
        return intersection.material.kS.scale(Math.pow(vr, intersection.material.nShininess));
    }
}