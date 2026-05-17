package renderer;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for {@link Camera} ray construction and {@link Intersectable} geometries.
 * <p>
 * These tests verify that rays generated through a pixel grid correctly intersect
 * with various shapes (Sphere, Plane, Triangle) and yield the expected total count.
 * </p>
 * * @author Dhéliat
 */
class CameraIntersectionIntegrationTests {

    /**
     * Helper method to calculate the total number of intersections between a camera's
     * view plane and a given geometry.
     *
     * @param testName      Description of the test case.
     * @param camera        The camera configured with resolution and view plane.
     * @param geometry      The geometric body to test for intersections.
     * @param expectedCount The total number of expected intersection points.
     */
    private void assertIntersectionsCount(String testName, Camera camera, Intersectable geometry, int expectedCount) {
        int count = 0;
        int nX = camera.getNx();
        int nY = camera.getNy();

        // Iterate over all pixels in the resolution grid
        for (int i = 0; i < nY; ++i) {
            for (int j = 0; j < nX; ++j) {
                // Use the updated 2-parameter signature: constructRay(xIndex, yIndex)
                Ray ray = camera.constructRay(j, i);
                List<Point> intersections = geometry.findIntersections(ray);
                if (intersections != null) {
                    count += intersections.size();
                }
            }
        }

        assertEquals(expectedCount, count, testName + ": Wrong number of intersections.");
    }

    /**
     * Integration tests for Sphere and Camera.
     */
    @Test
    void testCameraRaySphereIntegration() {
        Camera camera1 = Camera.getBuilder()
                .setLocation(new Point(0, 0, 0))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(3, 3)
                .setVpDistance(1)
                .setResolution(3, 3)
                .build();

        // TC01: Small sphere in front of the camera (2 intersections)
        assertIntersectionsCount("TC01: Small sphere", camera1,
                new Sphere(new Point(0, 0, -3), 1), 2);

        // TC02: Large sphere, all rays intersect (18 intersections)
        Camera camera2 = Camera.getBuilder()
                .setLocation(new Point(0, 0, 0.5))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(3, 3)
                .setVpDistance(1)
                .setResolution(3, 3)
                .build();
        assertIntersectionsCount("TC02: Large sphere", camera2,
                new Sphere(new Point(0, 0, -2.5), 2.5), 18);

        // TC03: Medium sphere (10 intersections)
        assertIntersectionsCount("TC03: Medium sphere", camera2,
                new Sphere(new Point(0, 0, -2), 2), 10);

        // TC04: Sphere contains the camera (9 intersections)
        assertIntersectionsCount("TC04: Camera inside sphere", camera2,
                new Sphere(new Point(0, 0, -1), 4), 9);

        // TC05: Sphere behind the camera (0 intersections)
        assertIntersectionsCount("TC05: Sphere behind camera", camera1,
                new Sphere(new Point(0, 0, 1), 0.5), 0);
    }

    /**
     * Integration tests for Plane and Camera.
     */
    @Test
    void testCameraRayPlaneIntegration() {
        Camera camera = Camera.getBuilder()
                .setLocation(new Point(0, 0, 0))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(3, 3)
                .setVpDistance(1)
                .setResolution(3, 3)
                .build();

        // TC01: Plane orthogonal to the axis (9 intersections)
        assertIntersectionsCount("TC01: Orthogonal plane", camera,
                new Plane(new Point(0, 0, -2), new Vector(0, 0, 1)), 9);

        // TC02: Tilted plane (9 intersections)
        assertIntersectionsCount("TC02: Tilted plane", camera,
                new Plane(new Point(0, 0, -2), new Vector(0, -1, 5)), 9);

        // TC03: Plane parallel to the upper rays (6 intersections)
        assertIntersectionsCount("TC03: Tilted plane parallel to rays", camera,
                new Plane(new Point(0, 0, -5), new Vector(0, -1, 1)), 6);
    }

    /**
     * Integration tests for Triangle and Camera.
     */
    @Test
    void testCameraRayTriangleIntegration() {
        Camera camera = Camera.getBuilder()
                .setLocation(new Point(0, 0, 0))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(3, 3)
                .setVpDistance(1)
                .setResolution(3, 3)
                .build();

        // TC01: Small triangle (1 intersection)
        assertIntersectionsCount("TC01: Small triangle", camera,
                new Triangle(new Point(0, 1, -2), new Point(1, -1, -2), new Point(-1, -1, -2)), 1);

        // TC02: Large triangle (2 intersections)
        assertIntersectionsCount("TC02: Large triangle", camera,
                new Triangle(new Point(0, 20, -2), new Point(1, -1, -2), new Point(-1, -1, -2)), 2);
    }

    static class DirectionalLightTests {

    }
}