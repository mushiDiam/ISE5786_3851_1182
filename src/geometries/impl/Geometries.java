package geometries.impl;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite class representing a collection of geometric shapes.
 * It implements the Intersectable interface, allowing it to be treated
 * as a single geometric entity.
 */
public class Geometries extends Intersectable {

    /**
     * List of geometries. Initialized at declaration.
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Default constructor (empty collection).
     */
    public Geometries() {
    }

    /**
     * Constructor that accepts a variable number of geometries[cite: 68].
     * Uses the add method to comply with the DRY principle[cite: 70].
     *
     * @param geometries an array or comma-separated list of geometries
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds geometries to the collection[cite: 69].
     * Uses Java's built-in Collections.addAll instead of manual logic.
     *
     * @param geometries an array or comma-separated list of geometries to add
     */
    public void add(Intersectable... geometries) {
        Collections.addAll(this.geometries, geometries);
    }

    /**
     * Finds intersections between a ray and all geometries in the collection.
     * * @param ray the ray to test for intersections
     * @return a list of all intersection points, or null if none are found
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        // Do not create the list object before the loop
        List<Point> intersections = null;

        // Collect intersections from all geometries using delegation
        for (Intersectable geo : geometries) {
            List<Point> geoIntersections = geo.findIntersections(ray);

            // Only create the list object when an item returns a non-null list
            if (geoIntersections != null) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                // Add to the created list
                intersections.addAll(geoIntersections);
            }
        }

        // Return null if no intersections were found, not an empty list
        return intersections;
    }
}