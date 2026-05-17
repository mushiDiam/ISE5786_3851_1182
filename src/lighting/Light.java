package lighting;

import primitives.Color;

/**
 * Abstract base class for all light sources.
 * Stores only the original light intensity.
 */
abstract class Light {

    /** The original intensity of the light source */
    protected final Color _intensity;

    /**
     * Constructor initializing the light intensity.
     * @param intensity the original color/intensity of the light
     */
    protected Light(Color intensity) {
        this._intensity = intensity;
    }

    /**
     * Returns the original intensity of the light (getter - not point-dependent).
     * @return the light's original intensity
     */
    public Color getIntensity() {
        return _intensity;
    }
}