package parser;

import scene.Scene;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Public entry point ("facade") for loading a {@link Scene} from a file.
 *
 * <p><b>Single Responsibility:</b> orchestration and file I/O. It locates the
 * file, reads its text, hands the text to {@link JsonParser} (syntax) and the
 * resulting tree to {@link SceneBuilder} (domain mapping), then returns the
 * finished {@link Scene}.</p>
 *
 * <p>This is the only class the rest of the system (e.g. the render tests)
 * needs to know about. Parsing logic lives entirely inside this package, so no
 * parsing code ever appears in {@code RenderTests}, {@code Camera} or
 * {@code RayTracer} — satisfying the required architectural separation.</p>
 *
 * <p>Scene files are looked up under the {@value #SCENES_DIR} folder relative to
 * the working directory, so a call with {@code "basicRenderTestTwoColors"}
 * resolves to {@code scenes/basicRenderTestTwoColors.json}.</p>
 */
public final class SceneLoader {

    /**
     * Folder (relative to the working directory) that holds scene files.
     */
    private static final String SCENES_DIR = "scenes/";

    /**
     * File extension used for JSON scene files.
     */
    private static final String JSON_EXTENSION = ".json";

    /**
     * Non-instantiable utility class.
     */
    private SceneLoader() {
    }

    /**
     * Loads a {@link Scene} from a JSON file.
     *
     * @param name the base file name without folder or extension
     *             (e.g. {@code "basicRenderTestTwoColors"})
     * @return the fully constructed scene
     * @throws UncheckedIOException     if the file cannot be read
     * @throws IllegalArgumentException if the file content is not a valid scene
     */
    public static Scene loadFromJson(String name) {
        Path path = Path.of(SCENES_DIR + name + JSON_EXTENSION);
        String json;
        try {
            json = Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Could not read scene file: " + path.toAbsolutePath(), e);
        }

        Object tree = JsonParser.parse(json);
        if (!(tree instanceof Map))
            throw new IllegalArgumentException("Root of a JSON scene must be an object");

        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) tree;
        return new SceneBuilder().build("Scene from " + name + JSON_EXTENSION, root);
    }
}