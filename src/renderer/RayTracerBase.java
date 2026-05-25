package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Abstract base class for all ray-tracing strategies.
 * <p>
 * Holds the scene and provides two protected helper methods that populate
 * the per-intersection and per-light-source cache fields of an
 * {@link Intersection} object, so that concrete tracers can read those
 * values without recomputing them.
 */
abstract class RayTracerBase {

    /**
     * The scene being rendered.
     */
    protected final Scene _scene;

    /**
     * Constructs a ray tracer for the given scene.
     *
     * @param scene the scene to render
     */
    RayTracerBase(Scene scene) {
        this._scene = scene;
    }

    /**
     * Traces the given ray and returns the color seen along it.
     *
     * @param ray the ray to trace
     * @return the color seen along the ray
     */
    abstract Color traceRay(Ray ray);

    // ──────────────────────────────────────────────────────────────────────────
    //  Pre-processing helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Pre-computes and caches the per-intersection data needed for shading:
     * <ul>
     *   <li>{@code intersection.n}  – surface normal at the hit point</li>
     *   <li>{@code intersection.v}  – the ray's direction vector</li>
     *   <li>{@code intersection.vn} – dot product {@code n · v}</li>
     * </ul>
     *
     * @param intersection the intersection to populate
     * @param rayDirection the direction of the camera ray
     * @return {@code true} if shading should proceed (i.e. the ray is not
     * grazing the surface); {@code false} if {@code n · v == 0}
     */
    protected boolean preprocessIntersection(Intersection intersection, Vector rayDirection) {
        intersection.n = intersection.geometry.getNormal(intersection.point);
        intersection.v = rayDirection;
        intersection.vn = alignZero(intersection.n.dotProduct(intersection.v));
        return !isZero(intersection.vn);
    }

    /**
     * Pre-computes and caches the per-light-source data needed for shading:
     * <ul>
     *   <li>{@code intersection.l}  – direction from the light to the hit point</li>
     *   <li>{@code intersection.nl} – dot product {@code n · l}</li>
     *   <li>{@code intersection.iL} – light intensity at the hit point</li>
     * </ul>
     * <p>
     * Call this only after {@link #preprocessIntersection} has been called
     * (so that {@code intersection.n} and {@code intersection.vn} are set).
     *
     * @param intersection the intersection to populate
     * @param lightSource  the current light source
     * @return {@code true} if the light contributes to this point
     * (i.e. the light and the camera are on the same side of the surface);
     * {@code false} otherwise
     */
    protected boolean preprocessLightSource(Intersection intersection, LightSource lightSource) {
        intersection.l = lightSource.getL(intersection.point);
        intersection.nl = alignZero(intersection.n.dotProduct(intersection.l));
        if (intersection.nl * intersection.vn <= 0) return false;
        intersection.iL = lightSource.getIntensity(intersection.point);
        intersection.light = lightSource;
        return true;
    }
}