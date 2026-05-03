package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.MissingResourceException;

/**
 * Represents a camera in 3D space.
 * <p>
 * The camera is responsible for generating geometric rays through a pixel grid
 * representing the View Plane. It uses the Builder design pattern for its instantiation.
 * </p>
 */
public class Camera implements Cloneable {

    /**
     * The origin (position) point of the camera.
     */
    private Point _p0;

    /**
     * Directional vector pointing to the right of the camera.
     */
    private Vector _vRight;

    /**
     * Directional vector pointing upwards from the camera.
     */
    private Vector _vUp;

    /**
     * Directional vector pointing forward (target direction).
     */
    private Vector _vTo;

    /**
     * The physical width of the View Plane.
     */
    private double _width;

    /**
     * The physical height of the View Plane.
     */
    private double _height;

    /**
     * The focal distance between the camera and the View Plane.
     */
    private double _distance;

    /**
     * Number of columns (horizontal resolution) of the View Plane grid.
     */
    private int _nX = 1;

    /**
     * Number of rows (vertical resolution) of the View Plane grid.
     */
    private int _nY = 1;

    // --- Calculated fields ---

    /**
     * The center point of the View Plane.
     */
    private Point _pCenter;

    /**
     * The physical width of a single pixel.
     */
    private double _rx;

    /**
     * The physical height of a single pixel.
     */
    private double _ry;

    // --- Rendering fields ---

    /**
     * The image writer used to create the image file.
     */
    private ImageWriter _imageWriter;

    /**
     * The ray tracer used to calculate colors in the scene.
     */
    private RayTracerBase _rayTracer;

    /**
     * Private default constructor.
     * Camera instantiation must be done via the {@link Builder}.
     */
    private Camera() {
    }

    /**
     * Provides a new instance of the camera builder.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Retrieves the horizontal resolution.
     *
     * @return The number of columns of the View Plane grid.
     */
    public int getNx() {
        return _nX;
    }

    /**
     * Retrieves the vertical resolution.
     *
     * @return The number of rows of the View Plane grid.
     */
    public int getNy() {
        return _nY;
    }

    /**
     * Constructs a ray originating from the camera and passing through the center
     * of a specific pixel on the viewing plane.
     *
     * @param xIndex The column index of the pixel (X-axis, left to right)
     * @param yIndex The row index of the pixel (Y-axis, top to bottom)
     * @return The geometric {@link Ray} passing through the center of the pixel.
     */
    public Ray constructRay(int xIndex, int yIndex) {
        Point pIJ = _pCenter;

        double xJ = (xIndex - (_nX - 1) / 2d) * _rx;
        double yI = -(yIndex - (_nY - 1) / 2d) * _ry;

        if (!primitives.Util.isZero(xJ)) {
            pIJ = pIJ.add(_vRight.scale(xJ));
        }
        if (!primitives.Util.isZero(yI)) {
            pIJ = pIJ.add(_vUp.scale(yI));
        }

        return new Ray(_p0, pIJ.subtract(_p0));
    }

    /**
     * Renders the image by casting rays through every pixel and coloring them.
     *
     * @return the current camera instance
     */
    public Camera renderImage() {
        for (int i = 0; i < _nY; i++) {
            for (int j = 0; j < _nX; j++) {
                castRay(j, i);
            }
        }
        return this;
    }

    /**
     * Casts a single ray for a specific pixel and writes its calculated color to the image.
     *
     * @param xIndex the pixel's column index
     * @param yIndex the pixel's row index
     */
    private void castRay(int xIndex, int yIndex) {
        Ray ray = constructRay(xIndex, yIndex);
        Color color = _rayTracer.traceRay(ray);
        _imageWriter.writePixel(xIndex, yIndex, color);
    }

