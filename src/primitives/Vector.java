package primitives;

public class Vector extends Point {


    public Vector(Double3 _xyz) {
        super(_xyz);
    }

    public Vector(double x, double y, double z) {
        super(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector vector)) return false;
        return _xyz.equals(vector._xyz);
    }

    public Vector normalize() {
        double len = this.length();
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        double xx = _xyz._d1() * _xyz._d1();
        double yy = _xyz._d2() * _xyz._d2();
        double zz = _xyz._d3() * _xyz._d3();

        return xx + yy + zz;
    }

    public Vector crossProduct(Vector v) {
        return new Vector( //
                y() * v.z() - v.y() * z(), //
                z() * v.x() - v.z() * x(), //
                x() * v.y() - v.x() * y());
    }

}
