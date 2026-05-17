package renderer;

import geometries.api.Intersectable;
import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Abstract base class for all ray tracers.
 * Defines the core functionality of tracing a ray through a scene.
 */
abstract class RayTracerBase {
    protected final Scene _scene;

    public RayTracerBase(Scene scene) {
        this._scene = scene;
    }

    abstract Color traceRay(Ray ray);

    /**
     * Initialise les données de cache liées à l'intersection (v, n, nv).
     * @return true si la géométrie peut être éclairée (nv != 0), false sinon.
     */
    protected boolean preprocessIntersection(Intersectable.GeoPoint gp, primitives.Vector v) {
        gp.v = v;
        gp.n = gp.geometry.getNormal(gp.point);
        gp.nv = primitives.Util.alignZero(gp.n.dotProduct(gp.v));
        return gp.nv != 0;
    }

    /**
     * Vérifie si la source de lumière éclaire le point d'intersection.
     * @return true si la lumière et la caméra sont du même côté de la surface.
     */
    protected boolean preprocessLightSource(Intersectable.GeoPoint gp, lighting.LightSource light) {
        primitives.Vector l = light.getL(gp.point);
        double nl = primitives.Util.alignZero(gp.n.dotProduct(l));
        // La lumière éclaire le point si l et v sont du même côté (signes identiques)
        return nl * gp.nv > 0;
    }
}