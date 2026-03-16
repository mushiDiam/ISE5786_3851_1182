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
    void testAdd() {
    }

    @Test
    void testDistanceSquared() {
    }

    @Test
    void testDistance() {
    }
}