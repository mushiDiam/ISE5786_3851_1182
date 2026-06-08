package parser;

import geometries.api.Geometry;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import java.util.List;
import java.util.Map;

/**
 * Builds a {@link Scene} domain object from a generic parsed tree
 * (the {@link Map}/{@link List} structure produced by {@link JsonParser}).
 *
 * <p><b>Single Responsibility:</b> generic tree → domain objects.
 * This class knows about {@link Scene}, geometries, lights and materials,
 * but it knows nothing about JSON <em>text</em> syntax — that is the job of
 * {@link JsonParser}. This separation satisfies SRP and keeps the parser
 * reusable for other formats (e.g. a future XML reader could feed the same
 * builder).</p>
 *
 * <h2>Supported scene structure</h2>
 * <pre>
 * {
 *   "scene": {
 *     "background-color": "r g b",
 *     "ambient-light": { "color": "r g b" },
 *     "geometries": [ { "type": "...", ... }, ... ],
 *     "lights":     [ { "type": "...", ... }, ... ]
 *   }
 * }
 * </pre>
 *
 * <p>Coordinates and colors are written as space-separated triples
 * (e.g. {@code "0 0 -100"}) to mirror the XML attribute convention.
 * Both background, ambient, geometries and lights are optional, so the same
 * builder serves a bare Stage-5 scene and a fully-lit later-stage scene.</p>
 */
public class SceneBuilder {

    /**
     * Constructs a new SceneBuilder for javadoc purposes.
     */
    public SceneBuilder() {
    }

