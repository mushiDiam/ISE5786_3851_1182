package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link lighting.SpotLight}.
 * <p>
 * Verifies:
 * <ul>
 *   <li>{@link lighting.SpotLight#getL(Point)}</li>
 *   <li>{@link lighting.SpotLight#getIntensity(Point)}</li>
 * </ul>
 */
class SpotLightTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    SpotLightTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Tolerance for floating-point component comparisons.
     */
    private static final double DELTA = 1e-6;

    /**
     * Position of the spotlight.
     */
    private static final Point POSITION = new Point(0, 0, 0);

    /**
     * Beam direction of the spotlight (pointing along +X).
     */
    private static final Vector DIRECTION = new Vector(1, 0, 0);

    /**
     * Original intensity of the spotlight.
     */
    private static final Color INTENSITY = new Color(400, 200, 100);

    // ══════════════════════════════════════════════════════════════════════════
    //  getL  – inherited from PointLight, but verify it still works correctly
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link lighting.SpotLight#getL(Point)}.
     * <p>
     * SpotLight inherits getL from PointLight: normalized vector from position to point.
     */
    @Test
    void testGetL() {
        SpotLight light = new SpotLight(INTENSITY, POSITION, DIRECTION);

        // ============ Equivalence Partitions Tests ==============

        // EP01: Point directly in the beam direction (along +X).
        assertEquals(new Vector(1, 0, 0), light.getL(new Point(5, 0, 0)),
                "ERROR: SpotLight.getL() wrong direction for point in beam");

        // EP02: Point at an angle – result must be normalized.
        Point p = new Point(3, 4, 0);   // distance 5, direction (0.6, 0.8, 0)
        assertEquals(new Vector(3, 4, 0).normalize(), light.getL(p),
                "ERROR: SpotLight.getL() wrong normalized direction for off-axis point");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  getIntensity
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link lighting.SpotLight#getIntensity(Point)}.
     * <p>
     * Intensity = PointLight-intensity × max(0, direction · l).
     * When the point is exactly in the beam, direction · l = 1, so the
     * spotlight factor is 1 and intensity equals the point-light intensity.
     */
    @Test
    void testGetIntensity() {
        SpotLight light = new SpotLight(INTENSITY, POSITION, DIRECTION);

        // ============ Equivalence Partitions Tests ==============

        // EP01: Point directly in the beam (along +X) – beam factor = direction · l = 1.
        //   No distance attenuation (kC=1 by default), so result = INTENSITY × 1 = INTENSITY.
        assertEquals(INTENSITY, light.getIntensity(new Point(5, 0, 0)),
                "ERROR: SpotLight.getIntensity() wrong for point exactly in beam");

        // EP02: Point at 60° from beam direction.
        //   direction=(1,0,0), l toward (1,√3,0) normalized → l·direction = 0.5.
        //   No distance attenuation → result = INTENSITY × 0.5 = (200, 100, 50).
        Point p60 = new Point(1, Math.sqrt(3), 0);
        Color expected60 = new Color(200, 100, 50);
        assertEquals(expected60, light.getIntensity(p60),
                "ERROR: SpotLight.getIntensity() wrong for 60° off-axis point");

        // =============== Boundary Values Tests ==================

        // BV01: Point exactly perpendicular to beam direction – direction · l = 0 → black.
        //   Point is on the +Y axis; direction=(1,0,0); l=(0,1,0); dot=0.
        assertEquals(Color.BLACK, light.getIntensity(new Point(0, 5, 0)),
                "ERROR: SpotLight.getIntensity() must return black for point perpendicular to beam");

        // BV02: Point behind the beam direction – direction · l < 0 → black.
        //   Point is on the -X axis; l=(-1,0,0); dot=-1 < 0.
        assertEquals(Color.BLACK, light.getIntensity(new Point(-5, 0, 0)),
                "ERROR: SpotLight.getIntensity() must return black for point behind spotlight");
    }
}