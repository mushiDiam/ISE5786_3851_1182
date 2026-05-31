package primitives;

/**
 * Represents a three-dimensional mathematical vector.
 * <p>
 * A {@code Vector} is defined by its coordinates in 3D space and supports common
 * vector operations such as addition, scaling, dot product, cross product,
 * length calculation, and normalization.
 * </p>
 * <p>
 * The zero vector {@code (0, 0, 0)} is not allowed, since it has no direction
 * and cannot be normalized.
 * </p>
 */
public final class Vector extends Point {

    /**
     * Unit vector in the positive X-axis direction.
     */
    public static final Vector AXIS_X = new Vector(1, 0, 0);

    /**
     * Unit vector in the positive Y-axis direction.
     */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);

    /**
     * Unit vector in the positive Z-axis direction.
     */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructor using Double3
     *
     * @param _xyz coordinates
     * @throws IllegalArgumentException if the vector is the null vector (0,0,0)
     */
    public Vector(Double3 _xyz) {
        super(_xyz);
        if (_xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("The null vector (0,0,0) is not allowed");
        }
    }

    /**
     * Constructor using 3 doubles
     *
     * @param x coordinate x
     * @param y coordinate y
     * @param z coordinate z
     * @throws IllegalArgumentException if the vector is the null vector (0,0,0)
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (this._xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("The null vector (0,0,0) is not allowed");
        }
    }

    /**
     * Adds another vector to this vector using component-wise addition.
     *
     * @param v the vector to add
     * @return a new {@code Vector} representing the sum of this vector and {@code v}
     */
    public Vector add(Vector v) {
        return new Vector(_xyz.add(v._xyz));
    }

    /**
     * Multiplies this vector by a scalar value, scaling its magnitude.
     *
     * @param scalar the scaling factor
     * @return a new {@code Vector} representing this vector scaled by {@code scalar}
     */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /**
     * Computes the dot product (scalar product) of this vector and another vector.
     * <p>
     * The dot product is calculated as: {@code x1*x2 + y1*y2 + z1*z2}
     * </p>
     *
     * @param u the vector to compute the dot product with
     * @return the dot product as a {@code double} value
     */
    public double dotProduct(Vector u) {
        return _xyz._d1() * u._xyz._d1() + _xyz._d2() * u._xyz._d2() + _xyz._d3() * u._xyz._d3();
    }

    /**
     * Computes the cross product (vector product) of this vector and another vector.
     * <p>
     * The cross product is a vector perpendicular to both input vectors, with
     * magnitude equal to the area of the parallelogram formed by the two vectors.
     * </p>
     *
     * @param v the vector to compute the cross product with
     * @return a new {@code Vector} representing the cross product of this vector and {@code v}
     * @throws IllegalArgumentException if the resulting vector is the zero vector
     *                                  (i.e., if this vector and {@code v} are parallel)
     */
    public Vector crossProduct(Vector v) {
        return new Vector(
                _xyz._d2() * v._xyz._d3() - v._xyz._d2() * _xyz._d3(),
                _xyz._d3() * v._xyz._d1() - v._xyz._d3() * _xyz._d1(),
                _xyz._d1() * v._xyz._d2() - v._xyz._d1() * _xyz._d2()
        );
    }

    /**
     * Calculates the squared length (squared magnitude) of this vector.
     * <p>
     * This method is more efficient than {@link #length()} when only the
     * squared length is needed, as it avoids the square root computation.
     * </p>
     *
     * @return the squared length of this vector as a {@code double} value
     */
    public double lengthSquared() {
        return dotProduct(this);
    }

    /**
     * Calculates the length (magnitude) of this vector.
     * <p>
     * The length is computed as the square root of {@link #lengthSquared()}.
     * </p>
     *
     * @return the length of this vector as a {@code double} value
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes the vector.
     *
     * @return a new normalized vector (length of 1)
     */
    public Vector normalize() {
        return new Vector(_xyz.scale(1.0 / length()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }
}