package renderer;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import static java.awt.Color.BLUE;

/**
 * Integration tests with multiple simultaneous light sources.
 */
class MultiLightsTests {

    /**
     * Default constructor for MultiLightsTests.
     */
    MultiLightsTests() {
    }

    /**
     * The first scene, used for testing a sphere with multiple lights.
     */
    private final Scene scene1 = new Scene("Test multi-lights sphere")
            .setAmbientLight(new AmbientLight(new Color(java.awt.Color.WHITE).scale(0.15)));

    /**
     * The second scene, used for testing triangles with multiple lights.
     */
    private final Scene scene2 = new Scene("Test multi-lights triangles")
            .setAmbientLight(new AmbientLight(new Color(java.awt.Color.WHITE).scale(0.15)));

    /**
     * The camera configuration for the first scene.
     */
    private final Camera.Builder camera1 = Camera.getBuilder()
            .setRayTracer(scene1, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000))
            .setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(150, 150).setVpDistance(1000);

    /**
     * The camera configuration for the second scene.
     */
    private final Camera.Builder camera2 = Camera.getBuilder()
            .setRayTracer(scene2, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000))
            .setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(200, 200).setVpDistance(1000);

    /**
     * Rendering of a sphere with Directional Light, Point Light and Spot Light
     */
    @Test
    void testSphereMultiLight() {
        scene1.geometries.add(
                new Sphere(new Point(0, 0, -50), 50d)
                        .setEmission(new Color(BLUE).reduce(2))
                        .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(300))
        );

        // Directional lighting (Yellow ambient light coming from above)
        scene1.lights.add(new DirectionalLight(new Color(150, 150, 0), new Vector(1, -1, -0.5)));

        // Spotlight (Red side lighting)
        scene1.lights.add(new PointLight(new Color(500, 0, 0), new Point(50, 50, 50))
                .setKl(0.0001).setKq(0.000005));

        // Spotlight (Concentrated green beam)
        scene1.lights.add(new SpotLight(new Color(0, 400, 0), new Point(-50, -50, 50), new Vector(1, 1, -2))
                .setKl(0.00001).setKq(0.000005));

        camera1.setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("lightSphereMulti");
    }

    /**
     * Rendering of two triangles with DirectionalLight, PointLight and SpotLight
     */
    @Test
    void testTrianglesMultiLight() {
        Material material = new Material().setKD(new Double3(0.5)).setKS(new Double3(0.5)).setShininess(300);

        Point[] vertices = {
                new Point(-150, -150, -150),
                new Point(150, 150, -150),
                new Point(75, -150, -150),
                new Point(-75, 150, -150)
        };

        scene2.geometries.add(
                new Triangle(vertices[0], vertices[1], vertices[2]).setMaterial(material),
                new Triangle(vertices[0], vertices[1], vertices[3]).setMaterial(material)
        );

        // Directional light (Blue base light)
        scene2.lights.add(new DirectionalLight(new Color(0, 0, 150), new Vector(0, 0, -1)));

        // Spot light (Red, at the top center)
        scene2.lights.add(new PointLight(new Color(500, 0, 0), new Point(0, 50, 50))
                .setKl(0.0001).setKq(0.00005));

        // Spotlight (angled green beam)
        scene2.lights.add(new SpotLight(new Color(0, 500, 0), new Point(-50, -50, 100), new Vector(1, 1, -2))
                .setKl(0.0001).setKq(0.00005));

        camera2.setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("lightTrianglesMulti");
    }
}