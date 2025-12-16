package at.technikum.mrp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Zentraler ObjectMapper für JSON (Jackson).
 * Wir konfigurieren hier auch LocalDateTime Support, damit createdAt sauber als string rauskommt.
 */
public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // Support für Java Time API (LocalDateTime, Instant, ...)
        MAPPER.registerModule(new JavaTimeModule());

        // Sonst würden Dates als "Timestamp Arrays" ausgegeben werden -> unlesbar im JSON
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
