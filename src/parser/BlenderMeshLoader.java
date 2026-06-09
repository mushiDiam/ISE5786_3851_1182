package parser;

import geometries.impl.Triangle;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import scene.Scene;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads a Blender mesh export (the {@code blender_export.json} format with
 * top-level {@code "materials"} and {@code "objects"}) into a {@link Scene} of
 * {@link Triangle}s, ready to render with the BVH acceleration.
 *
 * <p>This is a separate concern from the Stage-5 {@link SceneLoader}: the
 * Blender format is a vertex/polygon mesh, not the simple project scene schema,
 * so it gets its own loader. It still lives in the {@code parser} package and
 * reuses {@link JsonParser}, keeping all parsing out of the renderer and tests.</p>
 *
 * <h3>Coloring</h3>
 * <p>The export's {@code base_color} values are almost all the Blender default
 * grey (the real colors lived in textures that were not exported). Instead, each
 * surface is colored from its <b>material name</b> (e.g. {@code ..._roof_stone_...}
 * → slate, {@code ..._base_wood_...} → brown). The color is applied as a
 * <i>colored diffuse coefficient</i> so the surface is properly shaded by the
 * lights, plus a low matching emission so shadowed areas keep their hue.</p>
 */
public final class BlenderMeshLoader {

    /**
     * World scale applied to the (tiny) Blender coordinates.
     */
    private static final double SCALE = 40.0;

    /**
     * Fraction of the albedo emitted as a base tone, so shadows are not black.
     */
    private static final double EMISSION_BASE = 32.0;

    /**
     * Cache of resolved surfaces, keyed by material name.
     */
    private static final Map<String, Surface> SURFACES = new HashMap<>();

    /**
     * A resolved surface appearance: the shaded material plus the dim base
     * emission that carries its color into shadow.
     *
     * @param material the colored, shaded material
     * @param emission the low base-tone emission
     */
    private record Surface(Material material, Color emission) {
    }

    /**
     * Non-instantiable utility class.
     */
    private BlenderMeshLoader() {
    }

    /**
     * Loads the Blender export at the given path into a renderable scene.
     *
     * @param path file path of the Blender JSON export
     * @return a scene populated with the colored mesh triangles
     */
    public static Scene loadShrine(String path) {
        String json;
        try {
            json = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read mesh file: " + path, e);
        }

        Map<String, Object> root = asObject(JsonParser.parse(json));

        Scene scene = new Scene("Blender Shrine");
        long kept = 0, skipped = 0;

        for (Object objRaw : asArray(root.get("objects"))) {
            Map<String, Object> obj = asObject(objRaw);
            double[] loc = triple(asArray(obj.get("location")));
            Surface surface = surfaceFor(materialName(obj));

            // Pre-transform every vertex of this object exactly once
            List<Object> vlist = asArray(obj.get("vertices"));
            Point[] pts = new Point[vlist.size()];
            for (int i = 0; i < pts.length; i++) {
                double[] v = triple(asArray(vlist.get(i)));
                double wx = v[0] + loc[0];
                double wy = v[1] + loc[1];
                double wz = v[2] + loc[2];
                // Blender (x, y, z) Z-up  ->  project (x, z, y) Y-up, scaled
                pts[i] = new Point(wx * SCALE, wz * SCALE, wy * SCALE);
            }

            // Fan-triangulate each polygon: (v0, vi, vi+1)
            for (Object polyRaw : asArray(obj.get("polygons"))) {
                List<Object> poly = asArray(polyRaw);
                int n = poly.size();
                int i0 = ((Number) poly.get(0)).intValue();
                for (int i = 1; i < n - 1; i++) {
                    int i1 = ((Number) poly.get(i)).intValue();
                    int i2 = ((Number) poly.get(i + 1)).intValue();
                    try {
                        scene.geometries.add(new Triangle(pts[i0], pts[i1], pts[i2])
                                .setEmission(surface.emission())
                                .setMaterial(surface.material()));
                        kept++;
                    } catch (IllegalArgumentException degenerate) {
                        skipped++; // collinear / zero-area face — safe to drop
                    }
                }
            }
        }

        System.out.printf("[Shrine] loaded %d triangles (skipped %d degenerate)%n", kept, skipped);
        return scene;
    }

