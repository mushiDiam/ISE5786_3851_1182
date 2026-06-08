package geometries.impl;

import geometries.api.AABB;
import geometries.api.Intersectable;
import primitives.Ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * A composite of {@link Intersectable} objects (Composite design pattern).
 *
 * <p>Besides grouping geometries, this class doubles as the <b>BVH tree node</b>:
 * a hierarchy is just a {@code Geometries} whose children are themselves
 * {@code Geometries}. This reuse of the existing Composite (rather than a
 * separate tree class) is exactly what the mini-project requires.</p>
 */
public class Geometries extends Intersectable {

    /**
     * The child intersectables (leaf geometries and/or sub-groups).
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Maximum number of geometries kept in a single leaf node when building the
     * hierarchy. Smaller leaves prune more but build a deeper tree.
     */
    private static final int MAX_LEAF_SIZE = 2;

    /**
     * Constructs an empty composite.
     */
    public Geometries() {
    }

    /**
     * Constructs a composite from initial intersectables.
     *
     * @param geometries the intersectables to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds intersectables to this composite.
     *
     * @param geometries the intersectables to add
     */
    public void add(Intersectable... geometries) {
        Collections.addAll(this.geometries, geometries);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = null;
        for (Intersectable geometry : geometries) {
            // Each child re-applies its own CBR check inside calcIntersections,
            // so whole sub-trees are skipped when the ray misses their box.
            var geoIntersections = geometry.calcIntersections(ray, maxDistance);
            if (geoIntersections != null) {
                if (intersections == null) intersections = new LinkedList<>();
                intersections.addAll(geoIntersections);
            }
        }
        return intersections;
    }

    @Override
    protected AABB calculateBoundingBox() {
        AABB box = null;
        for (Intersectable g : geometries) {
            AABB childBox = g.getBoundingBox();
            box = (box == null) ? childBox : box.union(childBox);
        }
        return box == null ? AABB.INFINITE : box;
    }

    @Override
    public Intersectable buildBoundingBox() {
        for (Intersectable g : geometries)
            g.buildBoundingBox();          // children first...
        return super.buildBoundingBox();   // ...then this node = union of children
    }

    /**
     * Reorganizes this (flat) composite into a binary BVH tree in place, then
     * builds every bounding box. Call this from a test to switch acceleration ON;
     * skipping it leaves the scene flat with no boxes (acceleration OFF), so the
     * very same code renders both ways with no system changes.
     *
     * <p>Unbounded geometries (planes, tubes) are kept at the root and always
     * tested, since they cannot be placed in a spatial tree.</p>
     *
     * @return this composite, for chaining
     */
    public Geometries buildHierarchy() {
        List<Intersectable> leaves = new ArrayList<>();
        collectLeaves(this, leaves);

        List<Intersectable> bounded = new ArrayList<>();
        List<Intersectable> unbounded = new ArrayList<>();
        for (Intersectable leaf : leaves) {
            leaf.buildBoundingBox();
            if (leaf.getBoundingBox().isFinite()) bounded.add(leaf);
            else unbounded.add(leaf);
        }

        geometries.clear();
        if (bounded.size() > MAX_LEAF_SIZE) {
            Geometries tree = split(bounded, 0, bounded.size());
            geometries.addAll(tree.geometries);
        } else {
            geometries.addAll(bounded);
        }
        geometries.addAll(unbounded);

        buildBoundingBox();
        return this;
    }

    /**
     * Recursively collects every leaf (non-composite) intersectable.
     *
     * @param node the node to descend into
     * @param out  the accumulating list of leaves
     */
    private static void collectLeaves(Intersectable node, List<Intersectable> out) {
        if (node instanceof Geometries group)
            for (Intersectable child : group.geometries)
                collectLeaves(child, out);
        else
            out.add(node);
    }

    /**
     * Recursively splits a list of (already boxed) geometries into a balanced
     * binary tree, dividing along the longest axis of the centroid spread.
     *
     * @param items the geometries (sorted in place during the recursion)
     * @param from  start index (inclusive)
     * @param to    end index (exclusive)
     * @return the sub-tree node covering {@code items[from, to)}
     */
    private static Geometries split(List<Intersectable> items, int from, int to) {
        Geometries node = new Geometries();
        int count = to - from;
        if (count <= MAX_LEAF_SIZE) {
            for (int i = from; i < to; i++) node.add(items.get(i));
            return node;
        }

        int axis = longestAxis(items, from, to);
        Comparator<Intersectable> byCenter = switch (axis) {
            case 0 -> Comparator.comparingDouble(g -> g.getBoundingBox().getCenterX());
            case 1 -> Comparator.comparingDouble(g -> g.getBoundingBox().getCenterY());
            default -> Comparator.comparingDouble(g -> g.getBoundingBox().getCenterZ());
        };
        items.subList(from, to).sort(byCenter);

        int mid = from + count / 2;
        node.add(split(items, from, mid), split(items, mid, to));
        return node;
    }

    /**
     * Finds the axis (0=X, 1=Y, 2=Z) along which the geometry centers are most
     * spread out — the best axis to split on.
     *
     * @param items the geometries
     * @param from  start index (inclusive)
     * @param to    end index (exclusive)
     * @return the index of the longest axis
     */
    private static int longestAxis(List<Intersectable> items, int from, int to) {
        double minX = Double.POSITIVE_INFINITY, minY = minX, minZ = minX;
        double maxX = Double.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (int i = from; i < to; i++) {
            AABB b = items.get(i).getBoundingBox();
            minX = Math.min(minX, b.getCenterX());
            maxX = Math.max(maxX, b.getCenterX());
            minY = Math.min(minY, b.getCenterY());
            maxY = Math.max(maxY, b.getCenterY());
            minZ = Math.min(minZ, b.getCenterZ());
            maxZ = Math.max(maxZ, b.getCenterZ());
        }
        double ex = maxX - minX, ey = maxY - minY, ez = maxZ - minZ;
        if (ex >= ey && ex >= ez) return 0;
        return (ey >= ez) ? 1 : 2;
    }
}