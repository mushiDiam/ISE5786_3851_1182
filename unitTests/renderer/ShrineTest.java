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
                .setBackground(new Color(120, 130, 145))      // light studio-grey background
                .setAmbientLight(new AmbientLight(new Color(55, 57, 65)));   // lifts the shadows

        // Bright warm key light from the upper front-left (front is the -Z side)
        scene.lights.add(new DirectionalLight(
                new Color(220, 210, 185), new Vector(0.35, -0.9, 0.45)));

        // Cool fill from the camera side so nothing facing us is black
        scene.lights.add(new PointLight(
                new Color(150, 165, 200), new Point(-300, 250, -650))
                .setKl(0.00008).setKq(0.0000025));

        // Soft overhead light for even top illumination
        scene.lights.add(new PointLight(
                new Color(170, 170, 180), new Point(40, 650, -120))
                .setKl(0.00015).setKq(0.000008));

        // Warm rim light from behind-right (behind is +Z) — separates shrine from background
        scene.lights.add(new PointLight(
                new Color(200, 140, 80), new Point(350, 280, 350))
                .setKl(0.0003).setKq(0.00002));

        // Switch acceleration ON (the toggle is just this call)
        long tb = System.currentTimeMillis();
        scene.geometries.buildHierarchy();
        System.out.printf("[Shrine] BVH built in %.1f s%n",
                (System.currentTimeMillis() - tb) / 1000.0);

        // 3/4 front view from the front-left, slightly above — close enough to fill the frame
        Camera.getBuilder()
                .setLocation(new Point(-230, 200, -680))
                .setDirection(new Point(32, 105, -80), new Vector(0, 1, 0))
                .setVpSize(500, 500)
                .setVpDistance(550)
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