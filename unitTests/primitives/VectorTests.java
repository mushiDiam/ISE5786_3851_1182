package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorTests {

    // Tolerance for floating-point comparisons
    private static final double DELTA = 0.00001;

    @Test
    void testConstructors() {
        // ============ Boundary Values Tests ============
        // TC01: Attempt to create a null vector with 3 doubles
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                "Constructor must throw an exception for the null vector (0,0,0)");

        // TC02: Attempt to create a null vector with Double3 object
        // (Assuming Double3.ZERO is accessible in your project)
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                "Constructor must throw an exception for the null vector using Double3.ZERO");
    }

    @Test
    void testEquals() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(1, 2, 3);
        Vector v3 = new Vector(1, 2, 4);
        Point p1 = new Point(1, 2, 3);

        // ============ Equivalence Partitions Tests ============
        // TC01: Symmetrical equality
        assertEquals(v1, v2, "Vectors with identical coordinates should be equal");

        // TC02: Inequality with different coordinates
        assertNotEquals(v1, v3, "Vectors with different coordinates should not be equal");

        // ============ Boundary Values Tests ============
        // TC03: Comparison to self
        assertEquals(v1, v1, "A vector must be equal to itself");

        // TC04: Comparison to null
        assertNotEquals(v1, null, "A vector cannot be equal to null");

        // TC05: Comparison to a Point (Strict OOP check to ensure Vector != Point)
        assertNotEquals(v1, p1, "A Vector should not be equal to a Point, even with the same coordinates");
    }

    @Test
    void testAdd() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(-2, -4, -6);

        // ============ Equivalence Partitions Tests ============
        // TC01: Simple addition of two vectors
        assertEquals(new Vector(-1, -2, -3), v1.add(v2), "Vector addition failed");

        // ============ Boundary Values Tests ============
        // TC02: Addition of a vector with its opposite (results in null vector)
        assertThrows(IllegalArgumentException.class, () -> v1.add(new Vector(-1, -2, -3)),
                "Addition resulting in a null vector must throw an exception");
    }

    @Test
    void testScale() {
        Vector v1 = new Vector(1, 2, 3);

        // ============ Equivalence Partitions Tests ============
        // TC01: Multiplication by a positive scalar
        assertEquals(new Vector(2, 4, 6), v1.scale(2), "Multiplication by a positive scalar failed");

        // TC02: Multiplication by a negative scalar
        assertEquals(new Vector(-1, -2, -3), v1.scale(-1), "Multiplication by a negative scalar failed");

        // ============ Boundary Values Tests ============
        // TC03: Multiplication by 0 (results in null vector)
        assertThrows(IllegalArgumentException.class, () -> v1.scale(0),
                "Multiplication by 0 resulting in a null vector must throw an exception");
    }

    @Test
    void testDotProduct() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(-2, -4, -6);
        Vector v3 = new Vector(0, 3, -2);

        // ============ Equivalence Partitions Tests ============
        // TC01: Dot product of two arbitrary vectors
        assertEquals(-28, v1.dotProduct(v2), DELTA, "Dot product is incorrect");

        // ============ Boundary Values Tests ============
        // TC02: Dot product of orthogonal vectors (should be 0)
        assertEquals(0, v1.dotProduct(v3), DELTA, "Dot product of orthogonal vectors must be 0");
    }

    @Test
    void testCrossProduct() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(0, 3, -2);
        Vector v3 = new Vector(-2, -4, -6);

        // ============ Equivalence Partitions Tests ============
        // TC01: Standard cross product
        Vector vr = v1.crossProduct(v2);

        // Check 1: Does the resulting vector have the correct length?
        assertEquals(v1.length() * v2.length(), vr.length(), DELTA, "Cross product length is incorrect");

        // Check 2: Is the resulting vector orthogonal to both original vectors?
        assertEquals(0, vr.dotProduct(v1), DELTA, "Cross product result is not orthogonal to its first operand");
        assertEquals(0, vr.dotProduct(v2), DELTA, "Cross product result is not orthogonal to its second operand");

        // ============ Boundary Values Tests ============
        // TC02: Cross product of collinear (parallel) vectors
        assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(v3),
                "Cross product of two parallel vectors must throw an exception (null vector)");

        // TC03: Cross product of a vector with itself
        assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(v1),
                "Cross product of a vector with itself must throw an exception (null vector)");
    }

    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ============
        // TC01: Calculation with positive values (1^2 + 2^2 + 3^2 = 14)
        Vector v1 = new Vector(1, 2, 3);
        assertEquals(14, v1.lengthSquared(), DELTA, "Squared length is incorrect for positive coordinates");

        // TC02: Calculation with negative values ((-1)^2 + (-2)^2 + (-3)^2 = 14)
        Vector v2 = new Vector(-1, -2, -3);
        assertEquals(14, v2.lengthSquared(), DELTA, "Squared length is incorrect for negative coordinates");
    }

    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ============
        // TC01: Calculation with positive values
        Vector v1 = new Vector(0, 3, 4); // Classic Pythagorean quadruple (3, 4, 5)
        assertEquals(5, v1.length(), DELTA, "Length is incorrect for positive coordinates");

        // TC02: Calculation with negative values
        Vector v2 = new Vector(0, -3, -4);
        assertEquals(5, v2.length(), DELTA, "Length is incorrect for negative coordinates");
    }

    @Test
    void testNormalize() {
        Vector v = new Vector(1, 2, 3);
        Vector n = v.normalize();

        // ============ Equivalence Partitions Tests ============
        // TC01: The normalized vector must have a length of 1
        assertEquals(1, n.length(), DELTA, "Normalized vector does not have a length of 1");

        // TC02: Check collinearity (cross product must throw exception)
        assertThrows(IllegalArgumentException.class, () -> v.crossProduct(n),
                "Normalized vector is not collinear with the original vector");

        // TC03: Check direction (dot product must be positive, confirming they point the same way)
        assertTrue(v.dotProduct(n) > 0, "Normalized vector points in the opposite direction");

        // TC04: Explicit coordinate check for a known vector
        Vector v2 = new Vector(0, 3, 4);
        Vector expectedNormal = new Vector(0, 0.6, 0.8); // 3/5 and 4/5
        assertEquals(expectedNormal, v2.normalize(), "Normalize math is explicitly incorrect");
    }
}