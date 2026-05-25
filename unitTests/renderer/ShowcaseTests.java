package renderer;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
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
 */
public class ShowcaseTests {

    /**
     * Default constructor required by the Javadoc generator.
     */
    public ShowcaseTests() {
    }

    /**
     * Builds and renders the bonus showcase scenes.
     */
    @Test
    public void testBonus1And2Showcase() {
        Scene scene = new Scene("Showcase Scene");
        scene.setAmbientLight(new AmbientLight(new Color(15, 15, 15)));

        // Define materials
        Material glassMaterial = new Material().setKD(0.1).setKS(0.3).setShininess(20).setKT(0.7);
        Material mirrorMaterial = new Material().setKD(0.1).setKS(0.8).setShininess(60).setKR(0.85);
        Material matteMaterial = new Material().setKD(0.5).setKS(0.5).setShininess(30);
        Material floorMaterial = new Material().setKD(0.4).setKS(0.6).setShininess(50).setKR(0.2); // Slightly reflective floor
        // Removed KS to eliminate the bright specular highlight on the wall
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

                // 4. CYLINDER (The Arrow Stem): Starts at the floor (-50) and goes up 70 units to reach Y=20.
                new Cylinder(8, new Ray(new Point(-40, -50, -100), new Vector(0, 1, 0)), 70)
                        .setEmission(new Color(20, 60, 20))
                        .setMaterial(matteMaterial),

                // 5. TRIANGLE (The Arrow Head): Base sits exactly at Y=20 on top of the cylinder, tip at Y=40.
                new Triangle(new Point(-40, 40, -100), new Point(-55, 20, -100), new Point(-25, 20, -100))
                        .setEmission(new Color(100, 80, 20))
                        .setMaterial(matteMaterial),

                // 6. TUBE: An infinite horizontal pipeline resting on the floor against the back wall
                new Tube(6, new Ray(new Point(-100, -44, -130), new Vector(1, 0, 0)))
                        .setEmission(new Color(60, 20, 20))
                        .setMaterial(matteMaterial),

                // 7. SPHERE: Mirror ball resting in the other corner of the room
                new Sphere(new Point(60, -10, -110), 30) // Moved down and right slightly
                        .setEmission(new Color(10, 10, 10))
                        .setMaterial(mirrorMaterial),

                // 8. SPHERE: Glass ball floating in the center
                new Sphere(new Point(0, 10, -60), 25)
                        .setEmission(new Color(0, 0, 0))
                        .setMaterial(glassMaterial),

                // 9. POLYGON: Diamond shape hovering in the background against the new back wall
                new Polygon(new Point(0, 60, -145), new Point(30, 30, -145), new Point(0, 0, -145), new Point(-30, 30, -145))
                        .setEmission(new Color(20, 20, 80))
                        .setMaterial(matteMaterial),

                // 10, 11, 12. SPHERES: Scattered colorful balls on the floor
                new Sphere(new Point(-20, -40, -30), 10) // Red
                        .setEmission(new Color(150, 0, 0))
                        .setMaterial(matteMaterial),
                new Sphere(new Point(20, -40, -40), 10)  // Green
                        .setEmission(new Color(0, 150, 0))
                        .setMaterial(matteMaterial),
                new Sphere(new Point(0, -45, -15), 5)    // Small Blue
                        .setEmission(new Color(0, 0, 150))
                        .setMaterial(matteMaterial)
        );

        // ==========================================
        // LIGHTS
        // ==========================================
        scene.lights.add(new SpotLight(new Color(250, 150, 150), new Point(40, 100, 50), new Vector(-1, -1, -1))
                .setKl(1E-5).setKq(1.5E-7));
        scene.lights.add(new PointLight(new Color(250, 250, 250), new Point(-50, 50, 50))
                .setKl(0.0005).setKq(0.00005));

        // ==========================================
        // CAMERA SETUP
        // ==========================================
        Camera.Builder cameraBuilder = Camera.getBuilder()
                .setLocation(new Point(0, 30, 150))
                .setDirection(new Point(0, 0, -50))
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE);

        // ==========================================
        // BONUS 1: RENDER THE MAIN EPIC SCENE
        // ==========================================
        cameraBuilder.build()
                .renderImage()
                .writeToImage("Bonus1_EpicScene_MainAngle");

        // ==========================================
        // BONUS 2: ROTATE AND MOVE THE CAMERA
        // ==========================================
        // The instructions say to demonstrate the artistic effect of rotating the camera
        cameraBuilder.setRotation(15) // Tilt the camera 15 degrees clockwise
                .setLocation(new Point(40, 40, 120)) // Move it slightly to the right and up
                .setDirection(new Point(0, 0, -50))  // Keep looking at the center
                .build()
                .renderImage()
                .writeToImage("Bonus2_EpicScene_TiltedAngle");
    }
}