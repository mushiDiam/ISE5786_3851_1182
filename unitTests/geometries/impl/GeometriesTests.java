package geometries.tests;

import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link geometries.impl.Geometries} class.
 */
class GeometriesTests {

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
        // EP01: Some geometries are intersected (Sphere and Triangle, but not Plane) [cite: 73]
        List<Point> resultEP = geometries.findIntersections(new Ray(new Point(0, 0, 1.5), new Vector(0, 0, 1)));
        assertNotNull(resultEP, "Expected intersections");
        assertEquals(3, resultEP.size(), "Wrong number of points: expected 3 (2 from sphere, 1 from triangle) ");

        // =============== Boundary Values Tests ==================
        // BV01: Empty geometries collection (Optional but good practice)
        Geometries emptyGeometries = new Geometries();
        assertNull(emptyGeometries.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 0, 0))),
                "Empty geometries should return null");

        // BV02: No geometry is intersected [cite: 74]
        assertNull(geometries.findIntersections(new Ray(new Point(0, 0, 6), new Vector(0, 0, 1))),
                "No geometry intersected should return null (not empty list)");

        // BV03: Only one geometry is intersected (Triangle) [cite: 74]
        List<Point> resultBV03 = geometries.findIntersections(new Ray(new Point(0, 0, 4.5), new Vector(0, 0, 1)));
        assertNotNull(resultBV03, "Expected intersections");
        assertEquals(1, resultBV03.size(), "Wrong number of points: expected 1 ");

        // BV04: All geometries are intersected [cite: 74]
        List<Point> resultBV04 = geometries.findIntersections(new Ray(new Point(0, 0, -1), new Vector(0, 0, 1)));
        assertNotNull(resultBV04, "Expected intersections");
        // 1 (Plane) + 2 (Sphere) + 1 (Triangle) = 4 points
        assertEquals(4, resultBV04.size(), "Wrong number of points: expected 4 ");
    }
}