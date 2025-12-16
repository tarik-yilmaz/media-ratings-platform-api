package at.technikum.mrp.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser für Query-Strings (z.B. ?title=abc&sortBy=year).
 * Gibt eine Map zurück, damit Controller/Service einfacher damit arbeiten können.
 */
public class QueryUtil {

    /**
     * Parst die Query aus der URL (ohne '?') und decodiert URL-Encoding.
     * Beispiel: "title=Star%20Wars&sortBy=year"
     */
    public static Map<String, String> parse(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');

            // wenn kein "=" vorkommt, dann ist value einfach leer
            String key = (idx >= 0) ? pair.substring(0, idx) : pair;
            String val = (idx >= 0) ? pair.substring(idx + 1) : "";

            // URL decoding (z.B. %20 -> Leerzeichen)
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            val = URLDecoder.decode(val, StandardCharsets.UTF_8);

            map.put(key, val);
        }
        return map;
    }
}
