package renderer;

import geometries.api.Intersectable.GeoPoint;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import java.util.List;

import static primitives.Util.alignZero;

/**
 * A simple ray tracer implementing the Phong reflection model.
 */
class SimpleRayTracer extends RayTracerBase {

    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    @Override
    Color traceRay(Ray ray) {
        List<GeoPoint> intersections = _scene.geometries.findGeoIntersections(ray);
        if (intersections == null || intersections.isEmpty())
            return _scene.background;

        GeoPoint closestPoint = ray.findClosestGeoPoint(intersections);

        if (!preprocessIntersection(closestPoint, ray.direction())) {
            return _scene.ambientLight.getIntensity().scale(closestPoint.geometry.getMaterial().kA)
                    .add(closestPoint.geometry.getEmission());
        }

        return calcColor(closestPoint, ray);
    }

    private Color calcColor(GeoPoint gp, Ray ray) {
        return _scene.ambientLight.getIntensity()
                .scale(gp.geometry.getMaterial().kA)
                .add(calcLocalEffects(gp)); // Plus besoin de passer "ray", gp contient "v"
    }

    private Color calcLocalEffects(GeoPoint gp) {
        Color color = gp.geometry.getEmission();
        Material material = gp.geometry.getMaterial();

        for (LightSource lightSource : _scene.lights) {
            // Utilisation du cache pour la source de lumière
            if (preprocessLightSource(gp, lightSource)) {
                Vector l = lightSource.getL(gp.point);
                double nl = primitives.Util.alignZero(gp.n.dotProduct(l));

                Color iL = lightSource.getIntensity(gp.point);
                color = color.add(
                        iL.scale(calcDiffuse(material, nl)
                                .add(calcSpecular(material, gp.n, l, nl, gp.v)))
                );
            }
        }
        return color;
    }

    /**
     * Calculates the diffuse component: kD * |l · n|
     */
    private Double3 calcDiffuse(Material material, double ln) {
        return material.kD.scale(Math.abs(ln));
    }

    /**
     * Calculates the specular component: kS * max(0, -v · r)^nShininess
     */
    private Double3 calcSpecular(Material material, Vector n, Vector l, double ln, Vector v) {
        // Reflection vector: r = l - 2*(l·n)*n
        Vector r = l.subtract(n.scale(2.0 * ln));
        double vr = alignZero(-v.dotProduct(r));
        if (vr <= 0) return Double3.ZERO;
        return material.kS.scale(Math.pow(vr, material.nShininess));
    }
}