    /**
     * Prints a grid on the image to help visualize pixel alignment.
     *
     * @param interval the gap (in pixels) between grid lines
     * @param color    the color of the grid lines
     * @return the current camera instance
     */
    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; i++) {
            for (int j = 0; j < _nX; j++) {
                if (i % interval == 0 || j % interval == 0) {
                    _imageWriter.writePixel(j, i, color);
                }
            }
        }
        return this;
    }

    /**
     * Delegates the file saving operation to the image writer.
     *
     * @param imageName the name of the final image file
     */
    public void writeToImage(String imageName) {
        _imageWriter.writeToImage(imageName);
    }

    /**
     * Creates and returns an exact copy of this camera instance.
     *
     * @return A clone of the Camera object.
     * @throws CloneNotSupportedException If cloning fails.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Static inner class implementing the Builder design pattern to securely
     * configure and construct a {@link Camera} instance.
     */
    public static class Builder {

        /**
         * The camera instance currently under construction.
         */
        private final Camera _camera;

        /**
         * Temporary target point used to define the direction.
         */
        private Point _target = null;

        /**
         * Temporary explicit 'Up' vector.
         */
        private Vector _explicitUp = null;

        /**
         * Temporary explicit 'To' vector.
         */
        private Vector _explicitTo = null;

        /**
         * Initializes a new builder with a blank camera.
         */
        public Builder() {
            _camera = new Camera();
        }

        /**
         * Sets the position (origin) of the camera.
         *
         * @param location The origin point of the camera.
         * @return The current Builder instance for chaining.
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera's direction using two vectors.
         * The vectors must be orthogonal.
         *
         * @param to The vector pointing forward.
         * @param up The vector pointing upwards.
         * @return The current Builder instance for chaining.
         */
        public Builder setDirection(Vector to, Vector up) {
            _explicitTo = to;
            _explicitUp = up;
            return this;
        }

        /**
         * Sets the camera's direction towards a target point with an explicit 'up' vector.
         *
         * @param target The geometric point the camera should look at.
         * @param up     The vector indicating the "up" direction.
         * @return The current Builder instance for chaining.
         */
        public Builder setDirection(Point target, Vector up) {
            _target = target;
            _explicitUp = up;
            return this;
        }

        /**
         * Sets the camera's direction towards a target point (the default 'up' will be the Y-axis).
         *
         * @param target The geometric point the camera should look at.
         * @return The current Builder instance for chaining.
         */
        public Builder setDirection(Point target) {
            _target = target;
            return this;
        }

        /**
         * Sets the physical dimensions of the View Plane.
         *
         * @param width  The physical width of the View Plane.
         * @param height The physical height of the View Plane.
         * @return The current Builder instance for chaining.
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance between the camera and its View Plane.
         *
         * @param distance The focal distance.
         * @return The current Builder instance for chaining.
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the pixel resolution of the final image.
         *
         * @param nX Number of columns (horizontal resolution).
         * @param nY Number of rows (vertical resolution).
         * @return The current Builder instance for chaining.
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Injects the desired ray tracer into the camera setup.
         *
         * @param scene the scene to be rendered
         * @param type  the type of ray tracer to use
         * @return the current builder instance
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracer = new SimpleRayTracer(scene);
            } else {
                throw new IllegalArgumentException("Unsupported RayTracerType");
            }
            return this;
        }

        /**
         * Verifies the validity of all injected data, performs necessary calculations
         * (orthogonality, pixel sizes), and returns the final camera.
         *
         * @return A valid and cloned {@link Camera} instance.
         * @throws MissingResourceException If a critical parameter (position, direction) is missing.
         * @throws IllegalArgumentException If geometric data is invalid (negative values, non-orthogonal vectors).
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            // Default initialization if a ray tracer wasn't specifically provided
            if (_camera._rayTracer == null) {
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
            }

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        /**
         * Verifies that the resolution is strictly positive and initializes the image writer.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution must be strictly positive.");
            }
            // Initialize the ImageWriter based on the validated resolution
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Calculates and verifies the orthogonality of the camera's coordinate system (vTo, vUp, vRight).
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null) {
                throw new MissingResourceException("The camera's center (location) is missing", "Camera", "_p0");
            }
            if (_explicitTo == null && _target == null) {
                throw new MissingResourceException("The camera's direction is missing", "Camera", "direction");
            }

            if (_explicitUp == null) {
                _explicitUp = new Vector(0, 1, 0);
            }

            if (_explicitTo != null) {
                _camera._vTo = _explicitTo.normalize();
            } else {
                _camera._vTo = _target.subtract(_camera._p0).normalize();
            }

            try {
                _camera._vRight = _camera._vTo.crossProduct(_explicitUp).normalize();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("The vTo and vUp vectors cannot be parallel.");
            }

            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
        }

        /**
         * Verifies the View Plane dimensions and pre-calculates the center and pixel sizes.
         */
        private void checkViewPlane() {
            if (_camera._width <= 0 || _camera._height <= 0) {
                throw new IllegalArgumentException("View Plane dimensions must be strictly positive.");
            }
            if (_camera._distance <= 0) {
                throw new IllegalArgumentException("The View Plane distance must be strictly positive.");
            }

            _camera._pCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
            _camera._rx = _camera._width / _camera._nX;
            _camera._ry = _camera._height / _camera._nY;
        }
    }
}
