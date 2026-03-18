package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link primitives.Ray} class.
 */
class RayTests {

    /**
     * Error message for normalization failure
     */
    private static final String ERROR_NORMALIZE = "ERROR: Ray constructor does not normalize the direction vector correctly";

    /**
     * Tolerance for floating-point comparisons
     */
    private static final double DELTA = 0.00001;

    /**
     * Test method for {@link primitives.Ray#Ray(primitives.Point, primitives.Vector)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Test that the constructor normalizes the direction vector
        // Using a Pythagorean triple (0, 3, 4) for the vector, which has a length of 5.
        Vector direction = new Vector(0, 3, 4);
        Ray ray = new Ray(new Point(0, 0, 0), direction);

        Vector expectedDirection = new Vector(0, 0.6, 0.8);

        // Verify the direction vector is exactly the normalized version of the input
        assertEquals(expectedDirection, ray.direction(), ERROR_NORMALIZE);

        // Verify the length of the direction vector is exactly 1
        assertEquals(1.0, ray.direction().length(), DELTA, ERROR_NORMALIZE);
    }
}