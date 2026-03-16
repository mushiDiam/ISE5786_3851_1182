package primitives;

/**
 * Represents a 3D point in space.
 */
public class Point {
    /**
     * The coordinates of the point.
     */
    protected final Double3 _xyz;

    /**
     * The origin point (0,0,0).
     */
    public static final Point ZERO = new Point(Double3.ZERO);

    /**
     * The unit point (1,1,1).
     */
//    public static final Point ONE = new Point(Double3.ONE);

    /**
     * Constructor for Point using 3 coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Point(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Constructor for Point using a Double3 object.
     * @param xyz the Double3 coordinates
     */
    public Point(Double3 xyz) {
        this._xyz = xyz;
    }

    /**
     * Subtracts a point from this point, returning a vector from the other point to this point.
     * @param p the point to subtract
     *
     * @return a new Vector representing the difference
     */
    public Vector subtract(Point p) {
        return new Vector(_xyz.subtract(p._xyz));
    }

    /**
     * Adds a vector to this point, returning a new point.
     * @param v the vector to add
     *
     * @return a new Point representing the sum
     */
    public Point add(Vector v) {
        return new Point(_xyz.add(v._xyz));
    }

    /**
     * Computes the squared distance between this point and another point.
     * @param p the other point
     *
     * @return the squared distance
     */
    public double distanceSquared(Point p) {
        double deltaX = _xyz._d1() - p._xyz._d1();
        double deltaY = _xyz._d2() - p._xyz._d2();
        double deltaZ = _xyz._d3() - p._xyz._d3();

        return (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ);
    }

    /**
     * Computes the distance between this point and another point.
     * @param p the other point
     *
     * @return the distance
     */
    public double distance(Point p) {
        return Math.sqrt(distanceSquared(p));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return _xyz.equals(point._xyz);
    }

    @Override
    public int hashCode() {
        return _xyz.hashCode();
    }

    @Override
    public String toString() {
        return _xyz.toString();
    }
}