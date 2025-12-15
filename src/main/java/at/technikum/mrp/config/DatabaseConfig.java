package at.technikum.mrp.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//Datenbank-Konfiguration

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                System.err.println("Fehler: application.properties nicht gefunden!");
            } else {
                properties.load(input);
                System.out.println("✓ Datenbank-Konfiguration geladen");
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Konfiguration: " + e.getMessage());
        }
    }


    //Gibt eine neue Datenbank-Verbindung zurück
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new SQLException("Datenbank-Konfiguration unvollständig");
        }

        return DriverManager.getConnection(url, user, password);
    }

    // Testet die Datenbankverbindung.
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

    // Holt einen Konfigurationswert
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Holt einen Konfigurationswert mit Standardwert
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}