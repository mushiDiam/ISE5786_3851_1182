package primitives;

public class Ray {
    private final Point _head;
    private final Vector _direction;

    public Ray(Point head, Vector direction) {
        _head = head;
        _direction = direction.normalize();
    }
    public Vector direction(){
        return _direction;
    }
}
