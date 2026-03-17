package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Test
    void testSubtract1() {
        Point p1 = new Point(1,2,3);
        Point p2 = new Point(-1,2,4);
        assertEquals(new Vector(-2,0,1),p2.subtract(p1),"Not equal");
    }

    @Test
    void testSubtract2() {
        Point p1 = new Point(1,2,3);
        assertThrows(IllegalArgumentException.class,()->p1.subtract(p1),"Should be invalid");
    }

    @Test
    void testAdd1() {
        Point p = new Point(1,2,3);
        Vector v  = new Vector(-1,-2,-3);
        assertEquals(new Point(0,0,0),p.add(v),"Not equal");
    }

    @Test
    void testAdd2() {
        Point p = new Point(1,2,3);
        Vector v  = new Vector(-1,-2,3);
        assertEquals(new Point(0,0,6),p.add(v),"Not equal");
    }

    @Test
    void testDistanceSquared1() {
        Point p1 = new Point(1,1,0);
        Point p2 = new Point(0,1,1);
        assertEquals(2,p1.distanceSquared(p2),"Not equal");
    }

    @Test
    void testDistanceSquared2() {
        Point p1 = new Point(1,1,0);
        assertEquals(0,p1.distanceSquared(p1),"Not equal");
    }

    @Test
    void testDistanceSymmetryAndNegative() {
        Point p1 = new Point(-5, -3, -1);
        Point p2 = new Point(2, 4, 6);

        double dist1 = p1.distance(p2);
        double dist2 = p2.distance(p1);

        assertEquals(dist1, dist2, "The distance should be symmetric");
    }


    @Test
    void testDistance() {
        Point p1 = new Point(1,1,0);
        Point p2 = new Point(0,1,1);
        assertEquals(Math.sqrt(2),p1.distance(p2),"Not equal");
    }
}