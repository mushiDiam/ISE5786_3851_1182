package primitives;

/**
 * Represents the material properties of a geometric body.
 * PDS (Plain Data Structure) - no constructor, public fields with chaining setters.
 */
public class Material {

    /** Ambient light attenuation factor. */
    public Double3 kA = Double3.ONE;

    /** Diffuse reflection coefficient (kD). Default is zero. */
    public Double3 kD = Double3.ZERO;

    /** Specular reflection coefficient (kS). Default is zero. */
    public Double3 kS = Double3.ZERO;

    /** Shininess exponent (nShininess). Default is 0. */
    public int nShininess = 0;

    /** Sets kA with a Double3 value. */
    public Material setKa(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /** Sets kA with a double value (applied to all channels). */
    public Material setKa(double kA) {
        this.kA = new Double3(kA);
        return this;
    }

    /** Sets kD with a Double3 value. */
    public Material setKD(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /** Sets kD with a double value. */
    public Material setKD(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /** Sets kS with a Double3 value. */
    public Material setKS(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /** Sets kS with a double value. */
    public Material setKS(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /** Sets the shininess exponent. */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }
}