package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * A ray tracer implementing the Phong reflection model with Global Illumination.
 * Supports soft shadows, reflections, and refractions.
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Maximum recursion depth for global illumination calculations.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Minimum contribution threshold below which recursion stops.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /**
     * Initial attenuation factor for recursive color calculations.
     */
    private static final Double3 INITIAL_K = Double3.ONE;

    /**
     * Constructs a ray tracer for the given scene.
     *
     * @param scene the scene to render
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Ray tracing entry point
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Traces a ray and returns the computed color.
     *
     * @param ray the ray to trace
     * @return the resulting color, or the scene background if no intersection is found
     */
    @Override
    Color traceRay(Ray ray) {
        Intersection closest = findClosestIntersection(ray);
        if (closest == null) return _scene.background;
        return calcColor(closest, ray);
    }

    /**
     * Finds the closest intersection of the ray with the scene geometries.
     *
     * @param ray the ray to test
     * @return the closest intersection, or {@code null} if none exist
     */
    private Intersection findClosestIntersection(Ray ray) {
        var intersections = _scene.geometries.calcIntersections(ray);
        if (intersections == null) return null;
        return ray.findClosestIntersection(intersections);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Color computation (Recursive)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Wrapper method for calculating the color at an intersection point.
     * Adds ambient light once, preprocesses the intersection, and starts recursion.
     *
     * @param gp  the intersection to shade
     * @param ray the camera ray that hit the intersection
     * @return the shaded color at the intersection
     */
    private Color calcColor(Intersection gp, Ray ray) {
        Color ambient = _scene.ambientLight.getIntensity().scale(gp.material.kA);

        if (!preprocessIntersection(gp, ray.direction()))
            return ambient.add(gp.geometry.getEmission());

        return ambient.add(calcColor(gp, MAX_CALC_COLOR_LEVEL, INITIAL_K));
    }

    /**
     * Recursive method to calculate color including local and global effects.
     *
     * @param intersection the intersection to shade
     * @param level        the current recursion depth
     * @param k            accumulated attenuation factor
     * @return the computed color contribution
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = intersection.geometry.getEmission()
                .add(calcColorLocalEffects(intersection, k));
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Computes the local light-source contributions with partial-transparency shadows.
     *
     * @param intersection the current intersection
     * @param k            accumulated attenuation factor
     * @return the local lighting contribution
     */
    private Color calcColorLocalEffects(Intersection intersection, Double3 k) {
        Color color = Color.BLACK;

        for (LightSource lightSource : _scene.lights) {
            // sets intersection.l, intersection.nl, intersection.iL, intersection.light
            if (!preprocessLightSource(intersection, lightSource)) continue;

            Double3 ktr = transparency(intersection);  // uses intersection.light internally

            if (!ktr.product(k).isLowerThan(MIN_CALC_COLOR_K))
                color = color.add(
                        intersection.iL.scale(ktr).scale(
                                calcDiffuse(intersection).add(calcSpecular(intersection))));
        }
        return color;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Global Effects (Reflection and Refraction)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes recursive reflection and refraction contributions.
     *
     * @param gp    the current intersection
     * @param level recursion depth remaining
     * @param k     accumulated attenuation factor
     * @return the global lighting contribution
     */
    private Color calcGlobalEffects(Intersection gp, int level, Double3 k) {
        Color color = Color.BLACK;

        Double3 kr = gp.material.kR;
        if (!kr.product(k).isLowerThan(MIN_CALC_COLOR_K)) {
            Vector r = gp.v.subtract(gp.n.scale(2 * gp.v.dotProduct(gp.n)));
            color = color.add(calcGlobalEffect(new Ray(gp.point, r, gp.n), level, kr, k));
        }

        Double3 kt = gp.material.kT;
        if (!kt.product(k).isLowerThan(MIN_CALC_COLOR_K))
            color = color.add(calcGlobalEffect(new Ray(gp.point, gp.v, gp.n), level, kt, k));

        return color;
    }

    /**
     * Computes a single recursive global effect contribution.
     *
     * @param ray   the secondary ray to trace
     * @param level recursion depth remaining
     * @param kx    material effect coefficient (kR or kT)
     * @param k     accumulated attenuation factor
     * @return the global effect contribution
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 kx, Double3 k) {
        Intersection closest = findClosestIntersection(ray);
        if (closest == null) return _scene.background.scale(kx);

        if (!preprocessIntersection(closest, ray.direction()))
            return closest.geometry.getEmission().scale(kx);

        return calcColor(closest, level - 1, k.product(kx)).scale(kx);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Phong components
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes the diffuse lighting component: {@code kD × |n·l|}.
     *
     * @param intersection the current intersection
     * @return the diffuse contribution
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.nl));
    }

    /**
     * Computes the specular lighting component: {@code kS × max(0, -v·r)^nShininess}.
     *
     * @param intersection the current intersection
     * @return the specular contribution
     */
    private Double3 calcSpecular(Intersection intersection) {
        var r = intersection.l.subtract(intersection.n.scale(2.0 * intersection.nl));
        double vr = -intersection.v.dotProduct(r);
        if (vr <= 0) return Double3.ZERO;
        return intersection.material.kS.scale(Math.pow(vr, intersection.material.nShininess));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Shadows
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes the accumulated transparency factor along the shadow ray from
     * the intersection point to the current light source.
     * Uses {@code intersection.l} and {@code intersection.light} from the cache
     * (both set by {@code preprocessLightSource}).
     *
     * @param intersection the current intersection (light cache must be populated)
     * @return accumulated kT factor; {@link Double3#ONE} means fully unblocked,
     * {@link Double3#ZERO} means fully in shadow
     */
    private Double3 transparency(Intersection intersection) {
        // l points FROM the light TO the surface; flip it to shoot toward the light
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.n);

        double maxDistance = intersection.light.getDistance(intersection.point);
        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay, maxDistance);

        if (shadowIntersections == null) return Double3.ONE;

        Double3 ktr = Double3.ONE;
        for (Intersection si : shadowIntersections) {
            ktr = ktr.product(si.material.kT);
            if (ktr.isLowerThan(MIN_CALC_COLOR_K)) return Double3.ZERO;
        }
        return ktr;
    }

    /**
     * Checks whether a point is unshaded from the current light source.
     * An opaque geometry (kT below threshold) fully blocks the light.
     * A partially transparent geometry does NOT block it.
     * Kept alongside {@link #transparency} so the professor can evaluate
     * the Part I/II shadow approach independently.
     *
     * @param intersection the current intersection (light cache must be populated)
     * @return {@code true} if the point receives light (not in shadow)
     */
    private boolean unshaded(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.n);

        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay,
                intersection.light.getDistance(intersection.point));
        if (shadowIntersections == null) return true;

        for (Intersection si : shadowIntersections)
            if (si.material.kT.isLowerThan(MIN_CALC_COLOR_K))
                return false;
        return true;
    }
}