package renderer;

import primitives.Blackboard;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;
import scene.Scene;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.IntStream;

/**
 * Represents a camera in 3D space.
 *
 * <p>The camera is responsible for generating rays through a pixel grid that
 * represents the view plane. It uses the Builder pattern for construction and
 * supports configuring the view plane, resolution, ray tracer, multi-threading,
 * anti-aliasing, and depth-of-field.</p>
 *
 * <p>Super-sampling dispatch priority inside {@link #castRay}:
 * <ol>
 *   <li>Depth-of-field beam (when {@code apertureSize > 0})</li>
 *   <li>Anti-aliasing beam (when {@code antiAliasingNumSamples > 1})</li>
 *   <li>Single central ray (default — original behavior)</li>
 * </ol>
 */
public class Camera implements Cloneable {

    // ─────────────────────────────────────────────────────────────────────────
    //  Camera geometry
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Camera position in world space.
     */
    private Point _p0;

    /**
     * Camera right vector (orthonormal basis component).
     */
    private Vector _vRight;

    /**
     * Camera up vector (orthonormal basis component).
     */
    private Vector _vUp;

    /**
     * Camera forward / viewing vector (orthonormal basis component).
     */
    private Vector _vTo;

    /**
     * View plane width in world units.
     */
    private double _width;

    /**
     * View plane height in world units.
     */
    private double _height;

    /**
     * Distance from the camera to the view plane in world units.
     */
    private double _distance;

    /**
     * Number of pixels in the horizontal (X) direction.
     */
    private int _nX = 1;

    /**
     * Number of pixels in the vertical (Y) direction.
     */
    private int _nY = 1;

    /**
     * Pre-computed center point of the view plane.
     */
    private Point _pCenter;

    /**
     * Pre-computed pixel width on the view plane ({@code width / nX}).
     */
    private double _rx;

    /**
     * Pre-computed pixel height on the view plane ({@code height / nY}).
     */
    private double _ry;

    /**
     * Image writer responsible for storing and exporting pixel colors.
     */
    private ImageWriter _imageWriter;

    /**
     * Ray tracer used to compute the color for each traced ray.
     */
    private RayTracerBase _rayTracer;

