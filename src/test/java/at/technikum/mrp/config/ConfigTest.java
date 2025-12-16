package at.technikum.mrp.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Konfiguration.
 * - Port in gültigem Bereich
 * - DB URL vorhanden und plausibel
 * - BCrypt Runden nicht zu niedrig
 */
public class ConfigTest {

    @Test
    public void testServerPort() {
        int port = ServerConfig.getPort();
        // Port muss im gültigen Bereich liegen (0 ist nicht erlaubt für echten Serverbetrieb)
        assertTrue(port > 0 && port <= 65535, "Port muss zwischen 1 und 65535 sein");
    }

    @Test
    public void testDatabaseConfig() {
        String url = DatabaseConfig.getProperty("db.url");
        assertNotNull(url, "DB URL sollte in Konfiguration sein");
        // einfacher Plausibilitätscheck: bei uns sollte es PostgreSQL sein
        assertTrue(url.contains("postgresql"), "DB URL sollte PostgreSQL enthalten");
    }

    @Test
    public void testBcryptRounds() {
        int rounds = ServerConfig.getBcryptRounds();
        // unter 10 wäre zu schwach (für ein Projekt als Minimum ok)
        assertTrue(rounds >= 10, "BCrypt sollte mindestens 10 Runden haben für Sicherheit");
    }
}
