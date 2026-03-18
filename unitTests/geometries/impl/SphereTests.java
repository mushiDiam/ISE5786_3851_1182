package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link geometries.impl.Sphere} class.
 */
class SphereTests {

    /**
     * Test method for {@link geometries.impl.Sphere#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        // Sphere with radius 5, centered at the origin (0, 0, 0)
        Sphere sphere = new Sphere(new Point(0, 0, 0), 5.0);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the surface of the sphere
        // Using a Pythagorean triple (0, 3, 4) for the point on the sphere surface.
        // The normal vector should be the normalized vector from the center to the point.
        Vector expectedNormal = new Vector(0, 3.0 / 5.0, 4.0 / 5.0);

        assertEquals(expectedNormal, sphere.getNormal(new Point(0, 3, 4)),
                "ERROR: getNormal() wrong result for a point on the sphere");
    }
}