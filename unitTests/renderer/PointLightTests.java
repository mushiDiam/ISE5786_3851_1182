package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link lighting.PointLight}.
 * <p>
 * Verifies:
 * <ul>
 *   <li>{@link lighting.PointLight#getL(Point)}</li>
 *   <li>{@link lighting.PointLight#getIntensity(Point)}</li>
 * </ul>
 */
class PointLightTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    PointLightTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Tolerance for floating-point comparisons.
     */
    private static final double DELTA = 1e-6;

    /**
     * Position of the point light.
     */
    private static final Point POSITION = new Point(0, 0, 0);

    /**
     * Original intensity of the light.
     */
    private static final Color INTENSITY = new Color(300, 200, 100);

    // ══════════════════════════════════════════════════════════════════════════
    //  getL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link lighting.PointLight#getL(Point)}.
     * <p>
     * The direction must be normalized and point from the light to the surface point.
     */
    @Test
    void testGetL() {
        PointLight light = new PointLight(INTENSITY, POSITION);

        // ============ Equivalence Partitions Tests ==============

        // EP01: Point along the positive X axis – direction must be (1,0,0).
        Point p1 = new Point(5, 0, 0);
        assertEquals(new Vector(1, 0, 0), light.getL(p1),
                "ERROR: PointLight.getL() returned wrong direction for point on X axis");

        // EP02: Point in an arbitrary position – result must be normalized.
        Point p2 = new Point(3, 4, 0);   // distance 5 from origin
        Vector expected = new Vector(3, 4, 0).normalize();  // (0.6, 0.8, 0)
        assertEquals(expected, light.getL(p2),
                "ERROR: PointLight.getL() returned wrong normalized direction");

        // =============== Boundary Values Tests ==================

        // BV01: Point directly above the light – direction must be (0,1,0).
        Point p3 = new Point(0, 10, 0);
        assertEquals(new Vector(0, 1, 0), light.getL(p3),
                "ERROR: PointLight.getL() wrong direction for point directly above light");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  getIntensity
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link lighting.PointLight#getIntensity(Point)}.
     * <p>
     * Intensity = {@code I0 / (kC + kL*d + kQ*d²)}.
     */
    @Test
    void testGetIntensity() {
        // EP01: No attenuation (kC=1, kL=0, kQ=0) – intensity equals the original.
        PointLight lightNoAtten = new PointLight(INTENSITY, POSITION);
        assertEquals(INTENSITY, lightNoAtten.getIntensity(new Point(100, 0, 0)),
                "ERROR: PointLight.getIntensity() with no attenuation must return original intensity");

        // EP02: Linear attenuation – intensity at distance 10 with kL=0.1 → factor = 1/(1+1) = 0.5.
        //   I0=(300,200,100), divided by 2 → (150,100,50).
        PointLight lightLinear = new PointLight(INTENSITY, POSITION).setKl(0.1);
        Color expected = new Color(150, 100, 50);
        assertEquals(expected, lightLinear.getIntensity(new Point(10, 0, 0)),
                "ERROR: PointLight.getIntensity() with linear attenuation is incorrect");

        // =============== Boundary Values Tests ==================

        // BV01: Quadratic attenuation at distance 10, kQ=0.01 → factor = 1/(1+1) = 0.5.
        PointLight lightQuad = new PointLight(INTENSITY, POSITION).setKq(0.01);
        assertEquals(expected, lightQuad.getIntensity(new Point(10, 0, 0)),
                "ERROR: PointLight.getIntensity() with quadratic attenuation is incorrect");
    }
}