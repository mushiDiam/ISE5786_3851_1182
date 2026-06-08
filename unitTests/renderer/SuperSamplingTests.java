package renderer;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * Mini-Project 1 — Super-sampling demonstration tests.
 *
 * <p>Demonstrates <b>Soft Shadows</b> via area-light super-sampling.
 * Each test renders the same scene with identical camera and geometry settings;
 * the only difference is whether {@link PointLight#setSize(double)} and
 * {@link SpotLight#setSize(double)} are called on the light sources.</p>
 *
 * <p>Requirements satisfied:
 * <ul>
 *   <li>≥ 10 distinct geometric bodies</li>
 *   <li>≥ 3 light sources at different positions</li>
 *   <li>Two test methods for the same scene: one without the improvement,
 *       one with the improvement</li>
 *   <li>Distinct output file names for easy visual comparison</li>
 *   <li>Console render-time output for both runs</li>
 * </ul>
 *
 * <h2>Scene description — "The Gallery"</h2>
 * <p>A room with a polished floor, three walls, and a collection of objects.
 * Three area lights illuminate the room from different angles to create
 * clearly visible overlapping penumbra regions when soft shadows are enabled.</p>
 *
 * <h2>How to read the output images</h2>
 * <ul>
 *   <li>{@code softShadows_OFF.png} — sharp, hard-edged shadow boundaries.</li>
 *   <li>{@code softShadows_ON.png}  — soft, gradient penumbra at shadow edges,
 *       especially visible around the sphere and pyramid bases.</li>
 * </ul>
 */
class SuperSamplingTests {

    /**
     * Default constructor to satisfy the JavaDoc generator.
     */
    SuperSamplingTests() {
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Shared scene constants
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Render resolution — use 800 for final images, 400 for quick drafts.
     */
    private static final int RESOLUTION = 4000;

    /**
     * Number of shadow-beam samples per light when soft shadows are enabled.
     */
    private static final int SHADOW_SAMPLES = 81;

    /**
     * Radius of the main overhead area light.
     * Set to 0 for hard shadows, >0 for soft shadows.
     */
    private static final double MAIN_LIGHT_SIZE = 15;
    /**
     * Radius of the fill area light.
     */
    private static final double FILL_LIGHT_SIZE = 10;
    /**
     * Radius of the accent area light.
     */
    private static final double ACCENT_LIGHT_SIZE = 8;

    // ── Materials ──────────────────────────────────────────────────────────────

    /**
     * Polished reflective floor.
     */
    private static final Material FLOOR_MAT = new Material()
            .setKD(0.4).setKS(0.5).setShininess(80).setKR(0.15);

    /**
     * Plain matte wall.
     */
    private static final Material WALL_MAT = new Material()
            .setKD(0.7).setKS(0.1).setShininess(10);

    /**
     * Glossy metallic sphere.
     */
    private static final Material METAL_MAT = new Material()
            .setKD(0.2).setKS(0.7).setShininess(200).setKR(0.6);

    /**
     * Translucent blue glass.
     */
    private static final Material GLASS_MAT = new Material()
            .setKD(0.1).setKS(0.5).setShininess(150).setKT(0.7).setKR(0.1);

    /**
     * Opaque red matte.
     */
    private static final Material RED_MAT = new Material()
            .setKD(0.6).setKS(0.3).setShininess(40);

    /**
     * Opaque green.
     */
    private static final Material GREEN_MAT = new Material()
            .setKD(0.6).setKS(0.2).setShininess(30);

    /**
     * White matte pedestal.
     */
    private static final Material WHITE_MAT = new Material()
            .setKD(0.8).setKS(0.1).setShininess(10);

    /**
     * Yellow-gold shiny.
     */
    private static final Material GOLD_MAT = new Material()
            .setKD(0.3).setKS(0.7).setShininess(120).setKR(0.2);

    /**
     * Purple matte.
     */
    private static final Material PURPLE_MAT = new Material()
            .setKD(0.7).setKS(0.2).setShininess(20);

    // ──────────────────────────────────────────────────────────────────────────
    //  Tests — without improvement (hard shadows)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Renders the showcase scene with point-source lights (size = 0), producing
     * hard, perfectly sharp shadow edges.
     *
     * <p>Use this image as the <b>before</b> reference when comparing with
     * {@link #testSoftShadowsOn()}.</p>
     */
    @Test
    void testSoftShadowsOff() {
        Scene scene = buildScene(false);

        long start = System.currentTimeMillis();

        Camera.getBuilder()
                .setLocation(new Point(80, 135, 350))
                .setDirection(new Point(0, -15, -15), new Vector(0, 1, 0))
                .setVpSize(200, 200)
                .setVpDistance(250)
                .setResolution(RESOLUTION, RESOLUTION)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                // Multi-threading enabled for both tests so render time difference
                // reflects only the cost of soft shadows, not single vs multi thread
                .setMultithreading(-1)
                .setDebugPrint(5)
                .build()
                .renderImage()
                .writeToImage("softShadows_OFF");

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("[OFF] Render time: %.2f seconds%n", elapsed / 1000.0);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Tests — with improvement (soft shadows)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Renders the showcase scene with area lights (size &gt; 0), producing soft
     * shadow edges with realistic umbra / penumbra transitions.
     *
     * <p>Uses {@value #SHADOW_SAMPLES} shadow samples per light ({@code DEFAULT_SHADOW_SAMPLES}
     * in {@code SimpleRayTracer}). The render time will be noticeably longer than
     * {@link #testSoftShadowsOff()} due to the additional shadow rays.</p>
     */
    @Test
    void testSoftShadowsOn() {
        Scene scene = buildScene(true);

        long start = System.currentTimeMillis();

        Camera.getBuilder()
                .setLocation(new Point(80, 135, 350))
                .setDirection(new Point(0, -15, -15), new Vector(0, 1, 0))
                .setVpSize(200, 200)
                .setVpDistance(250)
                .setResolution(RESOLUTION, RESOLUTION)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-2)
                .setDebugPrint(5)
                .build()
                .renderImage()
                .writeToImage("softShadows_ON");

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("[ON]  Render time: %.2f seconds%n", elapsed / 1000.0);
        System.out.printf("      (%d shadow samples per light, %d lights)%n",
                SHADOW_SAMPLES, 3);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Scene builder
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Builds "The Gallery" scene: 11 geometric bodies and 3 light sources.
     *
     * <p>The only difference between the two tests is the {@code softShadows} flag.
     * When {@code true}, each light source receives a non-zero size via
     * {@link PointLight#setSize(double)} / {@link SpotLight#setSize(double)},
     * enabling area-light soft-shadow sampling in {@code SimpleRayTracer}.</p>
     *
     * <h4>Geometry list</h4>
     * <ol>
     *   <li>Floor (Plane, y = −80)</li>
     *   <li>Back wall (Plane, z = −200)</li>
     *   <li>Left wall (Plane, x = −160)</li>
     *   <li>Large central sphere — metallic</li>
     *   <li>Medium sphere, left — blue glass</li>
     *   <li>Small sphere, right — red opaque</li>
     *   <li>Small sphere, back — green opaque</li>
     *   <li>Tall cylinder leaning against the left wall</li>
     *   <li>Short cylinder (pedestal), centre-right</li>
     *   <li>Purple pyramid, on floor left</li>
     *   <li>Yellow pyramid, on floor right</li>
     * </ol>
     *
     * <h4>Light list</h4>
     * <ol>
     *   <li>Main overhead {@link PointLight} — warm white, large area</li>
     *   <li>Left fill {@link PointLight} — cool blue, medium area</li>
     *   <li>Right accent {@link SpotLight} — warm yellow, tight cone</li>
     * </ol>
     *
     * @param softShadows {@code true} to assign a non-zero size to each light
     *                    (area light → soft shadows); {@code false} for point
     *                    lights with size 0 (hard shadows)
     * @return the fully configured scene ready for rendering
     */
    private Scene buildScene(boolean softShadows) {
        Scene scene = new Scene("The Gallery")
                .setAmbientLight(new AmbientLight(new Color(10, 10, 13)));

        // ── Geometry 1: Floor ─────────────────────────────────────────────────
        scene.geometries.add(
                new Plane(new Point(0, -80, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(30, 30, 30))
                        .setMaterial(FLOOR_MAT)
        );

        // ── Geometry 2: Back wall ─────────────────────────────────────────────
        scene.geometries.add(
                new Plane(new Point(0, 0, -200), new Vector(0, 0, 1))
                        .setEmission(new Color(40, 40, 50))
                        .setMaterial(WALL_MAT)
        );

        // ── Geometry 3: Left wall ─────────────────────────────────────────────
        scene.geometries.add(
                new Plane(new Point(-160, 0, 0), new Vector(1, 0, 0))
                        .setEmission(new Color(50, 40, 40))
                        .setMaterial(WALL_MAT)
        );

        // ── Geometry 4: Large central sphere — metallic mirror ────────────────
        scene.geometries.add(
                new Sphere(new Point(0, -20, -60), 60)
                        .setEmission(new Color(40, 40, 45))
                        .setMaterial(METAL_MAT)
        );

        // ── Geometry 5: Medium sphere, left — blue glass ──────────────────────
        scene.geometries.add(
                new Sphere(new Point(-90, -55, -30), 25)
                        .setEmission(new Color(10, 20, 80))
                        .setMaterial(GLASS_MAT)
        );

        // ── Geometry 6: Small sphere, right — red opaque ──────────────────────
        scene.geometries.add(
                new Sphere(new Point(90, -65, 10), 15)
                        .setEmission(new Color(160, 20, 20))
                        .setMaterial(RED_MAT)
        );

        // ── Geometry 7: Small sphere, back — green ────────────────────────────
        scene.geometries.add(
                new Sphere(new Point(40, -68, -130), 12)
                        .setEmission(new Color(20, 130, 30))
                        .setMaterial(GREEN_MAT)
        );

        // ── Geometry 8: Tall cylinder leaning against the left wall ───────────
        //   Base on the floor; axis tilts ~14.5° toward the left wall (x = −160)
        //   so the top rests against it.
        scene.geometries.add(
                new Cylinder(12,
                        new Ray(new Point(-120, -80, -100), new Vector(-0.25, 0.97, 0)),
                        130)
                        .setEmission(new Color(140, 130, 100))
                        .setMaterial(GOLD_MAT)
        );

        // ── Geometry 9: Short cylinder (pedestal), centre-right ───────────────
        scene.geometries.add(
                new Cylinder(20,
                        new Ray(new Point(80, -80, -80), new Vector(0, 1, 0)),
                        30)
                        .setEmission(new Color(160, 160, 160))
                        .setMaterial(WHITE_MAT)
        );

        // ── Geometry 10: Purple pyramid on floor, left ────────────────────────
        //   Triangular base on the floor + apex above its centroid → 4 faces
        Point pBase1 = new Point(-60, -79, 20);
        Point pBase2 = new Point(-20, -79, 60);
        Point pBase3 = new Point(-100, -79, 70);
        Point pApex = new Point(-60, -36, 50);
        scene.geometries.add(
                new Triangle(pBase1, pBase2, pApex)
                        .setEmission(new Color(100, 30, 120)).setMaterial(PURPLE_MAT),
                new Triangle(pBase2, pBase3, pApex)
                        .setEmission(new Color(100, 30, 120)).setMaterial(PURPLE_MAT),
                new Triangle(pBase3, pBase1, pApex)
                        .setEmission(new Color(100, 30, 120)).setMaterial(PURPLE_MAT),
                new Triangle(pBase1, pBase2, pBase3)
                        .setEmission(new Color(100, 30, 120)).setMaterial(PURPLE_MAT)
        );

        // ── Geometry 11: Yellow pyramid on floor, right ──────────────────────
        Point yBase1 = new Point(50, -79, 40);
        Point yBase2 = new Point(120, -79, 20);
        Point yBase3 = new Point(100, -79, 90);
        Point yApex = new Point(90, -36, 50);
        scene.geometries.add(
                new Triangle(yBase1, yBase2, yApex)
                        .setEmission(new Color(180, 120, 10)).setMaterial(GOLD_MAT),
                new Triangle(yBase2, yBase3, yApex)
                        .setEmission(new Color(180, 120, 10)).setMaterial(GOLD_MAT),
                new Triangle(yBase3, yBase1, yApex)
                        .setEmission(new Color(180, 120, 10)).setMaterial(GOLD_MAT),
                new Triangle(yBase1, yBase2, yBase3)
                        .setEmission(new Color(180, 120, 10)).setMaterial(GOLD_MAT)
        );

        // ── Lights ────────────────────────────────────────────────────────────

        // Light 1: Main overhead — warm white, large area for wide penumbra
        PointLight mainLight = new PointLight(
                new Color(150, 140, 120),
                new Point(0, 180, -50))
                .setKl(0.0002).setKq(0.00002);
        if (softShadows) mainLight.setSize(MAIN_LIGHT_SIZE);
        scene.lights.add(mainLight);

        // Light 2: Left fill — cool blue, medium area for coloured soft shadow
        PointLight fillLight = new PointLight(
                new Color(50, 60, 130),
                new Point(-180, 100, 80))
                .setKl(0.0002).setKq(0.00002);
        if (softShadows) fillLight.setSize(FILL_LIGHT_SIZE);
        scene.lights.add(fillLight);

        // Light 3: Right accent — warm yellow spotlight, tight cone with soft edge
        SpotLight accentLight = new SpotLight(
                new Color(200, 150, 40),
                new Point(200, 150, 100),
                new Vector(-1, -0.8, -0.6))
                .setKl(0.0002).setKq(0.00002)
                .setNarrowBeam(3);
        if (softShadows) accentLight.setSize(ACCENT_LIGHT_SIZE);
        scene.lights.add(accentLight);

        // Light 4 (bonus): Directional background light — always hard shadow (size = 0)
        scene.lights.add(
                new DirectionalLight(new Color(12, 12, 20), new Vector(1, -0.5, -1))
        );

        return scene;
    }
}
//      [ON]  Render time: 9.19 seconds
//      (81 shadow samples per light, 3 lights)
//      ----------------------------------------
//      [OFF] Render time: 1.49 seconds