package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link primitives.Vector} class.
 */
class VectorTests {

    /**
     * Default constructor for VectorTests.
     */
    VectorTests() {
    }

    /**
     * Tolerance for floating-point comparisons
     */
    private static final double DELTA = 0.00001;

    /**
     * Error message for math operations
     */
    private static final String ERROR_MATH = "ERROR: Math operation result is incorrect";

    /**
     * Error message for missing exceptions
     */
    private static final String ERROR_MISSING_EXCEPTION = "ERROR: Expected exception was not thrown";

    /**
     * Test method for {@link primitives.Vector#Vector(double, double, double)}
     * and {@link primitives.Vector#Vector(primitives.Double3)}.
     */
    @Test
    void testConstructors() {
        // =============== Boundary Values Tests ==================
        // BV01: Attempt to create a zero vector with 3 doubles
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                ERROR_MISSING_EXCEPTION);

        // BV02: Attempt to create a zero vector with Double3 object
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                ERROR_MISSING_EXCEPTION);
    }

    /**
     * Test method for {@link primitives.Vector#add(primitives.Vector)}.
     */
    @Test
    void testAdd() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(-2, -4, -6);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Simple addition of two vectors
        assertEquals(new Vector(-1, -2, -3), v1.add(v2), ERROR_MATH);

        // =============== Boundary Values Tests ==================
        // BV01: Addition of a vector with its opposite (results in zero vector)
        assertThrows(IllegalArgumentException.class, () -> v1.add(new Vector(-1, -2, -3)),
                ERROR_MISSING_EXCEPTION);
    }

    /**
     * Test method for {@link primitives.Vector#subtract(primitives.Point)}.
     */
    @Test
    void testSubtract() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(-2, -4, -6);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Simple subtraction of two vectors
        assertEquals(new Vector(3, 6, 9), v1.subtract(v2), ERROR_MATH);

        // =============== Boundary Values Tests ==================
        // BV01: Subtraction of a vector from itself (results in zero vector)
        assertThrows(IllegalArgumentException.class, () -> v1.subtract(v1),
                ERROR_MISSING_EXCEPTION);
    }

    /**
     * Test method for {@link primitives.Vector#scale(double)}.
     */
    @Test
    void testScale() {
        Vector v1 = new Vector(1, 2, 3);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Multiplication by a positive scalar
        assertEquals(new Vector(2, 4, 6), v1.scale(2), ERROR_MATH);

        // EP02: Multiplication by a negative scalar
        assertEquals(new Vector(-1, -2, -3), v1.scale(-1), ERROR_MATH);

        // =============== Boundary Values Tests ==================
        // BV01: Multiplication by 0 (results in zero vector)
        assertThrows(IllegalArgumentException.class, () -> v1.scale(0),
                ERROR_MISSING_EXCEPTION);
    }

    /**
     * Test method for {@link primitives.Vector#dotProduct(primitives.Vector)}.
     */
    @Test
    void testDotProduct() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(-2, -4, -6);
        Vector v3 = new Vector(0, 3, -2);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Dot product of two arbitrary vectors
        assertEquals(-28, v1.dotProduct(v2), DELTA, ERROR_MATH);

        // =============== Boundary Values Tests ==================
        // BV01: Dot product of orthogonal vectors (should be 0)
        assertEquals(0, v1.dotProduct(v3), DELTA, ERROR_MATH);
    }

    /**
     * Test method for {@link primitives.Vector#crossProduct(primitives.Vector)}.
     */
    @Test
    void testCrossProduct() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(0, 3, -2);
        Vector v3 = new Vector(-2, -4, -6);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard cross product
        Vector vr = v1.crossProduct(v2);
        assertEquals(v1.length() * v2.length(), vr.length(), DELTA, ERROR_MATH);
        assertEquals(0, vr.dotProduct(v1), DELTA, ERROR_MATH);
        assertEquals(0, vr.dotProduct(v2), DELTA, ERROR_MATH);

        // =============== Boundary Values Tests ==================
        // BV01: Cross product of parallel vectors
        assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(v3),
                ERROR_MISSING_EXCEPTION);

        // BV02: Cross product of a vector with itself
        assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(v1),
                ERROR_MISSING_EXCEPTION);
    }

    /**
     * Test method for {@link primitives.Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Calculation with positive values
        Vector v1 = new Vector(1, 2, 3);
        assertEquals(14, v1.lengthSquared(), DELTA, ERROR_MATH);

        // EP02: Calculation with negative values
        Vector v2 = new Vector(-1, -2, -3);
        assertEquals(14, v2.lengthSquared(), DELTA, ERROR_MATH);
    }

    /**
     * Test method for {@link primitives.Vector#length()}.
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Calculation with positive values
        Vector v1 = new Vector(0, 3, 4);
        assertEquals(5, v1.length(), DELTA, ERROR_MATH);

        // EP02: Calculation with negative values
        Vector v2 = new Vector(0, -3, -4);
        assertEquals(5, v2.length(), DELTA, ERROR_MATH);
    }

    /**
     * Test method for {@link primitives.Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        Vector v = new Vector(1, 2, 3);
        Vector n = v.normalize();

        // ============ Equivalence Partitions Tests ==============
        // EP01: The normalized vector must have a length of 1
        assertEquals(1, n.length(), DELTA, ERROR_MATH);

        // EP02: Check collinearity (cross product must throw exception)
        assertThrows(IllegalArgumentException.class, () -> v.crossProduct(n),
                ERROR_MISSING_EXCEPTION);

        // EP03: Check direction
        assertTrue(v.dotProduct(n) > 0, "ERROR: Normalized vector points in the opposite direction");
    }
}