package primitives;

/**
 * Represents the material properties of a geometric body.
 */
public class Material {
    /**
     * Ambient light attenuation factor.
     * Default is ONE so it doesn't block ambient light if not specified.
     */
    public Double3 kA = Double3.ONE;

    /**
     * Sets the ambient attenuation factor (kA).
     *
     * @param kA the ambient attenuation factor as a Double3
     * @return the current Material object (for method chaining)
     */
    public Material setKa(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient attenuation factor (kA) using a single double value.
     *
     * @param kA the ambient attenuation factor as a double (applied to all RGB channels)
     * @return the current Material object (for method chaining)
     */
    public Material setKa(double kA) {
        this.kA = new Double3(kA);
        return this;
    }
}