    // ─────────────────────────────────────────────────────────────────────────
    //  Multi-threading
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Thread count for parallel rendering.
     * <ul>
     *   <li>{@code 0}  – single-threaded (default)</li>
     *   <li>{@code -1} – Java parallel stream (JVM auto-detects core count)</li>
     *   <li>{@code -2} – raw threads, count = logical cores − {@link #SPARE_THREADS}</li>
     *   <li>{@code ≥1} – raw threads, explicit count</li>
     * </ul>
     */
    private int _threadsCount = 0;

    /**
     * Number of logical processor cores to leave free for JVM housekeeping when
     * using the automatic thread-count mode ({@code _threadsCount == -2}).
     */
    private static final int SPARE_THREADS = 2;

    /**
     * Progress-print interval expressed as a percentage of total pixels rendered.
     * {@code 0} suppresses all progress output.
     */
    private double _printInterval = 0;

    /**
     * Shared pixel manager that distributes pixels across threads and tracks
     * rendering progress.  Initialized at the start of {@link #renderImage()}.
     */
    private PixelManager _pixelManager;

    // ─────────────────────────────────────────────────────────────────────────
    //  Anti-aliasing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Number of sample rays cast through random / grid positions inside each
     * pixel for anti-aliasing.
     * {@code 1} = disabled (a single central ray is used, identical to the
     * original single-ray behavior).
     */
    private int _antiAliasingNumSamples = 1;

    // ─────────────────────────────────────────────────────────────────────────
    //  Depth of field
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Radius of the simulated aperture window.
     * {@code 0} = disabled (pinhole camera — no depth-of-field blur).
     * Larger values produce stronger out-of-focus blur.
     */
    private double _apertureSize = 0;

    /**
     * Distance from the camera along {@code _vTo} to the focal plane.
     * Objects located exactly on this plane appear sharp; objects at other
     * distances are blurred proportionally to their offset from the plane.
     * Default: {@code 100} world units.
     */
    private double _focalDistance = 100;

    /**
     * Number of aperture sample points used per pixel during depth-of-field
     * rendering.  More samples → smoother blur, longer render time.
     * Default: {@code 81} (equivalent to a 9 × 9 grid).
     */
    private int _dofNumSamples = 81;

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared super-sampling configuration
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Point distribution pattern used when building a {@link Blackboard} for any
     * camera-level super-sampling feature (anti-aliasing or depth of field).
     * Default: {@link Blackboard.SamplingPattern#GRID}.
     */
    private Blackboard.SamplingPattern _samplingPattern = Blackboard.SamplingPattern.GRID;

    /**
     * Target area shape used when building a {@link Blackboard} for any
     * camera-level super-sampling feature.
     * Default: {@link Blackboard.TargetShape#SQUARE}.
     */
    private Blackboard.TargetShape _targetShape = Blackboard.TargetShape.SQUARE;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor / builder access
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Private constructor — construction must go through {@link Builder}.
     */
    private Camera() {
    }

    /**
     * Returns a new {@link Builder} for configuring and constructing a camera.
     *
     * @return a fresh builder instance
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Getters (only those required by external callers)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the horizontal pixel resolution of the view plane.
     *
     * @return number of pixels in the X direction
     */
    public int getNx() {
        return _nX;
    }

    /**
     * Returns the vertical pixel resolution of the view plane.
     *
     * @return number of pixels in the Y direction
     */
    public int getNy() {
        return _nY;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Ray construction
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes and returns the world-space center point of the pixel at column
     * {@code xIndex}, row {@code yIndex}.
     * <p>
     * Extracted from {@link #constructRay} so that both single-ray rendering and
     * all super-sampling beam generators can reuse the same pixel-center formula
     * without duplicating code.
     *
     * @param xIndex pixel column index (0-based, left to right)
     * @param yIndex pixel row index (0-based, top to bottom)
     * @return the 3D center of the specified pixel on the view plane
     */
    private Point getPixelCenter(int xIndex, int yIndex) {
        Point pIJ = _pCenter;

        // Horizontal offset from the view-plane center
        double xJ = (xIndex - (_nX - 1) / 2d) * _rx;
        // Vertical offset (negated because screen Y grows downward)
        double yI = -(yIndex - (_nY - 1) / 2d) * _ry;

        // Apply offsets only when non-zero to avoid constructing the zero vector
        if (!Util.isZero(xJ)) pIJ = pIJ.add(_vRight.scale(xJ));
        if (!Util.isZero(yI)) pIJ = pIJ.add(_vUp.scale(yI));

        return pIJ;
    }

    /**
     * Constructs a ray from the camera origin through the center of the specified pixel.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return the constructed camera ray for that pixel
     */
    public Ray constructRay(int xIndex, int yIndex) {
        return new Ray(_p0, getPixelCenter(xIndex, yIndex).subtract(_p0));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Rendering — public entry point
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders the full image by computing the color for every pixel.
     * <p>
     * Initializes the {@link PixelManager} for progress tracking and thread
     * coordination, then delegates to the rendering strategy selected by
     * {@link #_threadsCount}.
     *
     * @return this camera instance for method chaining
     */
    public Camera renderImage() {
        _pixelManager = new PixelManager(_nY, _nX, _printInterval);
        return switch (_threadsCount) {
            case 0 -> renderImageNoThreads();
            case -1 -> renderImageStream();
            default -> renderImageRawThreads();
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Rendering — private strategies
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders the image using the caller's thread only (no parallelism).
     * Used when {@link #_threadsCount} is {@code 0}.
     *
     * @return this camera instance for method chaining
     */
    private Camera renderImageNoThreads() {
        for (int i = 0; i < _nY; i++)
            for (int j = 0; j < _nX; j++)
                castRay(j, i);
        return this;
    }

    /**
     * Renders the image using Java's parallel {@link IntStream}.
     * The JVM manages thread creation and scheduling automatically based on
     * the available processor count.
     * Used when {@link #_threadsCount} is {@code -1}.
     *
     * @return this camera instance for method chaining
     */
    private Camera renderImageStream() {
        IntStream.range(0, _nY).parallel()
                .forEach(i -> IntStream.range(0, _nX).parallel()
                        .forEach(j -> castRay(j, i)));
        return this;
    }

    /**
     * Renders the image using {@link #_threadsCount} explicitly created
     * {@link Thread} objects. Each thread repeatedly asks the {@link PixelManager}
     * for the next unrendered pixel until all pixels are done.
     * Used when {@link #_threadsCount} is greater than {@code 0}.
     *
     * @return this camera instance for method chaining
     */
    private Camera renderImageRawThreads() {
        var threads = new LinkedList<Thread>();
        int count = _threadsCount;

        // Create the requested number of worker threads
        while (count-- > 0)
            threads.add(new Thread(() -> {
                PixelManager.Pixel pixel;
                // Each thread works until no pixels remain
                while ((pixel = _pixelManager.nextPixel()) != null)
                    castRay(pixel.col(), pixel.row());
            }));

        // Start all threads
        for (var thread : threads) thread.start();

        // Wait for every thread to finish before returning
        try {
            for (var thread : threads) thread.join();
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Pixel rendering — dispatcher
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes and writes the color for a single pixel, then signals the
     * {@link PixelManager} that the pixel is complete (for progress tracking).
     * <p>
     * Super-sampling dispatch priority:
     * <ol>
     *   <li>Depth-of-field beam – activated when {@link #_apertureSize} {@code > 0}.</li>
     *   <li>Anti-aliasing beam – activated when {@link #_antiAliasingNumSamples}
     *       {@code > 1}.</li>
     *   <li>Single central ray – the original single-ray behavior (default).</li>
     * </ol>
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     */
    private void castRay(int xIndex, int yIndex) {
        Color color;

        if (_apertureSize > 0) {
            // Depth-of-field takes priority: aperture beam toward the focal point
            color = computeColorDoF(xIndex, yIndex);
        } else if (_antiAliasingNumSamples > 1) {
            // Anti-aliasing: beam through the pixel area
            color = computeColorAntiAliasing(xIndex, yIndex);
        } else {
            // Original single-ray behavior — no super-sampling
            color = _rayTracer.traceRay(constructRay(xIndex, yIndex));
        }

        _imageWriter.writePixel(xIndex, yIndex, color);
        _pixelManager.pixelDone(); // Notify the manager for progress tracking
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Super-sampling color computation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes the anti-aliased color for a pixel by tracing multiple rays through
     * sample points distributed across the pixel area and averaging the results.
     * <p>
     * The sampling region is centered at the pixel center on the view plane.
     * Its half-extent is {@code min(rx, ry) / 2} so that all sample points remain
     * within the pixel boundary regardless of the pixel aspect ratio.
     * The region's orientation uses the camera's own right/up vectors as basis.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return the averaged color for this pixel
     */
    private Color computeColorAntiAliasing(int xIndex, int yIndex) {
        Point pixelCenter = getPixelCenter(xIndex, yIndex);

        // Use the smaller dimension so all samples stay inside the pixel
        double halfSize = Math.min(_rx, _ry) / 2.0;

        // Build the sampling region inside this pixel
        List<Point> samples = new Blackboard()
                .setCenter(pixelCenter)
                .setSize(halfSize)
                .setNumSamples(_antiAliasingNumSamples)
                .setPattern(_samplingPattern)
                .setShape(_targetShape)
                .setBasis(_vRight, _vUp)      // View-plane axes
                .getSamplePoints();

        // Trace one ray per sample point and accumulate color
        Color color = Color.BLACK;
        for (Point sample : samples)
            color = color.add(_rayTracer.traceRay(new Ray(_p0, sample.subtract(_p0))));

        // Average: divide accumulated color by the actual sample count
        return color.reduce(samples.size());
    }

    /**
     * Computes the depth-of-field color for a pixel by tracing rays from multiple
     * sample points on the aperture window, all converging toward the same focal
     * point, and averaging the results.
     * <p>
     * <b>Focal point computation:</b> the central pixel ray is intersected with the
     * focal plane — a plane perpendicular to {@code _vTo} at distance
     * {@link #_focalDistance} from the camera.  This guarantees all focal points lie
     * on a true flat plane and not on a curved surface (which would distort the image).
     * <p>
     * <b>Aperture sampling:</b> a {@link Blackboard} centered at the camera origin
     * ({@code _p0}), with radius {@link #_apertureSize}, generates the aperture
     * sample points.  The aperture lies in the same plane as the camera (spanned by
     * {@code _vRight} and {@code _vUp}).
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return the averaged depth-of-field color for this pixel
     */
    private Color computeColorDoF(int xIndex, int yIndex) {
        // ── Step 1: find the focal point for this pixel ──────────────────────
        // Central ray direction from the camera through the pixel center
        Point pixelCenter = getPixelCenter(xIndex, yIndex);
        Vector centralDir = pixelCenter.subtract(_p0).normalize();

        // Intersect the central ray with the focal plane.
        // The focal plane is perpendicular to _vTo at distance _focalDistance.
        //   t = _focalDistance / (vTo · dir)
        // This places focalPoint on the true focal plane, not on a curved surface.
        double vToDotDir = Util.alignZero(_vTo.dotProduct(centralDir));
        double t = _focalDistance / vToDotDir;
        Point focalPoint = _p0.add(centralDir.scale(t));

        // ── Step 2: sample the aperture window ───────────────────────────────
        // The aperture is centered at the camera origin, spanning the right/up plane
        List<Point> aperturePoints = new Blackboard()
                .setCenter(_p0)
                .setSize(_apertureSize)
                .setNumSamples(_dofNumSamples)
                .setPattern(_samplingPattern)
                .setShape(_targetShape)
                .setBasis(_vRight, _vUp)      // Aperture lies in the camera plane
                .getSamplePoints();

        // ── Step 3: cast a ray from each aperture sample toward the focal point ─
        Color color = Color.BLACK;
        for (Point aperturePoint : aperturePoints) {
            // Direction from aperture sample to focal point (guaranteed non-zero
            // in practice because focalDistance >> apertureSize)
            Vector dir = focalPoint.subtract(aperturePoint);
            color = color.add(_rayTracer.traceRay(new Ray(aperturePoint, dir)));
        }

        // Average color across all aperture samples
        return color.reduce(aperturePoints.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Image output
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws a colored grid over the rendered image.
     * Useful for debugging pixel alignment and view-plane configuration.
     *
     * @param interval the pixel spacing between grid lines
     * @param color    the color to use for the grid lines
     * @return this camera instance for method chaining
     */
    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; i++)
            for (int j = 0; j < _nX; j++)
                if (i % interval == 0 || j % interval == 0)
                    _imageWriter.writePixel(j, i, color);
        return this;
    }

    /**
     * Writes the rendered image to disk under the given file name.
     *
     * @param imageName the output file name (without extension)
     */
    public void writeToImage(String imageName) {
        _imageWriter.writeToImage(imageName);
    }

    /**
     * Creates a shallow clone of this camera.
     * Used by {@link Builder#build()} to return an independent camera instance.
     *
     * @return a cloned camera instance
     * @throws CloneNotSupportedException if cloning is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Builder
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fluent builder for {@link Camera}.
     * <p>
     * All configuration is set here; the resulting {@link Camera} instance returned
     * by {@link #build()} is effectively immutable (the clone isolates it from the
     * builder's internal camera object).
     */
    public static class Builder {

        /**
         * Camera instance being configured step by step.
         */
        private final Camera _camera;

        /**
         * Optional look-at target point (alternative to an explicit forward vector).
         */
        private Point _target = null;

        /**
         * Up vector supplied explicitly by the user.
         */
        private Vector _explicitUp = null;

        /**
         * Forward vector supplied explicitly by the user.
         */
        private Vector _explicitTo = null;

        /**
         * Roll rotation around the viewing direction, in degrees.
         */
        private double _rotationAngleDegrees = 0;

        /**
         * Creates a new builder backed by a fresh, unconfigured camera instance.
         */
        public Builder() {
            _camera = new Camera();
        }

        // ── Geometry ─────────────────────────────────────────────────────────

        /**
         * Sets the camera's world-space position.
         *
         * @param location the camera location
         * @return this builder for chaining
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera direction using explicit forward and up vectors.
         *
         * @param to the forward direction vector (need not be normalized)
         * @param up the up vector (need not be normalized)
         * @return this builder for chaining
         */
        public Builder setDirection(Vector to, Vector up) {
            _explicitTo = to;
            _explicitUp = up;
            return this;
        }

        /**
         * Sets the camera direction using a look-at target point and an explicit
         * up vector.
         *
         * @param target the point the camera looks at
         * @param up     the up vector
         * @return this builder for chaining
         */
        public Builder setDirection(Point target, Vector up) {
            _target = target;
            _explicitUp = up;
            return this;
        }

        /**
         * Sets the camera direction using a look-at target point with the default
         * up vector {@code (0, 1, 0)}.
         *
         * @param target the point the camera looks at
         * @return this builder for chaining
         */
        public Builder setDirection(Point target) {
            _target = target;
            return this;
        }

        /**
         * Sets the view plane dimensions.
         *
         * @param width  view plane width in world units
         * @param height view plane height in world units
         * @return this builder for chaining
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane.
         *
         * @param distance distance in world units
         * @return this builder for chaining
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the pixel resolution of the rendered image.
         *
         * @param nX number of horizontal pixels
         * @param nY number of vertical pixels
         * @return this builder for chaining
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Sets the ray tracer implementation to use with this camera.
         *
         * @param scene the scene to render
         * @param type  the desired ray tracer strategy
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code type} is not supported
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracer = new SimpleRayTracer(scene);
            } else {
                throw new IllegalArgumentException("Unsupported RayTracerType: " + type);
            }
            return this;
        }

        /**
         * Applies a roll rotation around the viewing direction.
         *
         * @param angleDegrees rotation angle in degrees (positive = clockwise when
         *                     looking forward)
         * @return this builder for chaining
         */
        public Builder setRotation(double angleDegrees) {
            _rotationAngleDegrees = angleDegrees;
            return this;
        }

        // ── Multi-threading ───────────────────────────────────────────────────

        /**
         * Configures the multi-threading rendering mode.
         * <p>
         * Parameter semantics:
         * <ul>
         *   <li>{@code -2} – raw threads, count set to {@code availableProcessors() − SPARE_THREADS}</li>
         *   <li>{@code -1} – Java parallel stream (JVM chooses thread count)</li>
         *   <li>{@code  0} – single-threaded (default)</li>
         *   <li>{@code ≥1} – raw threads, exactly this many</li>
         * </ul>
         *
         * @param threads threading mode / explicit thread count
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code threads} is less than {@code -2}
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2)
                throw new IllegalArgumentException("Multithreading parameter must be -2 or higher");
            if (threads == -2) {
                // Automatic: reserve SPARE_THREADS cores for the JVM
                int cores = Runtime.getRuntime().availableProcessors() - SPARE_THREADS;
                _camera._threadsCount = cores <= 2 ? 1 : cores; // at least 1 thread
            } else {
                _camera._threadsCount = threads;
            }
            return this;
        }

        /**
         * Sets the console progress-print interval during rendering.
         * Pass {@code 0} to suppress all progress output.
         *
         * @param interval print interval as a percentage of total pixels (e.g. {@code 1}
         *                 prints a line every 1 % of the image)
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code interval} is negative
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0)
                throw new IllegalArgumentException("Print interval must be non-negative");
            _camera._printInterval = interval;
            return this;
        }

        // ── Anti-aliasing ─────────────────────────────────────────────────────

        /**
         * Enables and configures anti-aliasing super-sampling.
         * <p>
         * Each pixel is sampled at {@code numSamples} different positions inside
         * its area; the resulting colors are averaged.  Pass {@code 1} (or omit
         * this call) to disable anti-aliasing and use a single central ray.
         * <p>
         * Recommended values: 9 (3×3) for drafts, 81 (9×9) for demos,
         * 289–1089 (17×17–33×33) for final renders.
         *
         * @param numSamples number of sample rays per pixel (must be ≥ 1)
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code numSamples} is less than 1
         */
        public Builder setAntiAliasing(int numSamples) {
            if (numSamples < 1)
                throw new IllegalArgumentException("Anti-aliasing sample count must be at least 1");
            _camera._antiAliasingNumSamples = numSamples;
            return this;
        }

        // ── Depth of field ────────────────────────────────────────────────────

        /**
         * Sets the radius of the simulated aperture window for depth-of-field blur.
         * <p>
         * Objects on the focal plane (set by {@link #setFocalDistance}) appear sharp;
         * objects at other depths are blurred proportionally to their distance from
         * the plane.  A larger aperture produces stronger blur.
         * Pass {@code 0} (or omit this call) to keep the pinhole camera model.
         *
         * @param size aperture radius in world units (must be ≥ 0; {@code 0} = disabled)
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code size} is negative
         */
        public Builder setApertureSize(double size) {
            if (size < 0)
                throw new IllegalArgumentException("Aperture size must be non-negative");
            _camera._apertureSize = size;
            return this;
        }

        /**
         * Sets the focal distance for depth-of-field rendering.
         * <p>
         * This is the distance from the camera along {@code vTo} to the focal plane.
         * Objects on the focal plane appear sharp.
         *
         * @param distance focal plane distance in world units (must be positive)
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code distance} is not positive
         */
        public Builder setFocalDistance(double distance) {
            if (distance <= 0)
                throw new IllegalArgumentException("Focal distance must be positive");
            _camera._focalDistance = distance;
            return this;
        }

        /**
         * Sets the number of aperture sample points used per pixel for depth-of-field.
         * More samples produce a smoother blur at the cost of longer render times.
         *
         * @param numSamples aperture sample count (must be ≥ 1)
         * @return this builder for chaining
         * @throws IllegalArgumentException if {@code numSamples} is less than 1
         */
        public Builder setDofNumSamples(int numSamples) {
            if (numSamples < 1)
                throw new IllegalArgumentException("DOF sample count must be at least 1");
            _camera._dofNumSamples = numSamples;
            return this;
        }

        // ── Shared sampling configuration ─────────────────────────────────────

        /**
         * Sets the point distribution pattern used for all camera-level super-sampling
         * (both anti-aliasing and depth of field).
         *
         * @param pattern the sampling pattern
         * @return this builder for chaining
         */
        public Builder setSamplingPattern(Blackboard.SamplingPattern pattern) {
            _camera._samplingPattern = pattern;
            return this;
        }

        /**
         * Sets the target-area shape used for all camera-level super-sampling
         * (both anti-aliasing and depth of field).
         *
         * @param shape the target area shape
         * @return this builder for chaining
         */
        public Builder setTargetShape(Blackboard.TargetShape shape) {
            _camera._targetShape = shape;
            return this;
        }

        // ── Build ─────────────────────────────────────────────────────────────

        /**
         * Validates all configuration, computes the camera basis vectors and
         * view-plane geometry, and returns an immutable {@link Camera} instance.
         *
         * @return the fully constructed camera
         * @throws MissingResourceException if the camera location or direction was not set
         * @throws IllegalArgumentException if any configuration value is geometrically invalid
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            applyRotation();
            checkViewPlane();

            // Default ray tracer if the user did not set one
            if (_camera._rayTracer == null)
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException e) {
                return null; // Cannot happen: Camera implements Cloneable
            }
        }

        // ── Build helpers (private) ───────────────────────────────────────────

        /**
         * Validates the resolution and initializes the {@link ImageWriter}.
         *
         * @throws IllegalArgumentException if either dimension is not strictly positive
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Resolution must be strictly positive.");
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Validates the location and direction inputs and computes the three
         * orthonormal camera basis vectors ({@code _vTo}, {@code _vRight}, {@code _vUp}).
         *
         * @throws MissingResourceException if the location or direction is absent
         * @throws IllegalArgumentException if the forward and up vectors are parallel
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null)
                throw new MissingResourceException(
                        "The camera's center (location) is missing", "Camera", "_p0");
            if (_explicitTo == null && _target == null)
                throw new MissingResourceException(
                        "The camera's direction is missing", "Camera", "direction");

            // Default up vector if none was supplied
            if (_explicitUp == null)
                _explicitUp = new Vector(0, 1, 0);

            // Forward vector: from explicit direction or from look-at target
            _camera._vTo = (_explicitTo != null)
                    ? _explicitTo.normalize()
                    : _target.subtract(_camera._p0).normalize();

            // Right vector: forward × up (will throw if they are parallel)
            try {
                _camera._vRight = _camera._vTo.crossProduct(_explicitUp).normalize();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The vTo and vUp vectors cannot be parallel.");
            }

            // Recompute up to ensure a perfectly orthonormal basis
            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
        }

        /**
         * Applies a roll rotation to the camera's up and right vectors around the
         * viewing axis. Does nothing if the rotation angle is zero.
         */
        private void applyRotation() {
            if (Util.isZero(_rotationAngleDegrees)) return;

            double angleRad = Math.toRadians(_rotationAngleDegrees);
            double cosTheta = Util.alignZero(Math.cos(angleRad));
            double sinTheta = Util.alignZero(Math.sin(angleRad));

            // Rodrigues' rotation in the up/right plane:
            //   newVUp = cos(θ)·vUp + sin(θ)·vRight
            Vector newVUp = null;
            if (cosTheta != 0) newVUp = _camera._vUp.scale(cosTheta);
            if (sinTheta != 0) {
                Vector rightComponent = _camera._vRight.scale(sinTheta);
                newVUp = (newVUp == null) ? rightComponent : newVUp.add(rightComponent);
            }

            if (newVUp != null) {
                _camera._vUp = newVUp.normalize();
                _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            }
        }

        /**
         * Validates the view plane dimensions and distance, then pre-computes the
         * view plane center point and per-pixel step sizes ({@code _rx}, {@code _ry}).
         *
         * @throws IllegalArgumentException if any view plane value is not strictly positive
         */
        private void checkViewPlane() {
            if (_camera._width <= 0 || _camera._height <= 0)
                throw new IllegalArgumentException("View Plane dimensions must be strictly positive.");
            if (_camera._distance <= 0)
                throw new IllegalArgumentException("The View Plane distance must be strictly positive.");

            _camera._pCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
            _camera._rx = _camera._width / _camera._nX;
            _camera._ry = _camera._height / _camera._nY;
        }
    }
}