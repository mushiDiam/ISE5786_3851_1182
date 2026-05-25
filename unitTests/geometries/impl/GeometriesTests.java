package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link geometries.impl.Geometries} class.
 */
class GeometriesTests {

    /**
     * Default constructor for GeometriesTests.
     */
    GeometriesTests() {
    }

    /**
     * Test method for {@link geometries.impl.Geometries#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // Setup a collection of shapes along the Z-axis
        Plane plane = new Plane(new Point(0, 0, 1), new Vector(0, 0, 1));
        Sphere sphere = new Sphere(new Point(0, 0, 3), 1d); // intersects at z=2 and z=4
        Triangle triangle = new Triangle(new Point(2, -2, 5), new Point(-2, -2, 5), new Point(0, 2, 5));

        Geometries geometries = new Geometries(plane, sphere, triangle);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Some geometries are intersected (Sphere and Triangle, but not Plane)
        List<Point> resultEP = geometries.findIntersections(new Ray(new Point(0, 0, 1.5), new Vector(0, 0, 1)));
        assertNotNull(resultEP, "Expected intersections");
        assertEquals(3, resultEP.size(), "Wrong number of points: expected 3 (2 from sphere, 1 from triangle) ");

        // =============== Boundary Values Tests ==================
        // BV01: Empty geometries collection (Optional but good practice)
        Geometries emptyGeometries = new Geometries();
        assertNull(emptyGeometries.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 0, 0))),
                "Empty geometries should return null");

        // BV02: No geometry is intersected
        assertNull(geometries.findIntersections(new Ray(new Point(0, 0, 6), new Vector(0, 0, 1))),
                "No geometry intersected should return null (not empty list)");

        // BV03: Only one geometry is intersected (Triangle)
        List<Point> resultBV03 = geometries.findIntersections(new Ray(new Point(0, 0, 4.5), new Vector(0, 0, 1)));
        assertNotNull(resultBV03, "Expected intersections");
        assertEquals(1, resultBV03.size(), "Wrong number of points: expected 1 ");

        // BV04: All geometries are intersected
        List<Point> resultBV04 = geometries.findIntersections(new Ray(new Point(0, 0, -1), new Vector(0, 0, 1)));
        assertNotNull(resultBV04, "Expected intersections");
        // 1 (Plane) + 2 (Sphere) + 1 (Triangle) = 4 points
        assertEquals(4, resultBV04.size(), "Wrong number of points: expected 4 ");
    }

    /**
     * Test method for {@link geometries.api.Intersectable#calcIntersections(Ray, double)}.
     * Tests the Bonus functionality (Stage 8) ensuring intersections are properly filtered
     * by the maxDistance parameter across all geometries in the collection.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Plane plane = new Plane(new Point(0, 0, 1), new Vector(0, 0, 1));
        Sphere sphere = new Sphere(new Point(0, 0, 3), 1d);
        Triangle triangle = new Triangle(new Point(2, -2, 5), new Point(-2, -2, 5), new Point(0, 2, 5));
        Geometries geometries = new Geometries(plane, sphere, triangle);

        // Ray from (0, 0, -1) going along (0, 0, 1)
        // Expected intersections and distances from ray head:
        // Plane:    z=1 (distance 2)
        // Sphere:   z=2 (distance 3)
        // Sphere:   z=4 (distance 5)
        // Triangle: z=5 (distance 6)
        Ray ray = new Ray(new Point(0, 0, -1), new Vector(0, 0, 1));

        // TC01: maxDistance before any geometry
        assertNull(geometries.calcIntersections(ray, 1), "Should be null");

        // TC02: maxDistance reaches exactly the plane
        assertEquals(1, geometries.calcIntersections(ray, 2).size(), "Should only intersect Plane");

        // TC03: maxDistance reaches the front of the sphere
        assertEquals(2, geometries.calcIntersections(ray, 3).size(), "Should intersect Plane and front of Sphere");

        // TC04: maxDistance is inside the sphere
        assertEquals(2, geometries.calcIntersections(ray, 4).size(), "Should still be 2 since back of sphere is further");

        // TC05: maxDistance reaches the back of the sphere
        assertEquals(3, geometries.calcIntersections(ray, 5).size(), "Should intersect Plane, front, and back of Sphere");

        // TC06: maxDistance reaches the triangle
        assertEquals(4, geometries.calcIntersections(ray, 6).size(), "Should intersect all 4 boundaries");

        // TC07: maxDistance beyond all geometries
        assertEquals(4, geometries.calcIntersections(ray, 10).size(), "Should intersect all 4 boundaries");
    }
}