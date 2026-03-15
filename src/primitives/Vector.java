package primitives;

public final class Vector extends Point {

    public static final Vector AXIS_X = new Vector(1, 0, 0);
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }

    /**
     * Add two vectors
     *
     * @param v the vector to add
     * @return a new vector
     */
    public Vector add(Vector v) {
        return new Vector(_xyz.add(v._xyz));
    }

    /**
     * multiply the vector by a scalar
     *
     * @param scalar (must be double, not int)
     * @return a new vector
     */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /**
     * dot product between two vector
     *
     * @param u vector
     * @return a scalar
     */
    public double dotProduct(Vector u) {
        return _xyz._d1() * u._xyz._d1() + _xyz._d2() * u._xyz._d2() + _xyz._d3() * u._xyz._d3();
    }

    /**
     * cross product between two vectors
     *
     * @param v vector
     * @return a new vector
     */
    public Vector crossProduct(Vector v) {
        return new Vector(
                _xyz._d2() * v._xyz._d3() - v._xyz._d2() * _xyz._d3(),
                _xyz._d3() * v._xyz._d1() - v._xyz._d3() * _xyz._d1(),
                _xyz._d1() * v._xyz._d2() - v._xyz._d1() * _xyz._d2()
        );
    }

    /**
     * calc the length^2 of a vector
     *
     * @return length^2 as a double value
     */
    public double lengthSquared() {
        return dotProduct(this);
    }

    /**
     * calc the real length of a vector by using lengthSquared method
     *
     * @return length as a double value
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
}