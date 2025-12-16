package at.technikum.mrp.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lädt die DB-Konfiguration aus application.properties und stellt eine JDBC-Verbindung bereit.
 * Wird von den Repositories verwendet, damit wir nicht überall DB-URL/User/Passwort hardcoden.
 */
public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        // Wird einmal beim Klassenladen ausgeführt: properties Datei einlesen
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                // Wenn die Datei fehlt, ist später keine DB-Verbindung möglich
                System.err.println("Fehler: application.properties nicht gefunden!");
            } else {
                properties.load(input);
                System.out.println("✓ Datenbank-Konfiguration geladen");
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Konfiguration: " + e.getMessage());
        }
    }

    /**
     * Baut und liefert eine neue DB-Verbindung (JDBC Connection).
     * Achtung: Der Aufrufer (Repository) nutzt try-with-resources, damit die Connection sauber geschlossen wird.
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        // Ohne diese Werte kann JDBC keine Verbindung aufbauen
        if (url == null || user == null || password == null) {
            throw new SQLException("Datenbank-Konfiguration unvollständig");
        }

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Kleiner Startup-Check: Verbindet kurz zur DB, um zu sehen ob Docker/DB erreichbar ist.
     * (Für Debugging), um zu sehen, ob die Verbindung grundsätzlich funktioniert.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✓ Datenbank-Verbindung OK");
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Datenbank-Verbindung fehlgeschlagen: " + e.getMessage());
            System.err.println("  Stelle sicher, dass Docker läuft: docker-compose up -d");
            return false;
        }
    }

    /**
     * Liest einen Wert aus der Konfiguration (ohne Default).
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Liest einen Wert aus der Konfiguration, falls nicht vorhanden wird defaultValue verwendet.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
