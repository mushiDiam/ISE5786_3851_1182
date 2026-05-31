package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Blackboard;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;
import scene.Scene;

import java.util.List;

/**
 * A ray tracer implementing the Phong reflection model with Global Illumination.
 * Supports partial-transparency shadows, soft shadows via area-light super-sampling,
 * glossy reflections, diffuse (blurry) transparency, and full recursive GI.
 *
 * <p><b>Soft-shadow dependency:</b> {@code LightSource.getSize()} must be implemented
 * by every concrete light type ({@code PointLight} returns its radius field,
 * {@code SpotLight} inherits, {@code DirectionalLight} returns 0).</p>
 *
 * <p><b>Glossy / diffuse dependency:</b> {@link primitives.Material#kBlurR} and
 * {@link primitives.Material#kBlurT} control the beam spread for glossy reflection
 * and diffuse transparency respectively. {@code 0} = ideal (no blur).</p>
 */
class SimpleRayTracer extends RayTracerBase {

    // ──────────────────────────────────────────────────────────────────────────
    //  Constants
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Maximum recursion depth for global-illumination (reflection / refraction) calculations.
     * Recursion stops when the level reaches 1.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Minimum attenuation threshold below which a contribution is negligible
     * and further recursion is abandoned.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /**
     * Initial (un-attenuated) coefficient at the start of the recursive color computation.
     */
    private static final Double3 INITIAL_K = Double3.ONE;

    /**
     * Default number of shadow-beam sample points used per shading point when
     * rendering soft shadows for an area light.
     * <p>Recommended values: 4 (debug), 9 (draft), 81 (demo), 289–1089 (final).</p>
     */
    private static final int DEFAULT_SHADOW_SAMPLES = 81;

    /**
     * Default number of secondary rays in a glossy-reflection or diffuse-transparency beam.
     * <p>Recommended values: same as shadow samples; can be tuned independently.</p>
     */
    private static final int DEFAULT_GLOSSY_SAMPLES = 81;

    /**
     * Distance from the surface point along the ideal secondary ray at which the
     * virtual target sampling area is placed for glossy / diffuse glass effects.
     * <p>
     * With {@code TARGET_DISTANCE = 100}, the material blur parameter
     * ({@code kBlurR} or {@code kBlurT}) is interpreted as a gradient percentage:
     * {@code blur = 5} spreads the beam ±5 units at 100 units depth, equivalent to
     * a half-angle of {@code arctan(5/100) ≈ 2.86°}.
     * </p>
     */
    private static final double TARGET_DISTANCE = 100.0;

    // ──────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Constructs a ray tracer for the given scene.
     *
     * @param scene the scene to render
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Ray-tracing entry point
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Traces a single ray through the scene and returns the computed color.
     *
     * @param ray the ray to trace
     * @return the color seen along the ray, or the scene background if no hit
     */
    @Override
    Color traceRay(Ray ray) {
        Intersection closest = findClosestIntersection(ray);
        if (closest == null) return _scene.background;
        return calcColor(closest, ray);
    }

    /**
     * Finds the closest intersection of a ray with all scene geometries.
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
    //  Color computation (recursive)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Entry point for color calculation at an intersection. Adds ambient light
     * once, pre-processes the intersection data, and starts recursion.
     *
     * @param gp  the intersection to shade
     * @param ray the camera ray that reached this intersection
     * @return the shaded color at the intersection point
     */
    private Color calcColor(Intersection gp, Ray ray) {
        Color ambient = _scene.ambientLight.getIntensity().scale(gp.material.kA);
        if (!preprocessIntersection(gp, ray.direction()))
            return ambient.add(gp.geometry.getEmission());
        return ambient.add(calcColor(gp, MAX_CALC_COLOR_LEVEL, INITIAL_K));
    }

