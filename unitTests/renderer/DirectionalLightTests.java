package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link lighting.DirectionalLight}.
 * <p>
 * Verifies:
 * <ul>
 *   <li>{@link lighting.DirectionalLight#getL(Point)}</li>
 *   <li>{@link lighting.DirectionalLight#getIntensity(Point)}</li>
 * </ul>
 */
class DirectionalLightTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    DirectionalLightTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Tolerance for floating-point comparisons.
     */
    private static final double DELTA = 1e-6;

    /**
     * A fixed light direction (will be normalized by the constructor).
     */
    private static final Vector DIRECTION = new Vector(1, 0, 0);

    /**
     * A fixed light intensity.
     */
    private static final Color INTENSITY = new Color(200, 100, 50);

    /**
     * The directional light under test.
     */
    private static final DirectionalLight LIGHT = new DirectionalLight(INTENSITY, DIRECTION);

    /**
     * An arbitrary point in the scene.
     */
    private static final Point P1 = new Point(5, 3, -2);

    /**
     * A second, far-away point.
     */
    private static final Point P2 = new Point(500, 300, -200);

    // ══════════════════════════════════════════════════════════════════════════
    //  getL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link lighting.DirectionalLight#getL(Point)}.
     * <p>
     * A directional light's direction is constant across the entire scene.
     */
    @Test
    void testGetL() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getL at any point equals the fixed normalized direction.
        assertEquals(DIRECTION.normalize(), LIGHT.getL(P1),
                "ERROR: DirectionalLight.getL() returned wrong direction");

        // EP02: getL is the same for a completely different point (direction is global).
        assertEquals(LIGHT.getL(P1), LIGHT.getL(P2),
                "ERROR: DirectionalLight.getL() must be constant regardless of the point");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  getIntensity
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link lighting.DirectionalLight#getIntensity(Point)}.
     * <p>
     * A directional light has no distance attenuation: intensity is constant.
     */
    @Test
    void testGetIntensity() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Intensity at a nearby point equals the original intensity.
        assertEquals(INTENSITY, LIGHT.getIntensity(P1),
                "ERROR: DirectionalLight.getIntensity() returned wrong color at close point");

        // EP02: Intensity at a far-away point is identical (no attenuation).
        assertEquals(LIGHT.getIntensity(P1), LIGHT.getIntensity(P2),
                "ERROR: DirectionalLight.getIntensity() must be constant regardless of the point");
    }
}