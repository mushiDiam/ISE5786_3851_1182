package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

public class Tube extends RadialGeometry{
    Ray axis;
    public Tube( double radius , Ray axis ) {
        super(radius);
        this.axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        return null;
        //TODO
    }
}
