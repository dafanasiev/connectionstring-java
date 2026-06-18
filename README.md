# connectionstring-java

Java port of `github.com/dafanasiev/connectionstring-go`.

## Usage

```java
Map<String, String> values = ConnectionStringParser.parseConnectionString(
    "host=localhost;password=\"some string=in quotes\";"
);
```

The parser accepts semicolon-separated `key=value` pairs, trims surrounding whitespace from keys and values, supports quoted values, and allows `=` and `;` inside quoted values.
