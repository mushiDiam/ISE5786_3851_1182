package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.MissingResourceException;

/**
 * Represents a camera in 3D space.
 *
 * <p>The camera is responsible for generating rays through a pixel grid that
 * represents the view plane. It uses the Builder pattern for construction and
 * supports configuring the view plane, resolution, and ray tracer.</p>
 */
public class Camera implements Cloneable {

    /** Camera position in world space. */
    private Point _p0;

    /** Camera right vector. */
    private Vector _vRight;

    /** Camera up vector. */
    private Vector _vUp;

    /** Camera forward/viewing vector. */
    private Vector _vTo;

    /** View plane width. */
    private double _width;

    /** View plane height. */
    private double _height;

    /** Distance from the camera to the view plane. */
    private double _distance;

    /** Number of pixels in the X direction. */
    private int _nX = 1;

    /** Number of pixels in the Y direction. */
    private int _nY = 1;

    /** Center point of the view plane. */
    private Point _pCenter;

    /** Pixel width on the view plane. */
    private double _rx;

    /** Pixel height on the view plane. */
    private double _ry;

    /** Image writer used for the output image. */
    private ImageWriter _imageWriter;

    /** Ray tracer used to compute pixel colors. */
    private RayTracerBase _rayTracer;

    /**
     * Private constructor to enforce use of the Builder.
     */
    private Camera() {
    }

    /**
     * Returns a new builder for configuring and creating a camera.
     *
     * @return a new camera builder
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Returns the horizontal resolution of the view plane.
     *
     * @return the number of pixels in the X direction
     */
    public int getNx() {
        return _nX;
    }

    /**
     * Returns the vertical resolution of the view plane.
     *
     * @return the number of pixels in the Y direction
     */
    public int getNy() {
        return _nY;
    }

    /**
     * Constructs a ray through the center of the specified pixel.
     *
     * @param xIndex the pixel column index
     * @param yIndex the pixel row index
     * @return the constructed ray
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
     * Renders the image by tracing a ray through every pixel.
     *
     * @return this camera instance for method chaining
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
     * Traces and writes the color for a single pixel.
     *
     * @param xIndex the pixel column index
     * @param yIndex the pixel row index
     */
    private void castRay(int xIndex, int yIndex) {
        Ray ray = constructRay(xIndex, yIndex);
        Color color = _rayTracer.traceRay(ray);
        _imageWriter.writePixel(xIndex, yIndex, color);
    }

    /**
     * Draws a colored grid over the rendered image.
     *
     * @param interval the spacing between grid lines
     * @param color    the grid line color
     * @return this camera instance for method chaining
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
     * Writes the rendered image to disk.
     *
     * @param imageName the output image file name
     */
    public void writeToImage(String imageName) {
        _imageWriter.writeToImage(imageName);
    }

    /**
     * Creates a shallow clone of this camera.
     *
     * @return a cloned camera instance
     * @throws CloneNotSupportedException if cloning is not supported
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Static inner class implementing the Builder design pattern for {@link Camera}.
     */
    public static class Builder {

        /** Camera instance being configured. */
        private final Camera _camera;

        /** Optional target point used when direction is specified by target. */
        private Point _target = null;

        /** Explicit up vector supplied by the user. */
        private Vector _explicitUp = null;

        /** Explicit to vector supplied by the user. */
        private Vector _explicitTo = null;

        /** Rotation angle around the viewing direction, in degrees. */
        private double _rotationAngleDegrees = 0;

        /**
         * Creates a new builder with a fresh camera instance.
         */
        public Builder() {
            _camera = new Camera();
        }

        /**
         * Sets the camera location.
         *
         * @param location the camera location in world space
         * @return this builder for chaining
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera direction using explicit to/up vectors.
         *
         * @param to the forward direction vector
         * @param up the up vector
         * @return this builder for chaining
         */
        public Builder setDirection(Vector to, Vector up) {
            _explicitTo = to;
            _explicitUp = up;
            return this;
        }

        /**
         * Sets the camera direction using a target point and up vector.
         *
         * @param target the point the camera should look at
         * @param up     the up vector
         * @return this builder for chaining
         */
        public Builder setDirection(Point target, Vector up) {
            _target = target;
            _explicitUp = up;
            return this;
        }

        /**
         * Sets the camera direction using a target point and a default up vector.
         *
         * @param target the point the camera should look at
         * @return this builder for chaining
         */
        public Builder setDirection(Point target) {
            _target = target;
            return this;
        }

        /**
         * Sets the view plane size.
         *
         * @param width  the view plane width
         * @param height the view plane height
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
         * @param distance the view plane distance
         * @return this builder for chaining
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the view plane resolution.
         *
         * @param nX the number of pixels in the X direction
         * @param nY the number of pixels in the Y direction
         * @return this builder for chaining
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Sets the ray tracer implementation to use with the camera.
         *
         * @param scene the scene to render
         * @param type  the ray tracer type
         * @return this builder for chaining
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
         * Rotates the camera around its viewing direction by a given angle.
         *
         * @param angleDegrees the rotation angle in degrees (positive for clockwise)
         * @return the current Builder object for chaining
         */
        public Builder setRotation(double angleDegrees) {
            _rotationAngleDegrees = angleDegrees;
            return this;
        }

        /**
         * Builds the configured camera instance.
         *
         * @return the constructed camera
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            applyRotation(); // Apply any requested rotation after vectors are built
            checkViewPlane();

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
         * Validates and initializes the resolution-dependent fields.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution must be strictly positive.");
            }
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Validates the location and direction inputs and computes the camera basis.
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
         * Applies the rotation to the up and right vectors around the viewing axis.
         */
        private void applyRotation() {
            if (primitives.Util.isZero(_rotationAngleDegrees)) {
                return;
            }

            double angleRad = Math.toRadians(_rotationAngleDegrees);
            double cosTheta = primitives.Util.alignZero(Math.cos(angleRad));
            double sinTheta = primitives.Util.alignZero(Math.sin(angleRad));

            Vector newVUp = _camera._vUp;

            if (cosTheta != 0) {
                newVUp = newVUp.scale(cosTheta);
            } else {
                newVUp = null;
            }

            if (sinTheta != 0) {
                Vector rightComponent = _camera._vRight.scale(sinTheta);
                newVUp = (newVUp == null) ? rightComponent : newVUp.add(rightComponent);
            }

            _camera._vUp = newVUp.normalize();
            _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
        }

        /**
         * Validates the view plane dimensions and computes pixel sizes.
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