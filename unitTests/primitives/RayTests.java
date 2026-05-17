package primitives;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link primitives.Ray} class.
 * Covers:
 * <ul>
 *   <li>{@link Ray#Ray(Point, Vector)} – constructor normalization</li>
 *   <li>{@link Ray#getPoint(double)} – point on ray at parameter t</li>
 *   <li>{@link Ray#findClosestPoint(List)} – closest point from a list</li>
 * </ul>
 */
class RayTests {

    // ──────────────────────────── shared constants ────────────────────────────

    /**
     * Tolerance for floating-point comparisons
     */
    private static final double DELTA = 0.00001;

    /**
     * Origin point used in multiple tests
     */
    private static final Point ORIGIN = new Point(1, 2, 3);

    /**
     * A simple axis-aligned direction (will be normalized in constructor)
     */
    private static final Vector DIRECTION = new Vector(0, 3, 4);

    /**
     * A unit ray along the X-axis, origin at (1,2,3)
     */
    private static final Ray RAY = new Ray(ORIGIN, new Vector(1, 0, 0));

    // error messages
    /**
     * Error message for constructor normalization failure
     */
    private static final String ERROR_NORMALIZE = "ERROR: Ray constructor does not normalize the direction vector correctly";
    /**
     * Error message for getPoint failure
     */
    private static final String ERROR_GET_POINT = "ERROR: getPoint() returned wrong point";
    /**
     * Error message for findClosestPoint failure
     */
    private static final String ERROR_CLOSEST_POINT = "ERROR: findClosestPoint() returned wrong result";

    // ══════════════════════════════════════════════════════════════════════════
    //  Constructor tests
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     * Verifies that the direction vector is normalized on construction.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Constructor must normalize a non-unit direction vector.
        // Using the Pythagorean triple (0,3,4) whose length is 5.
        Ray ray = new Ray(new Point(0, 0, 0), DIRECTION);

        assertEquals(new Vector(0, 0.6, 0.8), ray.direction(), ERROR_NORMALIZE);
        assertEquals(1.0, ray.direction().length(), DELTA, ERROR_NORMALIZE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  getPoint tests  (Stage 3)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link Ray#getPoint(double)}.
     * RAY has origin (1,2,3) and normalized direction (1,0,0).
     */
    @Test
    void testGetPoint() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: t > 0 – point is ahead of the origin along the ray.
        assertEquals(new Point(4, 2, 3), RAY.getPoint(3), ERROR_GET_POINT);

        // EP02: t < 0 – point is behind the origin (opposite direction).
        assertEquals(new Point(-2, 2, 3), RAY.getPoint(-3), ERROR_GET_POINT);

        // =============== Boundary Values Tests ==================

        // BV01: t = 0 – the point must be exactly the origin.
        assertEquals(ORIGIN, RAY.getPoint(0), ERROR_GET_POINT);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  findClosestPoint tests  (Stage 5)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link Ray#findClosestPoint(List)}.
     * RAY has origin (1,2,3) and direction (1,0,0).
     * Points are chosen along the X-axis for easy distance reasoning.
     */
    @Test
    void testFindClosestPoint() {
        // Three points at distances 2, 5 and 9 from the origin along the ray.
        Point pClose = new Point(3, 2, 3);  // distance 2  – closest
        Point pMiddle = new Point(6, 2, 3);  // distance 5
        Point pFar = new Point(10, 2, 3); // distance 9  – farthest

        // ============ Equivalence Partitions Tests ==============

        // EP01: Closest point is in the middle of the list (not first or last).
        Point pFirst = new Point(8, 2, 3);  // distance 7
        Point pLast = new Point(9, 2, 3);  // distance 8
        assertEquals(pClose,
                RAY.findClosestPoint(List.of(pFirst, pClose, pLast)),
                ERROR_CLOSEST_POINT);

        // =============== Boundary Values Tests ==================

        // BV01: Null list – must return null (no intersections).
        assertNull(RAY.findClosestPoint(null), ERROR_CLOSEST_POINT);

        // BV02: Closest point is the first element of the list.
        assertEquals(pClose,
                RAY.findClosestPoint(List.of(pClose, pMiddle, pFar)),
                ERROR_CLOSEST_POINT);

        // BV03: Closest point is the last element of the list.
        assertEquals(pClose,
                RAY.findClosestPoint(List.of(pFar, pMiddle, pClose)),
                ERROR_CLOSEST_POINT);
    }
}