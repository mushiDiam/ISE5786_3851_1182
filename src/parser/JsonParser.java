package parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal, dependency-free JSON parser.
 *
 * <p>Converts a JSON text into a generic Java object tree:
 * <ul>
 *   <li>JSON object → {@link Map}&lt;String, Object&gt;</li>
 *   <li>JSON array  → {@link List}&lt;Object&gt;</li>
 *   <li>JSON string → {@link String}</li>
 *   <li>JSON number → {@link Double}</li>
 *   <li>true / false → {@link Boolean}</li>
 *   <li>null → {@code null}</li>
 * </ul>
 *
 * <p><b>Single Responsibility:</b> JSON <em>syntax</em> → generic tree.
 * This class knows nothing about scenes, geometries, or colors — it only
 * understands JSON grammar. Domain mapping is the job of {@link SceneBuilder}.</p>
 */
public final class JsonParser {

    /**
     * The full JSON text being parsed.
     */
    private final String _text;

    /**
     * Current read position within {@link #_text}.
     */
    private int _pos;

    /**
     * Private constructor — entry point is the static {@link #parse(String)}.
     *
     * @param text the JSON source text
     */
    private JsonParser(String text) {
        _text = text;
        _pos = 0;
    }

    /**
     * Parses a JSON document into a generic Java object tree.
     *
     * @param json the JSON source text
     * @return the parsed value (Map / List / String / Double / Boolean / null)
     * @throws IllegalArgumentException if the text is not valid JSON
     */
    public static Object parse(String json) {
        JsonParser parser = new JsonParser(json);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser._pos < parser._text.length())
            throw new IllegalArgumentException(
                    "Unexpected trailing characters at position " + parser._pos);
        return value;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Grammar
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Parses any JSON value at the current position.
     *
     * @return the parsed value
     */
    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNull();
            default -> parseNumber();
        };
    }

    /**
     * Parses a JSON object {@code { "key": value, ... }}.
     *
     * @return a map of the object's members (insertion-ordered)
     */
    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') {
            _pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            map.put(key, parseValue());
            skipWhitespace();
            char c = next();
            if (c == '}') break;
            if (c != ',') throw error("',' or '}'");
        }
        return map;
    }

    /**
     * Parses a JSON array {@code [ value, value, ... ]}.
     *
     * @return a list of the array's elements
     */
    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') {
            _pos++;
            return list;
        }
        while (true) {
            list.add(parseValue());
            skipWhitespace();
            char c = next();
            if (c == ']') break;
            if (c != ',') throw error("',' or ']'");
        }
        return list;
    }

    /**
     * Parses a JSON string literal, handling standard escape sequences.
     *
     * @return the unescaped string contents
     */
    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') break;
            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        sb.append((char) Integer.parseInt(_text.substring(_pos, _pos + 4), 16));
                        _pos += 4;
                    }
                    default -> throw error("a valid escape sequence");
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Parses a JSON number (integer or floating point) as a {@link Double}.
     *
     * @return the parsed number
     */
    private Double parseNumber() {
        int start = _pos;
        while (_pos < _text.length() && "+-0123456789.eE".indexOf(_text.charAt(_pos)) >= 0)
            _pos++;
        return Double.parseDouble(_text.substring(start, _pos));
    }

    /**
     * Parses a JSON boolean literal ({@code true} / {@code false}).
     *
     * @return the parsed boolean
     */
    private Boolean parseBoolean() {
        if (_text.startsWith("true", _pos)) {
            _pos += 4;
            return Boolean.TRUE;
        }
        if (_text.startsWith("false", _pos)) {
            _pos += 5;
            return Boolean.FALSE;
        }
        throw error("a boolean literal");
    }

    /**
     * Parses the JSON {@code null} literal.
     *
     * @return {@code null}
     */
    private Object parseNull() {
        if (_text.startsWith("null", _pos)) {
            _pos += 4;
            return null;
        }
        throw error("the null literal");
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Low-level helpers
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Returns the current character without consuming it.
     *
     * @return the character at the current position
     */
    private char peek() {
        if (_pos >= _text.length())
            throw error("more input");
        return _text.charAt(_pos);
    }

    /**
     * Consumes and returns the current character.
     *
     * @return the character at the current position
     */
    private char next() {
        if (_pos >= _text.length())
            throw error("more input");
        return _text.charAt(_pos++);
    }

    /**
     * Consumes the current character and verifies it equals {@code expected}.
     *
     * @param expected the required character
     */
    private void expect(char expected) {
        if (next() != expected)
            throw error("'" + expected + "'");
    }

    /**
     * Advances past any whitespace characters.
     */
    private void skipWhitespace() {
        while (_pos < _text.length() && Character.isWhitespace(_text.charAt(_pos)))
            _pos++;
    }

    /**
     * Builds a descriptive parse-error exception for the current position.
     *
     * @param expected a description of what was expected
     * @return the exception to throw
     */
    private IllegalArgumentException error(String expected) {
        return new IllegalArgumentException(
                "JSON parse error at position " + _pos + ": expected " + expected);
    }
}