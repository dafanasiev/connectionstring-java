package com.github.dafanasiev.connectionstring;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parser for semicolon-separated connection strings such as:
 * {@code key=value; other key="quoted value=with equals";}
 *
 * <p>This is a Java port of github.com/dafanasiev/connectionstring-go.</p>
 */
public final class ConnectionStringParser {
    private ConnectionStringParser() {
    }

    /**
     * Parses a connection string into key/value pairs.
     *
     * <p>Grammar mirrored from the Go PEG parser:</p>
     * <pre>
     * Input  <- Pairs EOF
     * Pairs  <- Pair (";" Pair)* ";"?
     * Pair   <- Key "=" Value
     * Key    <- [^=;]+
     * Value  <- Quoted / Bare
     * Quoted <- '"' ((! '"') .)* '"'
     * Bare   <- [^;]+
     * </pre>
     *
     * @param connectionString input string; leading and trailing whitespace is ignored
     * @return insertion-ordered map of parsed values; later duplicate keys replace earlier values
     * @throws ParseException if the input does not match the grammar
     */
    public static Map<String, String> parseConnectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "connectionString");
        Parser parser = new Parser(connectionString.trim());
        return parser.parseInput();
    }

    public static final class ParseException extends IllegalArgumentException {
        private final int offset;

        private ParseException(String message, int offset) {
            super(message + " at offset " + offset);
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
    }

    private static final class Parser {
        private final String input;
        private int pos;

        private Parser(String input) {
            this.input = input;
        }

        private Map<String, String> parseInput() {
            Map<String, String> result = parsePairs();
            if (!isEnd()) {
                throw error("unexpected input");
            }
            return result;
        }

        private Map<String, String> parsePairs() {
            Map<String, String> result = new LinkedHashMap<>();
            Pair first = parsePair();
            result.put(first.key, first.value);

            while (peek(';')) {
                int semicolon = pos;
                pos++;
                if (isEnd()) {
                    return result; // optional trailing semicolon
                }
                try {
                    Pair pair = parsePair();
                    result.put(pair.key, pair.value);
                } catch (ParseException ex) {
                    // Preserve a helpful position near the separator that started this pair.
                    throw new ParseException("expected pair after ';'", semicolon);
                }
            }
            return result;
        }

        private Pair parsePair() {
            String key = parseKey();
            if (!peek('=')) {
                throw error("expected '='");
            }
            pos++;
            String value = parseValue();
            return new Pair(key, value);
        }

        private String parseKey() {
            int start = pos;
            while (!isEnd()) {
                char c = input.charAt(pos);
                if (c == '=' || c == ';') {
                    break;
                }
                pos++;
            }
            if (pos == start) {
                throw error("expected key");
            }
            return input.substring(start, pos).trim();
        }

        private String parseValue() {
            skipWhitespace();
            if (peek('"')) {
                return parseQuoted();
            }
            return parseBare();
        }

        private void skipWhitespace() {
            while (!isEnd() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private String parseQuoted() {
            int start = pos;
            pos++; // opening quote
            while (!isEnd() && input.charAt(pos) != '"') {
                pos++;
            }
            if (isEnd()) {
                throw new ParseException("unterminated quoted value", start);
            }
            int endExclusive = pos;
            pos++; // closing quote
            return input.substring(start + 1, endExclusive).trim();
        }

        private String parseBare() {
            int start = pos;
            while (!isEnd() && input.charAt(pos) != ';') {
                pos++;
            }
            if (pos == start) {
                throw error("expected value");
            }
            return input.substring(start, pos).trim();
        }

        private boolean peek(char expected) {
            return !isEnd() && input.charAt(pos) == expected;
        }

        private boolean isEnd() {
            return pos >= input.length();
        }

        private ParseException error(String message) {
            return new ParseException(message, pos);
        }
    }

    private record Pair(String key, String value) {
    }
}
