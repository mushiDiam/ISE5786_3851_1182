package geometries.impl;

import primitives.Ray;

public class Cylinder extends Tube{
    double height;
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this.height = height;
    }
}
