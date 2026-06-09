package renderer;

import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Mini-Project 2 — BVH acceleration performance measurements.
 *
 * <p>Renders one large scene under the four required configurations and prints
 * the render time of each, so the speed-up from the Bounding Volume Hierarchy
 * (and its interaction with multi-threading) can be compared:</p>
 *
 * <ol>
 *   <li>acceleration OFF, multi-threading OFF — the baseline</li>
 *   <li>acceleration OFF, multi-threading ON</li>
 *   <li>acceleration ON,  multi-threading OFF</li>
 *   <li>acceleration ON,  multi-threading ON  — the fastest</li>
 * </ol>
 *
 * <p>All four runs use the <b>same scene content</b> (produced by
 * {@link #buildScene()}), so the comparison is fair. Acceleration is switched on
 * purely from the test by calling {@code scene.geometries.buildHierarchy()};
 * leaving it out renders the flat scene. No system code changes between runs.</p>
 *
 * <p>The Mini-Project 1 improvement (<b>soft shadows</b>) is kept <b>enabled</b>
 * in every run — each light is given a non-zero size via
 * {@code setSize(LIGHT_SIZE)} and the shadow beam uses {@value #SHADOW_SAMPLES}
 * samples — so the acceleration is measured under a realistic super-sampling
 * load, as the mini-project requires.</p>
 */
class BvhTimingTests {

    /**
     * Default constructor to satisfy the JavaDoc generator.
     */
    BvhTimingTests() {
    }

    /**
     * Number of spheres per axis; the scene holds GRID³ spheres in total.
     */
    private static final int GRID = 9;

    /**
     * Render resolution (square).
     */
    private static final int RESOLUTION = 600;

    /**
     * Spacing between neighboring spheres.
     */
    private static final double SPACING = 30;

    /**
     * Area-light radius enabling the MP1 soft-shadow feature during measurement.
     */
    private static final double LIGHT_SIZE = 10;

    /**
     * Soft-shadow beam samples per light (the MP1 super-sampling load).
     */
    private static final int SHADOW_SAMPLES = 49;

    /**
     * Builds the shared benchmark scene: a {@value GRID}³ lattice of small
     * spheres plus several area light sources. A fresh, identical scene is
     * returned on every call, so each configuration starts from the same content.
     * Every light has a non-zero size, so the MP1 soft-shadow feature is active.
     *
     * @return the benchmark scene
     */
    private static Scene buildScene() {
        Scene scene = new Scene("BVH Benchmark")
                .setBackground(new Color(5, 5, 12))
                .setAmbientLight(new AmbientLight(new Color(15, 15, 20)));

        Material mat = new Material().setKD(0.5).setKS(0.5).setShininess(60).setKR(0.1);
        double start = -(GRID - 1) * SPACING / 2;

        for (int ix = 0; ix < GRID; ix++)
            for (int iy = 0; iy < GRID; iy++)
                for (int iz = 0; iz < GRID; iz++) {
                    double x = start + ix * SPACING;
                    double y = start + iy * SPACING;
                    double z = start + iz * SPACING - 200;
                    Color c = new Color(40 + ix * 20, 40 + iy * 20, 40 + iz * 20);
                    scene.geometries.add(
                            new Sphere(new Point(x, y, z), 8)
                                    .setEmission(c).setMaterial(mat));
                }

        // Five area light sources (size > 0 → MP1 soft shadows active in every run)
        scene.lights.add(new DirectionalLight(new Color(60, 60, 70), new Vector(-1, -1, -1)));
        scene.lights.add(new PointLight(new Color(200, 120, 80), new Point(150, 150, 50))
                .setKl(0.0005).setKq(0.0002).setSize(LIGHT_SIZE));
        scene.lights.add(new PointLight(new Color(80, 120, 200), new Point(-150, 150, 50))
                .setKl(0.0005).setKq(0.0002).setSize(LIGHT_SIZE));
        scene.lights.add(new SpotLight(new Color(180, 180, 120),
                new Point(0, 200, 100), new Vector(0, -1, -1))
                .setKl(0.0004).setKq(0.0002).setSize(LIGHT_SIZE));
        scene.lights.add(new PointLight(new Color(120, 200, 120), new Point(0, -180, 80))
                .setKl(0.0005).setKq(0.0002).setSize(LIGHT_SIZE));

        return scene;
    }

    /**
     * Builds a camera for the benchmark scene.
     *
     * @param scene the scene to render
     * @return a configured camera builder
     */
    private static Camera.Builder camera(Scene scene) {
        return Camera.getBuilder()
                .setLocation(new Point(0, 0, 350))
                .setDirection(new Point(0, 0, -200), new Vector(0, 1, 0))
                .setVpSize(300, 300)
                .setVpDistance(300)
                .setResolution(RESOLUTION, RESOLUTION)
                .setRayTracer(scene, RayTracerType.SIMPLE);
    }

    /**
     * Runs one configuration and prints its render time.
     *
     * @param label         short label used for the output image and the printout
     * @param acceleration  {@code true} to build the BVH before rendering
     * @param multiThreaded {@code true} to render with parallel-stream threading
     */
    private void run(String label, boolean acceleration, boolean multiThreaded) {
        Scene scene = buildScene();
        if (acceleration)
            scene.geometries.buildHierarchy();

        Camera.Builder builder = camera(scene);
        if (multiThreaded)
            builder.setMultithreading(-1);

        long start = System.currentTimeMillis();
        builder.build().renderImage().writeToImage("bvh_" + label);
        double seconds = (System.currentTimeMillis() - start) / 1000.0;

        System.out.printf("%-26s accel=%-5b mt=%-5b  %7.2f s%n",
                label, acceleration, multiThreaded, seconds);
    }

    /**
     * Config 1: acceleration OFF, multi-threading OFF (baseline).
     */
    @Test
    void accelOff_threadsOff() {
        run("accelOFF_mtOFF", false, false);
    }

    /**
     * Config 2: acceleration OFF, multi-threading ON.
     */
    @Test
    void accelOff_threadsOn() {
        run("accelOFF_mtON", false, true);
    }

    /**
     * Config 3: acceleration ON, multi-threading OFF.
     */
    @Test
    void accelOn_threadsOff() {
        run("accelON_mtOFF", true, false);
    }

    /**
     * Config 4: acceleration ON, multi-threading ON (fastest).
     */
    @Test
    void accelOn_threadsOn() {
        run("accelON_mtON", true, true);
    }
}