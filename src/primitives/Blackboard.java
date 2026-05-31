package primitives;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable target-area sampler for all super-sampling image improvements.
 * <p>
 * A {@code Blackboard} represents a flat, 2D sampling region centered at a
 * configurable world-space point.  It generates a list of 3D sample points
 * distributed across that region according to the chosen {@link SamplingPattern}
 * and {@link TargetShape}, then maps them into world space using two orthonormal
 * local basis vectors.
 * <p>
 * The same infrastructure is shared by every super-sampling feature:
 * <ul>
 *   <li><b>Anti-aliasing</b> – pixel area on the view plane</li>
 *   <li><b>Depth of Field</b> – aperture window around the camera origin</li>
 *   <li><b>Soft Shadows</b> – area around a positional light source</li>
 *   <li><b>Glossy / Diffuse Glass</b> – virtual target area placed on the ideal
 *       secondary (reflection / refraction) ray</li>
 * </ul>
 * <p>
 * Typical usage (method-chaining):
 * <pre>
 * List&lt;Point&gt; samples = new Blackboard()
 *     .setCenter(lightPosition)
 *     .setSize(lightRadius)
 *     .setNumSamples(81)
 *     .setPattern(SamplingPattern.JITTERED)
 *     .setShape(TargetShape.CIRCLE)
 *     .buildBasis(shadowRayDirection)
 *     .getSamplePoints();
 * </pre>
 * <p>
 * Caching policy:
 * <ul>
 *   <li>{@link SamplingPattern#GRID} – the point list is computed once and
 *       cached until any parameter changes.</li>
 *   <li>{@link SamplingPattern#RANDOM} and {@link SamplingPattern#JITTERED} –
 *       a fresh list is generated on every {@link #getSamplePoints()} call to
 *       preserve stochastic variation across pixels.</li>
 * </ul>
 */
public class Blackboard {

    // ─────────────────────────────────────────────────────────────────────────
    //  Enumerations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Distribution pattern used to place sample points inside the target area.
     */
    public enum SamplingPattern {
        /**
         * Regular {@code n × n} grid – deterministic, results are cached until
         * parameters change.  Fastest pattern; can produce faint stripe artifacts
         * on some scene configurations.
         */
        GRID,

        /**
         * Fully random (stochastic) placement – a new set of points is generated on
         * every {@link #getSamplePoints()} call.  Produces natural noise; typically
         * needs more samples than Grid to converge to a smooth result.
         */
        RANDOM,

        /**
         * Jittered grid – each grid cell receives one sample displaced by a small
         * random offset within the cell bounds.  Combines the even coverage of Grid
         * with the naturalness of stochastic sampling.
         * A new list is generated on every call (the jitter is not cached).
         */
        JITTERED
    }

    /**
     * Geometric shape of the target area.
     */
    public enum TargetShape {
        /**
         * Axis-aligned square target area.  Simplest to fill uniformly; the corners
         * introduce a slight directional bias toward the diagonals.
         */
        SQUARE,

        /**
         * Circular target area inscribed in the enclosing square.  Better isotropy;
         * approximately {@code π/4 ≈ 78.5 %} of the square area is retained, so Grid
         * and Jitter patterns should over-generate by a factor of {@code 4/π ≈ 1.273}
         * to hit the target sample count after filtering.
         */
        CIRCLE
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Fields
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * World-space center of the target area.
     */
    private Point _center;

    /**
     * First local basis vector (horizontal axis of the target area).
     */
    private Vector _vX;

    /**
     * Second local basis vector (vertical axis of the target area).
     */
    private Vector _vY;

    /**
     * Half-side length (for {@link TargetShape#SQUARE}) or radius (for
     * {@link TargetShape#CIRCLE}) of the target area, in world units.
     * A value of {@code 0} disables spreading: {@link #getSamplePoints()} returns
     * only the center point, effectively turning off super-sampling for this beam.
     */
    private double _size = 0;

    /**
     * Target number of sample points.
     * <p>
     * For {@link SamplingPattern#GRID} and {@link SamplingPattern#JITTERED} the
     * actual count is {@code ⌈√n⌉²}, which may differ slightly from this value.
     * For {@link SamplingPattern#RANDOM} exactly this many points are returned
     * (circle filtering keeps generating until the count is met).
     * <p>
     * Default: {@code 9} (equivalent to a 3 × 3 grid).
     */
    private int _numSamples = 9;

    /**
     * Distribution pattern for sample points.
     * Default: {@link SamplingPattern#GRID}.
     */
    private SamplingPattern _pattern = SamplingPattern.GRID;

    /**
     * Shape of the target area.
     * Default: {@link TargetShape#SQUARE}.
     */
    private TargetShape _shape = TargetShape.SQUARE;

    /**
     * Cached sample list, valid only for {@link SamplingPattern#GRID}.
     * Set to {@code null} by {@link #invalidateCache()} whenever any parameter
     * that affects sample generation changes.
     * {@link SamplingPattern#RANDOM} and {@link SamplingPattern#JITTERED} never
     * populate this field.
     */
    private List<Point> _cachedSamples;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a {@code Blackboard} with default settings:
     * 9 samples, Grid pattern, Square shape, size 0 (spreading disabled).
     */
    public Blackboard() {
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Chaining setters
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sets the world-space center of the target area.
     *
     * @param p the new center point
     * @return this {@code Blackboard} for chaining
     */
    public Blackboard setCenter(Point p) {
        _center = p;
        invalidateCache();
        return this;
    }

    /**
     * Sets the half-side length (for {@link TargetShape#SQUARE}) or radius (for
     * {@link TargetShape#CIRCLE}) of the target area.
     * Passing {@code 0} disables super-sampling; only the center point is returned
     * by {@link #getSamplePoints()}.
     *
     * @param s the size in world units (must be ≥ 0)
     * @return this {@code Blackboard} for chaining
     * @throws IllegalArgumentException if {@code s} is negative
     */
    public Blackboard setSize(double s) {
        if (s < 0)
            throw new IllegalArgumentException("Blackboard size must be non-negative");
        _size = s;
        invalidateCache();
        return this;
    }

    /**
     * Sets the target number of sample points.
     * For Grid and Jitter the value is rounded up to the nearest perfect square
     * ({@code ⌈√n⌉²}); for Random it is used exactly.
     *
     * @param numSamples the desired sample count (must be ≥ 1)
     * @return this {@code Blackboard} for chaining
     * @throws IllegalArgumentException if {@code numSamples} is less than 1
     */
    public Blackboard setNumSamples(int numSamples) {
        if (numSamples < 1)
            throw new IllegalArgumentException("Number of samples must be at least 1");
        _numSamples = numSamples;
        invalidateCache();
        return this;
    }

    /**
     * Sets the sampling pattern.
     *
     * @param pattern the pattern to use; must not be {@code null}
     * @return this {@code Blackboard} for chaining
     */
    public Blackboard setPattern(SamplingPattern pattern) {
        _pattern = pattern;
        invalidateCache();
        return this;
    }

    /**
     * Sets the target-area shape.
     *
     * @param shape the shape to use; must not be {@code null}
     * @return this {@code Blackboard} for chaining
     */
    public Blackboard setShape(TargetShape shape) {
        _shape = shape;
        invalidateCache();
        return this;
    }

    /**
     * Sets the local X/Y basis vectors directly.
     * <p>
     * Use this when the basis is already known — for example, when the target area
     * is the view plane and the basis vectors are the camera's {@code vRight} and
     * {@code vUp} vectors (anti-aliasing, depth of field).
     * Both vectors are normalized before storage.
     *
     * @param vX the horizontal axis of the target area
     * @param vY the vertical axis of the target area
     * @return this {@code Blackboard} for chaining
     */
    public Blackboard setBasis(Vector vX, Vector vY) {
        _vX = vX.normalize();
        _vY = vY.normalize();
        invalidateCache();
        return this;
    }

    /**
     * Computes two orthonormal basis vectors for the target area from a given
     * normal direction and stores them as {@code _vX} and {@code _vY}.
     * <p>
     * The resulting vectors are both perpendicular to {@code normal} and to each
     * other, forming a local 2D coordinate system in the plane of the target area.
     * <p>
     * Call this when the target area's orientation is defined only by its facing
     * direction — for example:
     * <ul>
     *   <li>Soft shadows with a {@code PointLight}: pass the direction from the
     *       light to the surface point ({@code l}).</li>
     *   <li>Soft shadows with a {@code SpotLight}: pass the spotlight's beam
     *       direction ({@code dir}).</li>
     *   <li>Glossy / diffuse: pass the ideal reflection or refraction direction.</li>
     * </ul>
     *
     * @param normal the direction perpendicular to the target-area plane;
     *               must be a valid (non-zero) direction vector — it need not be
     *               normalized beforehand
     * @return this {@code Blackboard} for chaining
     */
    public Blackboard buildBasis(Vector normal) {
        // Choose an arbitrary reference vector that is guaranteed not to be parallel
        // to `normal`, preventing a degenerate (zero) cross product.
        // If normal is close to AXIS_X (|dot| >= 0.9), fall back to AXIS_Y.
        Vector arbitrary = Math.abs(normal.dotProduct(Vector.AXIS_X)) < 0.9
                ? Vector.AXIS_X
                : Vector.AXIS_Y;

        // _vX is perpendicular to both normal and arbitrary (right-hand side axis)
        _vX = normal.crossProduct(arbitrary).normalize();

        // _vY completes the right-handed orthonormal frame: perpendicular to both
        // normal and _vX (up axis of the target area)
        _vY = normal.crossProduct(_vX).normalize();

        invalidateCache();
        return this;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public sample access
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the list of 3D world-space sample points for the current configuration.
     * <p>
     * <b>Edge cases:</b>
     * <ul>
     *   <li>If {@code size == 0}, or the basis vectors have not been set via
     *       {@link #setBasis} / {@link #buildBasis}, a single-element list containing
     *       only {@link #_center} is returned (super-sampling is effectively off).</li>
     *   <li>If the center has not been set, an empty list is returned.</li>
     * </ul>
     *
     * @return a non-{@code null} list of at least one sample point
     */
    public List<Point> getSamplePoints() {
        // Spreading disabled or basis not configured → single-sample fallback
        if (Util.isZero(_size) || _vX == null || _vY == null) {
            return _center != null ? List.of(_center) : List.of();
        }

        return switch (_pattern) {
            case GRID -> {
                // Deterministic: rebuild only when a parameter has changed
                if (_cachedSamples == null) {
                    _cachedSamples = generateGrid();
                }
                yield _cachedSamples;
            }
            // RANDOM and JITTERED are NOT cached — fresh samples on every call
            case RANDOM -> generateRandom();
            case JITTERED -> generateJittered();
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Converts a 2D offset {@code (x, y)} in the local coordinate system of the target
     * area into a 3D world-space {@link Point}.
     * <p>
     * Zero offsets are skipped explicitly because {@link Vector#scale(double)} with
     * a zero scalar would construct the illegal zero vector and throw an exception.
     *
     * @param x offset along the {@code _vX} (horizontal) axis
     * @param y offset along the {@code _vY} (vertical) axis
     * @return the corresponding 3D world-space point
     */
    private Point offsetToPoint(double x, double y) {
        Point p = _center;
        // Add each component only when it is non-zero to avoid zero-vector construction
        if (!Util.isZero(x)) p = p.add(_vX.scale(x));
        if (!Util.isZero(y)) p = p.add(_vY.scale(y));
        return p;
    }

    /**
     * Returns {@code true} if the 2D offset {@code (x, y)} lies inside or on the
     * boundary of the circular target area whose radius is {@link #_size}.
     *
     * @param x offset along the {@code _vX} axis
     * @param y offset along the {@code _vY} axis
     * @return {@code true} if the point is within the circle
     */
    private boolean insideCircle(double x, double y) {
        return x * x + y * y <= _size * _size;
    }

    /**
     * Generates a regular {@code gridSize × gridSize} grid of sample points, where
     * {@code gridSize = ⌈√numSamples⌉}.
     * <p>
     * Each sample is placed at the center of its grid cell.
     * When {@link TargetShape#CIRCLE} is active, samples outside the inscribed circle
     * are discarded (approximately {@code 4/π ≈ 1.273×} more cells are generated than
     * the target count to compensate).
     * <p>
     * Falls back to a single center point if all candidates are filtered out.
     *
     * @return list of grid sample points in 3D world space; never {@code null}
     */
    private List<Point> generateGrid() {
        // gridSize × gridSize cells cover the requested number of samples
        int gridSize = (int) Math.ceil(Math.sqrt(_numSamples));
        double cellSize = 2.0 * _size / gridSize;

        List<Point> points = new ArrayList<>(gridSize * gridSize);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                // Center of cell (i, j) in local 2D coordinates
                double x = -_size + (j + 0.5) * cellSize;
                double y = -_size + (i + 0.5) * cellSize;

                // Discard samples that fall outside the inscribed circle
                if (_shape == TargetShape.CIRCLE && !insideCircle(x, y)) continue;

                points.add(offsetToPoint(x, y));
            }
        }

        // Guarantee at least one sample is returned
        return points.isEmpty() ? List.of(_center) : points;
    }

    /**
     * Generates exactly {@link #_numSamples} fully random sample points inside the
     * target area.
     * <p>
     * For {@link TargetShape#CIRCLE} an accept-reject loop is used: candidate points
     * are drawn uniformly from the enclosing square and discarded if they fall outside
     * the circle.  This is the standard unbiased approach; the expected number of
     * candidates required per accepted sample is {@code 4/π ≈ 1.273}.
     *
     * @return list of random sample points in 3D world space; never {@code null}
     */
    private List<Point> generateRandom() {
        List<Point> points = new ArrayList<>(_numSamples);

        if (_shape == TargetShape.CIRCLE) {
            // Accept-reject: keep generating until the circle is filled
            while (points.size() < _numSamples) {
                double x = Util.random(-_size, _size);
                double y = Util.random(-_size, _size);
                if (insideCircle(x, y)) {
                    points.add(offsetToPoint(x, y));
                }
            }
        } else {
            // Square: every generated point is automatically inside the area
            for (int i = 0; i < _numSamples; i++) {
                double x = Util.random(-_size, _size);
                double y = Util.random(-_size, _size);
                points.add(offsetToPoint(x, y));
            }
        }

        return points;
    }

    /**
     * Generates jittered sample points: one per grid cell, each randomly displaced
     * within its cell by an offset in {@code [-cellSize/2, +cellSize/2]}.
     * <p>
     * This combines the <em>even coverage</em> of {@link SamplingPattern#GRID} with
     * the <em>natural irregularity</em> of {@link SamplingPattern#RANDOM}, avoiding
     * both the repetitive pattern artifacts of a pure grid and the clustering of pure
     * random sampling.
     * <p>
     * When {@link TargetShape#CIRCLE} is active, samples that fall outside the
     * inscribed circle after jittering are discarded.
     * Falls back to a single center point if all candidates are filtered out.
     *
     * @return list of jittered sample points in 3D world space; never {@code null}
     */
    private List<Point> generateJittered() {
        int gridSize = (int) Math.ceil(Math.sqrt(_numSamples));
        double cellSize = 2.0 * _size / gridSize;

        List<Point> points = new ArrayList<>(gridSize * gridSize);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                // Start at the cell center, then apply a random jitter within the cell
                double x = -_size + (j + 0.5) * cellSize
                        + Util.random(-cellSize / 2.0, cellSize / 2.0);
                double y = -_size + (i + 0.5) * cellSize
                        + Util.random(-cellSize / 2.0, cellSize / 2.0);

                // Discard jittered samples that fell outside the inscribed circle
                if (_shape == TargetShape.CIRCLE && !insideCircle(x, y)) continue;

                points.add(offsetToPoint(x, y));
            }
        }

        // Guarantee at least one sample is returned
        return points.isEmpty() ? List.of(_center) : points;
    }

    /**
     * Clears the cached Grid sample list.
     * Called automatically by every setter that affects sample generation, ensuring
     * that stale cached points are never returned after a configuration change.
     */
    private void invalidateCache() {
        _cachedSamples = null;
    }
}