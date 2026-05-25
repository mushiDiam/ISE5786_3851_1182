package primitives;

/**
 * Represents the material properties of a geometry.
 * <p>
 * A material defines how a surface reacts to light, including ambient,
 * diffuse, and specular reflection coefficients, as well as shininess.
 * </p>
 */
public class Material {

    /**
     * Constructs a material with default reflection properties.
     */
    public Material() {
    }

    /**
     * Ambient light attenuation factor.
     */
    public Double3 kA = Double3.ONE;

    /**
     * Diffuse reflection coefficient (kD). Default is zero.
     */
    public Double3 kD = Double3.ZERO;

    /**
     * Specular reflection coefficient (kS). Default is zero.
     */
    public Double3 kS = Double3.ZERO;

    /**
     * Shininess exponent (nShininess). Default is 0.
     */
    public int nShininess = 0;

    /**
     * Transparency coefficient.
     */
    public Double3 kT = Double3.ZERO;

    /**
     * Reflection coefficient.
     */
    public Double3 kR = Double3.ZERO;

    /**
     * Sets the transparency coefficient using a scalar value.
     *
     * @param kT transparency coefficient
     * @return the current Material object for chaining
     */
    public Material setKT(double kT) {
        this.kT = new Double3(kT);
        return this;
    }

    /**
     * Sets the reflection coefficient using a scalar value.
     *
     * @param kR reflection coefficient
     * @return the current Material object for chaining
     */
    public Material setKR(double kR) {
        this.kR = new Double3(kR);
        return this;
    }

    /**
     * Sets kT with a Double3 value.
     *
     * @param kT transmission coefficient
     * @return the current Material object for chaining
     */
    public Material setKT(Double3 kT) {
        this.kT = kT;
        return this;
    }
    
    /**
     * Sets kR with a Double3 value.
     *
     * @param kR reflection coefficient
     * @return the current Material object for chaining
     */
    public Material setKR(Double3 kR) {
        this.kR = kR;
        return this;
    }

    /**
     * Sets kA with a Double3 value.
     *
     * @param kA ambient light attenuation factor
     * @return the current Material object for chaining
     */
    public Material setKa(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets kA with a double value (applied to all channels).
     *
     * @param kA ambient light attenuation factor
     * @return the current Material object for chaining
     */
    public Material setKa(double kA) {
        this.kA = new Double3(kA);
        return this;
    }

    /**
     * Sets kD with a Double3 value.
     *
     * @param kD diffuse reflection coefficient
     * @return the current Material object for chaining
     */
    public Material setKD(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /**
     * Sets kD with a double value.
     *
     * @param kD diffuse reflection coefficient
     * @return the current Material object for chaining
     */
    public Material setKD(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Sets kS with a Double3 value.
     *
     * @param kS specular reflection coefficient
     * @return the current Material object for chaining
     */
    public Material setKS(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /**
     * Sets kS with a double value.
     *
     * @param kS specular reflection coefficient
     * @return the current Material object for chaining
     */
    public Material setKS(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Sets the shininess exponent.
     *
     * @param nShininess shininess exponent
     * @return the current Material object for chaining
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }
}