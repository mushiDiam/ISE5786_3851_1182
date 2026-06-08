package renderer;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
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
 * Showcase scene for Bonus 1 and Bonus 2 of Stage 8.
 *
 * <p>This test builds a deep corner room showcase scene containing thirteen geometry
 * types and several lights, demonstrating global illumination, partial transparency,
 * and camera rotation.</p>
 *
 * <p>Multi-threading is enabled on every render via
 * {@code setMultithreading(-2)} (auto-detect cores minus 2 spares) and
 * {@code setDebugPrint(1)} (print progress every 1 %).</p>
 */
public class ShowcaseTests {

    /**
     * Default constructor required by the Javadoc generator.
     */
    public ShowcaseTests() {
    }

    /**
     * Builds and renders the bonus showcase scenes.
     * <p>
     * Two renders are produced:
     * <ul>
     *   <li>{@code Bonus1_EpicScene_MainAngle} — main camera angle.</li>
     *   <li>{@code Bonus2_EpicScene_TiltedAngle} — camera rotated 15° and shifted.</li>
     * </ul>
     * Render time for each pass is printed to the console.
     */
    @Test
    public void testBonus1And2Showcase() {
        Scene scene = new Scene("Showcase Scene");
        scene.setAmbientLight(new AmbientLight(new Color(15, 15, 15)));

        // Define materials
        Material glassMaterial = new Material().setKD(0.1).setKS(0.3).setShininess(20).setKT(0.7);
        Material mirrorMaterial = new Material().setKD(0.1).setKS(0.8).setShininess(60).setKR(0.85);
        Material matteMaterial = new Material().setKD(0.5).setKS(0.5).setShininess(30);
        Material floorMaterial = new Material().setKD(0.4).setKS(0.6).setShininess(50).setKR(0.2);
        // KS removed to eliminate the bright specular highlight on the wall
        Material slightlyReflectiveWall = new Material().setKD(0.5).setKS(0.0).setShininess(0).setKR(0.05);

        // ==========================================
        // ADDING 13 GEOMETRIES (Using all 6 types)
        // ==========================================
        scene.geometries.add(
                // 1. PLANE: The Floor (XZ Plane at y=-50)
                new Plane(new Point(0, -50, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(40, 40, 50))
                        .setMaterial(floorMaterial),

                // 2. PLANE: The Side Wall (Left YZ Plane at x=-100)
                new Plane(new Point(-100, 0, 0), new Vector(1, 0, 0))
                        .setEmission(new Color(30, 30, 40))
                        .setMaterial(matteMaterial),

                // 3. PLANE: The Back Wall (XY Plane at z=-150)
                new Plane(new Point(0, 0, -150), new Vector(0, 0, 1))
                        .setEmission(new Color(25, 25, 30))
                        .setMaterial(slightlyReflectiveWall),

                // 4. CYLINDER (Arrow Stem): Starts at the floor (-50) and rises 70 units to Y=20
                new Cylinder(8, new Ray(new Point(-40, -50, -100), new Vector(0, 1, 0)), 70)
                        .setEmission(new Color(20, 60, 20))
                        .setMaterial(matteMaterial),

                // 5. TRIANGLE (Arrow Head): Base sits at Y=20, tip at Y=40
                new Triangle(new Point(-40, 40, -100), new Point(-55, 20, -100), new Point(-25, 20, -100))
                        .setEmission(new Color(100, 80, 20))
                        .setMaterial(matteMaterial),

                // 6. TUBE: Infinite horizontal pipeline resting on the floor against the back wall
                new Tube(6, new Ray(new Point(-100, -44, -130), new Vector(1, 0, 0)))
                        .setEmission(new Color(60, 20, 20))
                        .setMaterial(matteMaterial),

                // 7. SPHERE: Mirror ball resting in the far corner
                new Sphere(new Point(60, -10, -110), 30)
                        .setEmission(new Color(10, 10, 10))
                        .setMaterial(mirrorMaterial),

                // 8. SPHERE: Glass ball floating in the center
                new Sphere(new Point(0, 10, -60), 25)
                        .setEmission(new Color(0, 0, 0))
                        .setMaterial(glassMaterial),

                // 9. POLYGON: Diamond hovering against the back wall
                new Polygon(new Point(0, 60, -145), new Point(30, 30, -145),
                        new Point(0, 0, -145), new Point(-30, 30, -145))
                        .setEmission(new Color(20, 20, 80))
                        .setMaterial(matteMaterial),

                // 10, 11, 12. SPHERES: Scattered colorful balls on the floor
                new Sphere(new Point(-20, -40, -30), 10)  // Red
                        .setEmission(new Color(150, 0, 0)).setMaterial(matteMaterial),
                new Sphere(new Point(20, -40, -40), 10)   // Green
                        .setEmission(new Color(0, 150, 0)).setMaterial(matteMaterial),
                new Sphere(new Point(0, -45, -15), 5)     // Small Blue
                        .setEmission(new Color(0, 0, 150)).setMaterial(matteMaterial)
        );

        // ==========================================
        // LIGHTS
        // ==========================================
        scene.lights.add(new SpotLight(new Color(250, 150, 150), new Point(40, 100, 50), new Vector(-1, -1, -1))
                .setKl(1E-5).setKq(1.5E-7));
        scene.lights.add(new PointLight(new Color(250, 250, 250), new Point(-50, 50, 50))
                .setKl(0.0005).setKq(0.00005));

        // ==========================================
        // CAMERA SETUP — multi-threading + progress output enabled
        // ==========================================
        Camera.Builder cameraBuilder = Camera.getBuilder()
                .setLocation(new Point(0, 30, 150))
                .setDirection(new Point(0, 0, -50))
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-2)   // auto-detect: availableProcessors() - 2 threads
                .setDebugPrint(1);       // print progress every 1 %

        // ==========================================
        // BONUS 1: RENDER THE MAIN EPIC SCENE
        // ==========================================
        System.out.println("[Bonus 1] Rendering main angle...");
        long start1 = System.currentTimeMillis();

        cameraBuilder.build()
                .renderImage()
                .writeToImage("Bonus1_EpicScene_MainAngle");

        System.out.printf("[Bonus 1] Done in %.2f s%n",
                (System.currentTimeMillis() - start1) / 1000.0);

        // ==========================================
        // BONUS 2: ROTATE AND MOVE THE CAMERA
        // ==========================================
        System.out.println("[Bonus 2] Rendering tilted angle...");
        long start2 = System.currentTimeMillis();

        cameraBuilder
                .setRotation(15)                          // tilt 15° clockwise
                .setLocation(new Point(40, 40, 120))      // shift right and up
                .setDirection(new Point(0, 0, -50))       // keep looking at centre
                .build()
                .renderImage()
                .writeToImage("Bonus2_EpicScene_TiltedAngle");

        System.out.printf("[Bonus 2] Done in %.2f s%n",
                (System.currentTimeMillis() - start2) / 1000.0);
    }

