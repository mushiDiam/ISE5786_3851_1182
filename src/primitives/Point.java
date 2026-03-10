package primitives;

import java.util.Objects;

public class Point {
    protected final Double3 _xyz;
    public static Double3 ZERO = new Double3(0, 0, 0);
//    public static Double3 ONE = new Double3(1, 1, 1);

    public Point(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    public Point(Double3 _xyz) {
        this._xyz = _xyz;
    }



    public Vector subtract(Point p) {
        return new Vector(_xyz.subtract(p._xyz));
    }

    public Point add(Vector v) {
        return new Point(_xyz.add(v._xyz));
    }

    public double distanceSquared(Point p) {
        double deltaX = _xyz._d1() - p._xyz._d1();
        double deltaY = _xyz._d2() - p._xyz._d2();
        double deltaZ = _xyz._d3() - p._xyz._d3();


        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public double distance(Point p) {
        return Math.sqrt(distanceSquared(p));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;
        return _xyz.equals(point._xyz);
    }

    @Override
    public int hashCode() {
        return _xyz.hashCode();
    }

    @Override
    public String toString() {
        return "" + _xyz;
    }
}
