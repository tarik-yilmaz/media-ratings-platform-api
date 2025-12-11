package at.technikum.mrp.config;

import java.util.Properties;


// Server-Konfiguration
public class ServerConfig {
    private static final Properties properties = new Properties();

    static {
        // Nutze DatabaseConfig um properties zu laden
        // (einfacher Trick: DatabaseConfig hat sie schon geladen)
        try {
            // Wir laden die properties nochmal für diese Klasse
            // (in einem echten Projekt würde man das zentral machen)
            try (java.io.InputStream input = ServerConfig.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Server-Konfiguration: " + e.getMessage());
        }
    }

    // Server-Port (Standard: 8080).
    public static int getPort() {
        try {
            return Integer.parseInt(properties.getProperty("server.port", "8080"));
        } catch (NumberFormatException e) {
            return 8080; // Standard-Port
        }
    }

    //Server-Host (Standard: localhost).
    public static String getHost() {
        return properties.getProperty("server.host", "localhost");
    }

    // BCrypt Runden für Passwort-Hashing
    public static int getBcryptRounds() {
        try {
            return Integer.parseInt(properties.getProperty("bcrypt.rounds", "12"));
        } catch (NumberFormatException e) {
            return 12; // Standard
        }
    }


    // Token Ablaufzeit in Stunden
    public static int getTokenExpirationHours() {
        try {
            return Integer.parseInt(properties.getProperty("token.expiration.hours", "24"));
        } catch (NumberFormatException e) {
            return 24; // Standard
        }
    }

    // Token Secret Key
    public static String getTokenSecret() {
        return properties.getProperty("token.secret", "student-project-key");
    }

    // Zeigt Server-Konfiguration auf der Konsole an
    public static void printConfig() {
        System.out.println("=== Server Konfiguration ===");
        System.out.println("Host: " + getHost());
        System.out.println("Port: " + getPort());
        System.out.println("BCrypt Runden: " + getBcryptRounds());
        System.out.println("Token Gültigkeit: " + getTokenExpirationHours() + " Stunden");
        System.out.println("============================");
    }
}