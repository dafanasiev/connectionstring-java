package io.github.dafanasiev.connectionstring;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectionStringParserTest {
    @Test
    void parsesOriginalGoFixture() {
        Map<String, String> got = ConnectionStringParser.parseConnectionString("""

                v="";

                a string=test;
                a number=987;a boolean=false;other value="some string=in quotes"

                """);

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("v", "");
        expected.put("a string", "test");
        expected.put("a number", "987");
        expected.put("a boolean", "false");
        expected.put("other value", "some string=in quotes");

        assertEquals(expected, got);
    }

    @Test
    void supportsEqualsAndSemicolonsInsideQuotedValues() {
        Map<String, String> got = ConnectionStringParser.parseConnectionString("a=one=two;b=\"x;y=z\";");
        assertEquals("one=two", got.get("a"));
        assertEquals("x;y=z", got.get("b"));
    }

    @Test
    void trimsKeysAndValues() {
        Map<String, String> got = ConnectionStringParser.parseConnectionString(" key = value ; quoted = \" value \" ");
        assertEquals("value", got.get("key"));
        assertEquals("value", got.get("quoted"));
    }

    @Test
    void rejectsMissingValue() {
        assertThrows(ConnectionStringParser.ParseException.class,
                () -> ConnectionStringParser.parseConnectionString("a="));
    }
}
