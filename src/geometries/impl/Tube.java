package geometries.impl;

import primitives.Ray;

public class Tube extends RadialGeometry{
    Ray axis;
    public Tube( double radius , Ray axis ) {
        super(radius);
        this.axis = axis;
    }
}
