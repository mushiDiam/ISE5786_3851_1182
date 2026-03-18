package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link primitives.Point} class.
 */
class PointTests {

    /**
     * Error message for wrong distance calculation
     */
    private static final String ERROR_DISTANCE = "ERROR: Distance calculation is incorrect";

    /**
     * Error message for exceptions that shouldn't be thrown
     */
    private static final String ERROR_EXCEPTION = "ERROR: Exception was thrown unexpectedly";

    /**
     * Error message for missing exceptions
     */
    private static final String ERROR_MISSING_EXCEPTION = "ERROR: Expected exception was not thrown";

    /**
     * Test method for {@link primitives.Point#subtract(primitives.Point)}.
     */
    @Test
    void testSubtract() {
        Point p1 = new Point(1, 2, 3);
        Point p2 = new Point(-1, 2, 4);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Simple subtraction between two different points
        assertEquals(new Vector(-2, 0, 1), p2.subtract(p1),
                "ERROR: Point subtraction does not work correctly");

        // =============== Boundary Values Tests ==================
        // BV01: Subtracting a point from itself (should throw exception to avoid zero vector)
        assertThrows(IllegalArgumentException.class, () -> p1.subtract(p1),
                ERROR_MISSING_EXCEPTION);
    }

    /**
     * Test method for {@link primitives.Point#add(primitives.Vector)}.
     */
    @Test
    void testAdd() {
        Point p = new Point(1, 2, 3);
        Vector v = new Vector(-1, -2, -3);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Adding a vector to a point
        assertEquals(new Point(0, 0, 0), p.add(v),
                "ERROR: Adding vector to point does not work correctly");
    }

    /**
     * Test method for {@link primitives.Point#distanceSquared(primitives.Point)}.
     */
    @Test
    void testDistanceSquared() {
        Point p1 = new Point(1, 1, 0);
        Point p2 = new Point(0, 1, 1);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Squared distance between two different points
        assertEquals(2, p1.distanceSquared(p2), ERROR_DISTANCE);

        // =============== Boundary Values Tests ==================
        // BV01: Squared distance from a point to itself
        assertEquals(0, p1.distanceSquared(p1), ERROR_DISTANCE);
    }

    /**
     * Test method for {@link primitives.Point#distance(primitives.Point)}.
     */
    @Test
    void testDistance() {
        Point p1 = new Point(1, 1, 0);
        Point p2 = new Point(0, 1, 1);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Distance between two different points
        assertEquals(Math.sqrt(2), p1.distance(p2), ERROR_DISTANCE);

        // =============== Boundary Values Tests ==================
        // BV01: Distance from a point to itself
        assertEquals(0, p1.distance(p1), ERROR_DISTANCE);
    }
}