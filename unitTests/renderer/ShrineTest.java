package renderer;

import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import parser.BlenderMeshLoader;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Renders the Blender "zen shrine" mesh (~685k triangles) using the BVH
 * acceleration. Without the hierarchy this scene would be unrenderable; with it,
 * the ray-vs-box pruning brings every ray down from O(n) to roughly O(log n).
 */
class ShrineTest {

    /**
     * Default constructor to satisfy the JavaDoc generator.
     */
    ShrineTest() {
    }

    /**
     * Loads the colored mesh, builds the BVH, lights the scene and renders it.
     * Requires a large heap — run with VM options {@code -Xms1G -Xmx8G}.
     */
    @Test
    void renderShrine() {
        long t0 = System.currentTimeMillis();

        Scene scene = BlenderMeshLoader.loadShrine("scenes/blender_export.json")
                .setBackground(new Color(10, 12, 20))
                .setAmbientLight(new AmbientLight(new Color(18, 18, 24)));

        // Warm key light from the upper front-left — the main shaper
        scene.lights.add(new DirectionalLight(
                new Color(190, 178, 150), new Vector(-0.4, -1, -0.5)));

        // Cool sky fill from the upper left — fills shadows with blue
        scene.lights.add(new PointLight(
                new Color(70, 95, 150), new Point(-400, 450, 500))
                .setKl(0.0002).setKq(0.000015));

        // Warm rim light from behind-right — separates the shrine from the background
        scene.lights.add(new PointLight(
                new Color(180, 120, 65), new Point(450, 280, -350))
                .setKl(0.0003).setKq(0.00002));

        // Warm lantern accent low at the front — a cozy glow near the base
        scene.lights.add(new PointLight(
                new Color(210, 140, 60), new Point(40, 70, 180))
                .setKl(0.0006).setKq(0.00006));

        // Switch acceleration ON (the toggle is just this call)
        long tb = System.currentTimeMillis();
        scene.geometries.buildHierarchy();
        System.out.printf("[Shrine] BVH built in %.1f s%n",
                (System.currentTimeMillis() - tb) / 1000.0);

        Camera.getBuilder()
                .setLocation(new Point(32, 228, 868))
                .setDirection(new Point(32, 127, -80), new Vector(0, 1, 0))
                .setVpSize(500, 500)
                .setVpDistance(500)
                .setResolution(4000, 4000)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-1)
                .setDebugPrint(2)
                .build()
                .renderImage()
                .writeToImage("zen_shrine");

        System.out.printf("[Shrine] total time %.1f s%n",
                (System.currentTimeMillis() - t0) / 1000.0);
    }
}