    /**
     * Recursive color calculation combining emission, local, and global effects.
     *
     * @param intersection the current surface intersection
     * @param level        remaining recursion depth
     * @param k            accumulated attenuation factor
     * @return the color contribution at this intersection
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = intersection.geometry.getEmission()
                .add(calcColorLocalEffects(intersection, k));
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Computes local (per-light) Phong shading, incorporating partial-transparency
     * shadows or soft shadows for area lights.
     *
     * @param intersection the current intersection
     * @param k            accumulated attenuation factor
     * @return the summed local color contribution from all light sources
     */
    private Color calcColorLocalEffects(Intersection intersection, Double3 k) {
        Color color = Color.BLACK;
        for (LightSource lightSource : _scene.lights) {
            if (!preprocessLightSource(intersection, lightSource)) continue;
            Double3 ktr = transparency(intersection);
            if (!ktr.product(k).isLowerThan(MIN_CALC_COLOR_K))
                color = color.add(
                        intersection.iL.scale(ktr).scale(
                                calcDiffuse(intersection).add(calcSpecular(intersection))));
        }
        return color;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Global effects — reflection and refraction
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes recursive reflection and refraction (global) color contributions.
     * <p>
     * For each effect the material blur parameter selects the rendering mode:
     * <ul>
     *   <li>{@code kBlurR == 0} — perfect mirror: single ideal reflection ray.</li>
     *   <li>{@code kBlurR  > 0} — glossy surface: beam of rays spread around the
     *       ideal reflection direction via {@link #calcGlobalEffectBeam}.</li>
     *   <li>{@code kBlurT == 0} — clear glass: single ideal refraction ray.</li>
     *   <li>{@code kBlurT  > 0} — diffuse (blurry) glass: beam of rays spread
     *       around the incoming ray direction via {@link #calcGlobalEffectBeam}.</li>
     * </ul>
     *
     * @param gp    the current intersection (must have {@code n}, {@code v}, {@code vn} set)
     * @param level remaining recursion depth
     * @param k     accumulated attenuation factor
     * @return the combined global color contribution
     */
    private Color calcGlobalEffects(Intersection gp, int level, Double3 k) {
        Color color = Color.BLACK;

        // ── Reflection ────────────────────────────────────────────────────────
        Double3 kr = gp.material.kR;
        if (!kr.product(k).isLowerThan(MIN_CALC_COLOR_K)) {
            // Ideal specular-reflection direction: r = v − 2(v·n)n
            Vector r = gp.v.subtract(gp.n.scale(2.0 * gp.v.dotProduct(gp.n)));

            if (Util.isZero(gp.material.kBlurR)) {
                // Perfect mirror — single ideal ray
                color = color.add(calcGlobalEffect(new Ray(gp.point, r, gp.n), level, kr, k));
            } else {
                // Glossy surface — beam of rays around the ideal reflection direction
                color = color.add(calcGlobalEffectBeam(gp, r, gp.material.kBlurR, level, kr, k));
            }
        }

        // ── Refraction (transparency) ─────────────────────────────────────────
        Double3 kt = gp.material.kT;
        if (!kt.product(k).isLowerThan(MIN_CALC_COLOR_K)) {
            // Ideal refraction continues along the incoming ray direction (v)
            if (Util.isZero(gp.material.kBlurT)) {
                // Clear glass — single ideal ray
                color = color.add(calcGlobalEffect(new Ray(gp.point, gp.v, gp.n), level, kt, k));
            } else {
                // Diffuse glass — beam of rays around the incoming direction
                color = color.add(calcGlobalEffectBeam(gp, gp.v, gp.material.kBlurT, level, kt, k));
            }
        }

        return color;
    }

    /**
     * Traces a single secondary (reflection or refraction) ray and returns its
     * attenuated color contribution.
     *
     * @param ray   the secondary ray to trace
     * @param level remaining recursion depth
     * @param kx    material coefficient for this effect ({@code kR} or {@code kT})
     * @param k     accumulated attenuation factor
     * @return the attenuated color contribution, already scaled by {@code kx}
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 kx, Double3 k) {
        Intersection closest = findClosestIntersection(ray);
        if (closest == null) return _scene.background.scale(kx);
        if (!preprocessIntersection(closest, ray.direction()))
            return closest.geometry.getEmission().scale(kx);
        return calcColor(closest, level - 1, k.product(kx)).scale(kx);
    }

    /**
     * Computes a glossy reflection or diffuse transparency contribution by tracing
     * a beam of rays distributed around the ideal secondary ray direction.
     *
     * <h4>Algorithm</h4>
     * <ol>
     *   <li>A virtual target area is placed on the ideal secondary ray at distance
     *       {@link #TARGET_DISTANCE}; its half-extent equals {@code blur}.</li>
     *   <li>A {@link Blackboard} generates {@link #DEFAULT_GLOSSY_SAMPLES} sample
     *       points across that area, with a basis orthogonal to {@code idealDir}.</li>
     *   <li>For each sample, a secondary ray is cast from the surface point toward
     *       the sample.  Rays that cross to the wrong side of the surface
     *       ({@code n · sampleDir} opposite sign to {@code n · idealDir}) are
     *       discarded — preventing light from leaking through at grazing angles.</li>
     *   <li>The colors of all valid rays (each already attenuated by {@code kx}
     *       inside {@link #calcGlobalEffect}) are averaged.  If no valid sample
     *       exists, the single ideal ray is used as a fallback.</li>
     * </ol>
     *
     * <p><b>Note:</b> unlike soft shadows, wrong-side samples are excluded from
     * <em>both</em> numerator and denominator, because for glossy surfaces the
     * average represents the fraction of the visible cone — not the fraction of a
     * light disk.</p>
     *
     * @param gp       the current intersection
     * @param idealDir the ideal secondary ray direction (normalized)
     * @param blur     the material blur parameter ({@code kBlurR} or {@code kBlurT});
     *                 interpreted as the target area half-extent at {@link #TARGET_DISTANCE}
     * @param level    remaining recursion depth
     * @param kx       material effect coefficient ({@code kR} or {@code kT})
     * @param k        accumulated attenuation factor
     * @return the averaged attenuated color contribution of the beam
     */
    private Color calcGlobalEffectBeam(Intersection gp, Vector idealDir, double blur,
                                       int level, Double3 kx, Double3 k) {
        // Place the virtual target area along the ideal secondary ray
        Point targetCenter = gp.point.add(idealDir.scale(TARGET_DISTANCE));

        // Build the sampling region orthogonal to the ideal ray direction
        List<Point> samples = new Blackboard()
                .setCenter(targetCenter)
                .setSize(blur)                    // half-extent at TARGET_DISTANCE
                .setNumSamples(DEFAULT_GLOSSY_SAMPLES)
                .buildBasis(idealDir)             // area normal = ideal direction
                .getSamplePoints();

        // Reference: sign of n · idealDir tells which surface side is "correct"
        double idealDotN = Util.alignZero(gp.n.dotProduct(idealDir));

        Color color = Color.BLACK;
        int validCount = 0;

        for (Point sample : samples) {
            // Vector from surface point to this target sample
            Vector dirToSample = sample.subtract(gp.point);

            // Filter: discard rays that cross to the wrong surface side
            // (n · dirToSample must share the sign of n · idealDir)
            double sampleDotN = Util.alignZero(gp.n.dotProduct(dirToSample));
            if (idealDotN * sampleDotN <= 0) continue; // wrong side — skip

            // Trace this secondary ray; calcGlobalEffect scales result by kx
            Ray beamRay = new Ray(gp.point, dirToSample, gp.n);
            color = color.add(calcGlobalEffect(beamRay, level, kx, k));
            validCount++;
        }

        // Fallback: if all samples filtered (extreme grazing angle) use the ideal ray
        if (validCount == 0)
            return calcGlobalEffect(new Ray(gp.point, idealDir, gp.n), level, kx, k);

        // Average over valid-sample colors only
        return color.reduce(validCount);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Phong components
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes the diffuse (Lambertian) lighting component: {@code kD × |n·l|}.
     *
     * @param intersection the current intersection (with pre-computed {@code nl})
     * @return the diffuse attenuation factor
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.nl));
    }

    /**
     * Computes the specular (Phong) lighting component:
     * {@code kS × max(0, −v·r)^nShininess}.
     *
     * @param intersection the current intersection
     * @return the specular attenuation factor
     */
    private Double3 calcSpecular(Intersection intersection) {
        var r = intersection.l.subtract(intersection.n.scale(2.0 * intersection.nl));
        double vr = -intersection.v.dotProduct(r);
        if (vr <= 0) return Double3.ZERO;
        return intersection.material.kS.scale(Math.pow(vr, intersection.material.nShininess));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Shadows — dispatcher
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes the accumulated transparency factor (ktr) from the shaded point
     * to the current light source.
     * <p>
     * Dispatches to {@link #singleRayTransparency} when {@code lightSize == 0}
     * (point/directional light, hard shadow) or {@link #softShadowTransparency}
     * when {@code lightSize > 0} (area light, soft shadow with penumbra).
     *
     * @param intersection the current intersection (light cache must be populated)
     * @return kT factor: {@link Double3#ONE} = unblocked, {@link Double3#ZERO} = full shadow
     */
    private Double3 transparency(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        double maxDistance = intersection.light.getDistance(intersection.point);
        double lightSize = intersection.light.getSize();

        if (Util.isZero(lightSize))
            return singleRayTransparency(intersection.point, pointToLight,
                    intersection.n, maxDistance);

        return softShadowTransparency(intersection, pointToLight, maxDistance, lightSize);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Shadows — single-ray helper
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Casts a single shadow ray and returns the accumulated transparency factor
     * of every geometry it passes through up to {@code maxDistance}.
     *
     * @param origin      surface point from which the shadow ray is fired
     * @param direction   direction toward the light source (or sample point)
     * @param normal      surface normal, used to offset the ray origin by ±DELTA
     * @param maxDistance maximum distance to check for blocking geometry
     * @return {@link Double3#ONE} if unblocked, {@link Double3#ZERO} if fully
     * blocked, or a component-wise product of blocking materials' kT values
     */
    private Double3 singleRayTransparency(Point origin, Vector direction,
                                          Vector normal, double maxDistance) {
        Ray shadowRay = new Ray(origin, direction, normal);
        var hits = _scene.geometries.calcIntersections(shadowRay, maxDistance);

        if (hits == null) return Double3.ONE;

        Double3 ktr = Double3.ONE;
        for (Intersection si : hits) {
            ktr = ktr.product(si.material.kT);
            if (ktr.isLowerThan(MIN_CALC_COLOR_K)) return Double3.ZERO;
        }
        return ktr;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Shadows — soft-shadow beam helper
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Computes soft shadows by sampling {@link #DEFAULT_SHADOW_SAMPLES} points on
     * the light source area and averaging the per-sample transparency factors.
     * <p>
     * The target area is centered at the light position with its basis orthogonal
     * to {@code intersection.l}.  Wrong-side samples (the "sunset effect") contribute
     * {@code 0} ktr but are counted in the denominator, as required by the spec.
     *
     * @param intersection the current intersection (light cache must be populated)
     * @param pointToLight direction from surface toward the light center
     * @param maxDistance  distance from surface to the light center
     * @param lightSize    radius of the light source area (must be &gt; 0)
     * @return the averaged ktr across all samples
     */
    private Double3 softShadowTransparency(Intersection intersection,
                                           Vector pointToLight,
                                           double maxDistance,
                                           double lightSize) {
        Point lightCenter = intersection.point.add(pointToLight.scale(maxDistance));

        List<Point> lightSamples = new Blackboard()
                .setCenter(lightCenter)
                .setSize(lightSize)
                .setNumSamples(DEFAULT_SHADOW_SAMPLES)
                .buildBasis(intersection.l)
                .getSamplePoints();

        if (lightSamples.isEmpty()) return Double3.ONE;

        double signRef = Util.alignZero(intersection.n.dotProduct(pointToLight));

        Double3 ktrSum = Double3.ZERO;
        for (Point sample : lightSamples) {
            Vector rawDir = sample.subtract(intersection.point);
            double sampleDotN = Util.alignZero(intersection.n.dotProduct(rawDir));
            if (signRef * sampleDotN <= 0) continue;  // wrong side — zero contribution

            double sampleDist = rawDir.length();
            ktrSum = ktrSum.add(
                    singleRayTransparency(intersection.point, rawDir,
                            intersection.n, sampleDist));
        }

        return ktrSum.divide(lightSamples.size());
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Kept for professor evaluation: Stage-8 binary shadow (Part I approach)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Checks whether the shaded point receives any light from the current source
     * using a single binary shadow test.  Retained alongside {@link #transparency}
     * so the professor can evaluate the Part I and Part II shadow approaches.
     *
     * @param intersection the current intersection (light cache must be populated)
     * @return {@code true} if the point is not fully in shadow
     */
    private boolean unshaded(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.n);
        var shadowIntersections = _scene.geometries.calcIntersections(
                shadowRay, intersection.light.getDistance(intersection.point));
        if (shadowIntersections == null) return true;
        for (Intersection si : shadowIntersections)
            if (si.material.kT.isLowerThan(MIN_CALC_COLOR_K)) return false;
        return true;
    }
}