    /**
     * Builds a complete {@link Scene} from a parsed JSON tree.
     *
     * @param sceneName the name to give the scene
     * @param root      the parsed JSON root object
     * @return the fully constructed scene
     */
    public Scene build(String sceneName, Map<String, Object> root) {
        // Allow either a wrapping "scene" object or a bare scene object
        Map<String, Object> node = asObject(root.containsKey("scene") ? root.get("scene") : root);

        Scene scene = new Scene(sceneName);

        if (node.containsKey("background-color"))
            scene.setBackground(parseColor(asString(node.get("background-color"))));

        if (node.containsKey("ambient-light")) {
            Map<String, Object> ambient = asObject(node.get("ambient-light"));
            scene.setAmbientLight(new AmbientLight(parseColor(asString(ambient.get("color")))));
        }

        if (node.containsKey("geometries"))
            for (Object g : asArray(node.get("geometries")))
                scene.geometries.add(parseGeometry(asObject(g)));

        if (node.containsKey("lights"))
            for (Object l : asArray(node.get("lights")))
                scene.lights.add(parseLight(asObject(l)));

        return scene;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Geometry mapping (extensible — add new types to the switch)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Converts a geometry node into a concrete {@link Geometry}.
     * Optional {@code emission} and {@code material} members are applied if present
     * (used from later stages; ignored when absent).
     *
     * @param node the geometry JSON object (must contain a {@code "type"} key)
     * @return the constructed geometry
     */
    private Geometry parseGeometry(Map<String, Object> node) {
        String type = asString(node.get("type"));
        Geometry geometry = switch (type) {
            case "sphere" -> new Sphere(parsePoint(asString(node.get("center"))),
                    parseDouble(node.get("radius")));
            case "triangle" -> new Triangle(parsePoint(asString(node.get("p0"))),
                    parsePoint(asString(node.get("p1"))),
                    parsePoint(asString(node.get("p2"))));
            case "plane" -> new Plane(parsePoint(asString(node.get("point"))),
                    parseVector(asString(node.get("normal"))));
            case "polygon" -> parsePolygon(node);
            default -> throw new IllegalArgumentException("Unknown geometry type: " + type);
        };

        if (node.containsKey("emission"))
            geometry.setEmission(parseColor(asString(node.get("emission"))));
        if (node.containsKey("material"))
            geometry.setMaterial(parseMaterial(asObject(node.get("material"))));

        return geometry;
    }

    /**
     * Builds a {@link Polygon} from a {@code "vertices"} array of point strings.
     *
     * @param node the polygon JSON object
     * @return the constructed polygon
     */
    private Polygon parsePolygon(Map<String, Object> node) {
        List<Object> verts = asArray(node.get("vertices"));
        Point[] points = new Point[verts.size()];
        for (int i = 0; i < points.length; i++)
            points[i] = parsePoint(asString(verts.get(i)));
        return new Polygon(points);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Material mapping (extensible — used by later stages)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Builds a {@link Material} from optional coefficient members.
     * Every key is optional; missing keys keep the {@link Material} default.
     *
     * @param node the material JSON object
     * @return the constructed material
     */
    private Material parseMaterial(Map<String, Object> node) {
        Material material = new Material();
        if (node.containsKey("kd")) material.setKD(parseDouble(node.get("kd")));
        if (node.containsKey("ks")) material.setKS(parseDouble(node.get("ks")));
        if (node.containsKey("shininess")) material.setShininess((int) parseDouble(node.get("shininess")));
        if (node.containsKey("kt")) material.setKT(parseDouble(node.get("kt")));
        if (node.containsKey("kr")) material.setKR(parseDouble(node.get("kr")));
        return material;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Light mapping (extensible — used by later stages)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Converts a light node into a concrete light source.
     * Attenuation ({@code kc}/{@code kl}/{@code kq}), {@code size} and
     * {@code narrow-beam} are optional where applicable.
     *
     * @param node the light JSON object (must contain a {@code "type"} key)
     * @return the constructed light source
     */
    private lighting.LightSource parseLight(Map<String, Object> node) {
        String type = asString(node.get("type"));
        Color color = parseColor(asString(node.get("color")));
        return switch (type) {
            case "directional" -> new DirectionalLight(color, parseVector(asString(node.get("direction"))));
            case "point" -> applyAttenuation(new PointLight(color, parsePoint(asString(node.get("position")))), node);
            case "spot" -> {
                SpotLight spot = new SpotLight(color,
                        parsePoint(asString(node.get("position"))),
                        parseVector(asString(node.get("direction"))));
                applyAttenuation(spot, node);
                if (node.containsKey("narrow-beam"))
                    spot.setNarrowBeam((int) parseDouble(node.get("narrow-beam")));
                yield spot;
            }
            default -> throw new IllegalArgumentException("Unknown light type: " + type);
        };
    }

    /**
     * Applies optional attenuation and size members to a {@link PointLight}
     * (also serves {@link SpotLight}, which extends it).
     *
     * @param light the light to configure
     * @param node  the light JSON object
     * @return the same light, for chaining
     */
    private PointLight applyAttenuation(PointLight light, Map<String, Object> node) {
        if (node.containsKey("kc")) light.setKc(parseDouble(node.get("kc")));
        if (node.containsKey("kl")) light.setKl(parseDouble(node.get("kl")));
        if (node.containsKey("kq")) light.setKq(parseDouble(node.get("kq")));
        if (node.containsKey("size")) light.setSize(parseDouble(node.get("size")));
        return light;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Primitive value helpers (DRY — single place per value kind)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Parses a space-separated triple into a {@link Color}.
     *
     * @param s a string of the form {@code "r g b"}
     * @return the color
     */
    private Color parseColor(String s) {
        double[] v = parseTriple(s);
        return new Color(v[0], v[1], v[2]);
    }

    /**
     * Parses a space-separated triple into a {@link Point}.
     *
     * @param s a string of the form {@code "x y z"}
     * @return the point
     */
    private Point parsePoint(String s) {
        double[] v = parseTriple(s);
        return new Point(v[0], v[1], v[2]);
    }

    /**
     * Parses a space-separated triple into a {@link Vector}.
     *
     * @param s a string of the form {@code "x y z"}
     * @return the vector
     */
    private Vector parseVector(String s) {
        double[] v = parseTriple(s);
        return new Vector(v[0], v[1], v[2]);
    }

    /**
     * Splits a space-separated string into exactly three doubles.
     *
     * @param s the source string
     * @return an array of three doubles
     * @throws IllegalArgumentException if the string does not hold three numbers
     */
    private double[] parseTriple(String s) {
        String[] parts = s.trim().split("\\s+");
        if (parts.length != 3)
            throw new IllegalArgumentException(
                    "Expected three space-separated numbers but got: \"" + s + "\"");
        return new double[]{
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2])
        };
    }

    /**
     * Reads a numeric value that may appear in JSON either as a bare number
     * or as a quoted string.
     *
     * @param value the parsed JSON value
     * @return the numeric value
     */
    private double parseDouble(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        return Double.parseDouble(value.toString().trim());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Type-cast helpers (clear errors instead of raw ClassCastException)
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Casts a parsed value to a JSON object map.
     *
     * @param o the value
     * @return the value as a map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asObject(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        throw new IllegalArgumentException("Expected a JSON object but got: " + o);
    }

    /**
     * Casts a parsed value to a JSON array list.
     *
     * @param o the value
     * @return the value as a list
     */
    @SuppressWarnings("unchecked")
    private List<Object> asArray(Object o) {
        if (o instanceof List) return (List<Object>) o;
        throw new IllegalArgumentException("Expected a JSON array but got: " + o);
    }

    /**
     * Casts a parsed value to a string (numbers are accepted and stringified).
     *
     * @param o the value
     * @return the value as a string
     */
    private String asString(Object o) {
        if (o instanceof String s) return s;
        if (o instanceof Number n) return n.toString();
        throw new IllegalArgumentException("Expected a JSON string but got: " + o);
    }
}