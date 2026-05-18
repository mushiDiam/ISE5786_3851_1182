package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;

/**
 * Unit tests for the ImageWriter class.
 */
class ImageWriterTests {

    /**
     * Default constructor for ImageWriterTests.
     */
    ImageWriterTests() {
    }

    /**
     * Test method for writing a basic image with a grid.
     */
    @Test
    void testImageWriter() {
        // Define constants to avoid magic numbers
        final int nX = 800;
        final int nY = 500;
        final int step = 50;

        // Define two contrasting colors for the background and the grid
        final Color backgroundColor = new Color(255, 255, 0); // Yellow
        final Color gridColor = new Color(255, 0, 0);       // Red

        // Create the ImageWriter object with resolution only
        ImageWriter imageWriter = new ImageWriter(nX, nY);

        // Nested loops to color all pixels
        for (int i = 0; i < nY; i++) {
            for (int j = 0; j < nX; j++) {
                // Determine if the current pixel is on a grid line (every 50 pixels)
                // Using ternary operator to avoid code duplication
                Color pixelColor = (i % step == 0 || j % step == 0) ? gridColor : backgroundColor;

                // Write the color to the pixel (j is the column/X, i is the row/Y)
                imageWriter.writePixel(j, i, pixelColor);
            }
        }

        // Save the image to the disk and provide the file name
        imageWriter.writeToImage("testYellowRedGrid");
    }
}