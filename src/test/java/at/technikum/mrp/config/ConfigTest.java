package at.technikum.mrp.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void testServerPort() {
        int port = ServerConfig.getPort();
        assertTrue(port > 0 && port <= 65535, "Port muss zwischen 1 und 65535 sein");
    }

    @Test
    public void testDatabaseConfig() {
        String url = DatabaseConfig.getProperty("db.url");
        assertNotNull(url, "DB URL sollte in Konfiguration sein");
        assertTrue(url.contains("postgresql"), "DB URL sollte PostgreSQL enthalten");
    }

    @Test
    public void testBcryptRounds() {
        int rounds = ServerConfig.getBcryptRounds();
        assertTrue(rounds >= 10, "BCrypt sollte mindestens 10 Runden haben für Sicherheit");
    }
}