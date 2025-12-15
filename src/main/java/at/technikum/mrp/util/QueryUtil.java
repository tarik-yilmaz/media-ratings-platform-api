package at.technikum.mrp.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QueryUtil {
    public static Map<String, String> parse(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key = (idx >= 0) ? pair.substring(0, idx) : pair;
            String val = (idx >= 0) ? pair.substring(idx + 1) : "";
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            val = URLDecoder.decode(val, StandardCharsets.UTF_8);
            map.put(key, val);
        }
        return map;
    }
}