    /**
     * Builds and renders the complex interior scene featuring furniture, a decorative
     * mirror, windows, and complex warm/cool lighting (sun, lamp, fluorescents).
     * <p>
     * Multi-threading and debug printing are enabled so a long render stays visible
     * in the console.
     * </p>
     */
    @Test
    public void testComplexInteriorRoomScene() {
        Scene scene = new Scene("Complex Interior Showcase Scene");
        scene.setAmbientLight(new AmbientLight(new Color(15, 15, 20)));

        // --- Materials ---
        Material reflectiveFloorMaterial = new Material().setKD(0.6).setKS(0.2).setShininess(30).setKR(0.25);
        Material wallMaterial = new Material().setKD(0.6).setKS(0.1).setShininess(5);
        Material mirrorMaterial = new Material().setKD(0.0).setKS(0.1).setKR(0.95);
        Material goldFrameMaterial = new Material().setKD(0.4).setKS(0.8).setShininess(60);
        Material woodMaterial = new Material().setKD(0.7).setKS(0.1).setShininess(20);
        Material windowGlassMaterial = new Material().setKD(0.1).setKS(0.1).setShininess(0).setKT(0.85);
        Material matteFurnitureMaterial = new Material().setKD(0.7).setKS(0.0).setShininess(10);
        Material ballMaterial = new Material().setKD(0.5).setKS(0.5).setShininess(40).setKR(0.2);

        // ==========================================
        // 1. THE ROOM PLANES (Floor, Walls, Ceiling)
        // ==========================================
        scene.geometries.add(
                new Plane(new Point(0, -100, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(220, 220, 220)).setMaterial(reflectiveFloorMaterial),
                new Plane(new Point(0, 0, -200), new Vector(0, 0, 1))
                        .setEmission(new Color(80, 80, 90)).setMaterial(wallMaterial),
                new Plane(new Point(-150, 0, 0), new Vector(1, 0, 0))
                        .setEmission(new Color(70, 70, 80)).setMaterial(wallMaterial),
                new Plane(new Point(150, 0, 0), new Vector(-1, 0, 0))
                        .setEmission(new Color(70, 70, 80)).setMaterial(wallMaterial),
                new Plane(new Point(0, 100, 0), new Vector(0, -1, 0))
                        .setEmission(new Color(150, 150, 160)).setMaterial(wallMaterial)
        );

        // ==========================================
        // 2. WINDOWS & SUN
        // ==========================================
        scene.geometries.add(
                new Polygon(new Point(-149.9, 0, -50), new Point(-149.9, 60, -50),
                        new Point(-149.9, 60, 10), new Point(-149.9, 0, 10))
                        .setEmission(new Color(10, 0, 0)).setMaterial(windowGlassMaterial),
                new Polygon(new Point(-149.9, 0, -120), new Point(-149.9, 60, -120),
                        new Point(-149.9, 60, -60), new Point(-149.9, 0, -60))
                        .setEmission(new Color(10, 0, 0)).setMaterial(windowGlassMaterial),
                new Sphere(new Point(-800, 20, -85), 100)
                        .setEmission(new Color(255, 80, 20))
        );

        // ==========================================
        // 3. INFINITE MIRRORS & FRAMES
        // ==========================================
        double mirrorZ = -199.8;
        scene.geometries.add(
                new Polygon(new Point(-40, 0, mirrorZ), new Point(40, 0, mirrorZ),
                        new Point(40, 60, mirrorZ), new Point(-40, 60, mirrorZ))
                        .setEmission(new Color(10, 10, 10)).setMaterial(mirrorMaterial),
                new Polygon(new Point(-60, -20, 190), new Point(60, -20, 190),
                        new Point(60, 80, 190), new Point(-60, 80, 190))
                        .setEmission(new Color(10, 10, 10)).setMaterial(mirrorMaterial)
        );

        // Gold frames for the main mirror
        scene.geometries.add(
                new Polygon(new Point(-42, 60, mirrorZ), new Point(42, 60, mirrorZ),
                        new Point(42, 62, mirrorZ), new Point(-42, 62, mirrorZ))
                        .setEmission(new Color(200, 150, 0)).setMaterial(goldFrameMaterial),
                new Polygon(new Point(-42, -2, mirrorZ), new Point(42, -2, mirrorZ),
                        new Point(42, 0, mirrorZ), new Point(-42, 0, mirrorZ))
                        .setEmission(new Color(200, 150, 0)).setMaterial(goldFrameMaterial),
                new Polygon(new Point(-42, 0, mirrorZ), new Point(-40, 0, mirrorZ),
                        new Point(-40, 60, mirrorZ), new Point(-42, 60, mirrorZ))
                        .setEmission(new Color(200, 150, 0)).setMaterial(goldFrameMaterial),
                new Polygon(new Point(40, 0, mirrorZ), new Point(42, 0, mirrorZ),
                        new Point(42, 60, mirrorZ), new Point(40, 60, mirrorZ))
                        .setEmission(new Color(200, 150, 0)).setMaterial(goldFrameMaterial)
        );

        // ==========================================
        // 4. FURNITURE & DECORATIONS (Table, Chairs, Triangle Centrepiece)
        // ==========================================
        Point tableCenter = new Point(0, -100, -50);
        scene.geometries.add(
                new Cylinder(3, new Ray(tableCenter, new Vector(0, 1, 0)), 50)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(25, new Ray(tableCenter.add(new Vector(0, 50, 0)), new Vector(0, 1, 0)), 2)
                        .setEmission(new Color(90, 45, 20)).setMaterial(woodMaterial),
                new Triangle(new Point(-4, -48, -48), new Point(4, -48, -48), new Point(0, -35, -52))
                        .setEmission(new Color(0, 150, 50)).setMaterial(matteFurnitureMaterial)
        );

        // Chair 1 (Facing Left)
        Point chair1Start = new Point(35, -100, -50);
        scene.geometries.add(
                new Polygon(new Point(chair1Start.getX() - 10, chair1Start.getY() + 30, chair1Start.getZ() - 10),
                        new Point(chair1Start.getX() + 10, chair1Start.getY() + 30, chair1Start.getZ() - 10),
                        new Point(chair1Start.getX() + 10, chair1Start.getY() + 30, chair1Start.getZ() + 10),
                        new Point(chair1Start.getX() - 10, chair1Start.getY() + 30, chair1Start.getZ() + 10))
                        .setEmission(new Color(90, 45, 20)).setMaterial(woodMaterial),
                new Polygon(new Point(chair1Start.getX() + 10, chair1Start.getY() + 30, chair1Start.getZ() - 10),
                        new Point(chair1Start.getX() + 10, chair1Start.getY() + 70, chair1Start.getZ() - 10),
                        new Point(chair1Start.getX() + 10, chair1Start.getY() + 70, chair1Start.getZ() + 10),
                        new Point(chair1Start.getX() + 10, chair1Start.getY() + 30, chair1Start.getZ() + 10))
                        .setEmission(new Color(90, 45, 20)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair1Start.getX() - 8, chair1Start.getY(), chair1Start.getZ() - 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair1Start.getX() + 8, chair1Start.getY(), chair1Start.getZ() - 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair1Start.getX() + 8, chair1Start.getY(), chair1Start.getZ() + 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair1Start.getX() - 8, chair1Start.getY(), chair1Start.getZ() + 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial)
        );

        // Chair 2 (Facing Right)
        Point chair2Start = new Point(-35, -100, -50);
        scene.geometries.add(
                new Polygon(new Point(chair2Start.getX() - 10, chair2Start.getY() + 30, chair2Start.getZ() - 10),
                        new Point(chair2Start.getX() + 10, chair2Start.getY() + 30, chair2Start.getZ() - 10),
                        new Point(chair2Start.getX() + 10, chair2Start.getY() + 30, chair2Start.getZ() + 10),
                        new Point(chair2Start.getX() - 10, chair2Start.getY() + 30, chair2Start.getZ() + 10))
                        .setEmission(new Color(90, 45, 20)).setMaterial(woodMaterial),
                new Polygon(new Point(chair2Start.getX() - 10, chair2Start.getY() + 30, chair2Start.getZ() - 10),
                        new Point(chair2Start.getX() - 10, chair2Start.getY() + 70, chair2Start.getZ() - 10),
                        new Point(chair2Start.getX() - 10, chair2Start.getY() + 70, chair2Start.getZ() + 10),
                        new Point(chair2Start.getX() - 10, chair2Start.getY() + 30, chair2Start.getZ() + 10))
                        .setEmission(new Color(90, 45, 20)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair2Start.getX() - 8, chair2Start.getY(), chair2Start.getZ() - 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair2Start.getX() + 8, chair2Start.getY(), chair2Start.getZ() - 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair2Start.getX() + 8, chair2Start.getY(), chair2Start.getZ() + 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(1.5, new Ray(new Point(chair2Start.getX() - 8, chair2Start.getY(), chair2Start.getZ() + 8), new Vector(0, 1, 0)), 30)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial)
        );

        // ==========================================
        // 5. THE 3 REFLECTING SPHERES (Foreground)
        // ==========================================
        scene.geometries.add(
                new Sphere(new Point(0, -85, 20), 15)      // Red Ball (Centre)
                        .setEmission(new Color(180, 0, 0)).setMaterial(ballMaterial),
                new Sphere(new Point(40, -88, 10), 12)     // Green Ball (Right)
                        .setEmission(new Color(0, 180, 0)).setMaterial(ballMaterial),
                new Sphere(new Point(-40, -88, 10), 12)    // Blue Ball (Left)
                        .setEmission(new Color(0, 0, 180)).setMaterial(ballMaterial)
        );

        // ==========================================
        // 6. DECORATIVE LAMP & CEILING LIGHTS
        // ==========================================
        scene.geometries.add(
                new Tube(2, new Ray(new Point(0, 98, -150), new Vector(1, 0, 0)))
                        .setEmission(new Color(150, 150, 150)).setMaterial(new Material().setKT(0.5)),
                new Tube(2, new Ray(new Point(0, 98, -50), new Vector(1, 0, 0)))
                        .setEmission(new Color(150, 150, 150)).setMaterial(new Material().setKT(0.5))
        );

        Point lampLocation = new Point(100, -100, -100);
        scene.geometries.add(
                new Cylinder(15, new Ray(lampLocation, new Vector(0, 1, 0)), 5)
                        .setEmission(new Color(60, 30, 15)).setMaterial(woodMaterial),
                new Cylinder(2, new Ray(lampLocation.add(new Vector(0, 5, 0)), new Vector(0, 1, 0)), 65)
                        .setEmission(new Color(200, 150, 0)).setMaterial(goldFrameMaterial),
                new Cylinder(12, new Ray(lampLocation.add(new Vector(0, 60, 0)), new Vector(0, 1, 0)), 25)
                        .setEmission(new Color(200, 180, 120)).setMaterial(matteFurnitureMaterial),
                new Sphere(lampLocation.add(new Vector(0, 70, 0)), 5)
                        .setEmission(new Color(255, 200, 50))
        );

        // ==========================================
        // 7. LIGHTING
        // ==========================================
        scene.lights.add(new PointLight(new Color(150, 150, 180), new Point(0, 90, -150))
                .setKl(0.001).setKq(0.0001));
        scene.lights.add(new PointLight(new Color(150, 150, 180), new Point(0, 90, -50))
                .setKl(0.001).setKq(0.0001));
        scene.lights.add(new PointLight(new Color(255, 150, 50), lampLocation.add(new Vector(0, 70, 0)))
                .setKl(0.005).setKq(0.0005));
        scene.lights.add(new DirectionalLight(new Color(255, 100, 50), new Vector(1, -0.2, -0.5)));

        // ==========================================
        // 8. CAMERA SETUP & RENDER
        //    setMultithreading(-2) → auto raw threads (cores - 2)
        //    setDebugPrint(1)      → print progress every 1 %
        // ==========================================
        System.out.println("[Interior] Rendering...");
        long start = System.currentTimeMillis();

        Camera.getBuilder()
                .setLocation(new Point(0, -10, 150))
                .setDirection(new Point(0, -10, -50))
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-2)   // auto-detect: availableProcessors() - 2 threads
                .setDebugPrint(1)        // print progress every 1 %
                .build()
                .renderImage()
                .writeToImage("Showcase_CustomRoom_FinalVersion");

        System.out.printf("[Interior] Done in %.2f s%n",
                (System.currentTimeMillis() - start) / 1000.0);
    }

    
}