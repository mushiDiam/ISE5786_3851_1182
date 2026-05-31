package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface for all external light sources.
 * Computes light propagation to a specific point in the scene.
 */
public interface LightSource {

    /**
     * Returns the normalized direction vector from the light source to the given point.
     *
     * @param p the illuminated point
     * @return normalized direction vector from light to point
     */
    Vector getL(Point p);

    /**
     * Returns the intensity of the light arriving at the given point.
     *
     * @param p the illuminated point
     * @return the color/intensity at that point
     */
    Color getIntensity(Point p);

    /**
     * Calculates the distance from the light source to a given point.
     *
     * @param point the point to measure the distance to
     * @return the distance
     */
    double getDistance(Point point);

    /**
     * Returns the radius of the light source's area for soft shadows.
     * Returns 0 for point-like sources (no soft shadow).
     *
     * @return the light source size (radius)
     */
    double getSize();
}