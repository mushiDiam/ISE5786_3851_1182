package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Abstract base class for all ray tracers.
 * Defines the core functionality of tracing a ray through a scene.
 */
abstract class RayTracerBase {

    /**
     * The scene to be rendered.
     */
    protected final Scene _scene;

    /**
     * Constructs a RayTracerBase with a given scene.
     *
     * @param scene the scene to trace rays in
     */
    public RayTracerBase(Scene scene) {
        this._scene = scene;
    }

    /**
     * Traces a ray and calculates the color of the point it intersects.
     *
     * @param ray the ray to be traced
     * @return the color of the intersected point, or the background color if no intersection occurs
     */
    abstract Color traceRay(Ray ray);
}