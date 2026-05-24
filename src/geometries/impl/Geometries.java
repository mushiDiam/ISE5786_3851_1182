package geometries.impl;

import geometries.api.Intersectable;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Composite class representing a collection of geometric shapes.
 * It implements the Intersectable interface, allowing it to be treated
 * as a single geometric entity using the Composite design pattern.
 * 
 * This class manages a collection of geometries and provides intersection
 * calculations by aggregating results from all contained geometries.
 * 
 * @author [Student ID]
 * @version 1.0
 */
public class Geometries extends Intersectable {

    /**
     * List of geometries in the collection. Initialized at declaration.
     */
    private final List<Intersectable> geometries = new ArrayList<>();


    /**
     * Default constructor creating an empty collection of geometries.
     */
    public Geometries() {
    }

    /**
     * Constructor that accepts a variable number of geometries.
     * Uses the add method to comply with the DRY principle.
     *
     * @param geometries an array or comma-separated list of geometries to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds geometries to the collection.
     * Uses Java's built-in Collections.addAll instead of manual logic.
     *
     * @param geometries an array or comma-separated list of geometries to add
     */
    public void add(Intersectable... geometries) {
        Collections.addAll(this.geometries, geometries);
    }

    /**
     * Finds intersections between a ray and all geometries in the collection.
     * 
     * Passes the maxDistance parameter down to each geometry to filter out
     * intersections beyond the specified distance. Results from all geometries
     * are collected and combined into a single list.
     *
     * @param ray         the ray to test for intersections
     * @param maxDistance the maximum allowed distance for an intersection
     * @return a list of all intersection points from all geometries, or null
     *         if no intersections are found
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = null;

        for (Intersectable geometry : geometries) {
            // Pass maxDistance down to each individual shape
            var geoIntersections = geometry.calcIntersections(ray, maxDistance);

            if (geoIntersections != null) {
                if (intersections == null) {
                    intersections = new LinkedList<>();
                }
                intersections.addAll(geoIntersections);
            }
        }
        return intersections;
    }
}