    /**
     * Reads an object's first material name.
     *
     * @param obj the object node
     * @return the material name, or empty string if none
     */
    private static String materialName(Map<String, Object> obj) {
        Object mats = obj.get("materials");
        if (mats instanceof List<?> list && !list.isEmpty())
            return list.get(0).toString();
        return "";
    }

    /**
     * Returns the cached surface for a material name, building it on first use.
     *
     * @param name the material name
     * @return the resolved surface appearance
     */
    private static Surface surfaceFor(String name) {
        return SURFACES.computeIfAbsent(name, BlenderMeshLoader::buildSurface);
    }

    /**
     * Builds a colored, shaded surface from keywords in the material name.
     * More specific keywords are tested before generic ones (e.g. {@code dark_wood}
     * before {@code wood}, {@code roof_stone}/{@code rock} before {@code stone}).
     *
     * @param name the material name
     * @return the surface appearance for that name
     */
    private static Surface buildSurface(String name) {
        String n = name.toLowerCase();

        double r, g, b, ks;
        int shininess;

        if (n.contains("chrome")) {
            r = 0.72;
            g = 0.74;
            b = 0.80;
            ks = 0.60;
            shininess = 250;
        } else if (n.contains("paper")) {
            r = 0.86;
            g = 0.80;
            b = 0.62;
            ks = 0.05;
            shininess = 20;
        } else if (n.contains("holder")) {
            r = 0.16;
            g = 0.14;
            b = 0.13;
            ks = 0.35;
            shininess = 80;
        } else if (n.contains("straw") || n.contains("tamati")) {
            r = 0.68;
            g = 0.57;
            b = 0.36;
            ks = 0.05;
            shininess = 20;
        } else if (n.contains("dark_wood")) {
            r = 0.26;
            g = 0.17;
            b = 0.10;
            ks = 0.10;
            shininess = 30;
        } else if (n.contains("wood")) {
            r = 0.50;
            g = 0.33;
            b = 0.18;
            ks = 0.10;
            shininess = 30;
        } else if (n.contains("roof_stone")) {
            r = 0.34;
            g = 0.38;
            b = 0.45;
            ks = 0.18;
            shininess = 50;
        } else if (n.contains("rock")) {
            r = 0.46;
            g = 0.43;
            b = 0.38;
            ks = 0.10;
            shininess = 30;
        } else if (n.contains("stone")) {
            r = 0.46;
            g = 0.46;
            b = 0.48;
            ks = 0.12;
            shininess = 40;
        } else {
            r = 0.55;
            g = 0.55;
            b = 0.55;
            ks = 0.10;
            shininess = 30;
        }

        Material material = new Material()
                .setKD(new Double3(r, g, b))   // colored diffuse → shaded color
                .setKS(ks)
                .setShininess(shininess);
        Color emission = new Color(r * EMISSION_BASE, g * EMISSION_BASE, b * EMISSION_BASE);
        return new Surface(material, emission);
    }

    /**
     * Reads the first three numbers of a JSON array as doubles.
     *
     * @param list a JSON array of at least three numbers
     * @return the three values
     */
    private static double[] triple(List<Object> list) {
        return new double[]{
                ((Number) list.get(0)).doubleValue(),
                ((Number) list.get(1)).doubleValue(),
                ((Number) list.get(2)).doubleValue()};
    }

    /**
     * Casts a parsed value to a JSON object.
     *
     * @param o the value
     * @return the value as a map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object o) {
        return (Map<String, Object>) o;
    }

    /**
     * Casts a parsed value to a JSON array.
     *
     * @param o the value
     * @return the value as a list
     */
    @SuppressWarnings("unchecked")
    private static List<Object> asArray(Object o) {
        return (List<Object>) o;
    }
}