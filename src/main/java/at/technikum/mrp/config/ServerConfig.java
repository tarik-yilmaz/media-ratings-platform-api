package at.technikum.mrp.config;

import java.util.Properties;

/**
 * Server- und Security-Konfiguration (Port/Host, BCrypt-Rounds, Token-Settings).
 * Werte kommen aus application.properties, damit man sie einfach ändern kann ohne Code anzufassen.
 */
public class ServerConfig {
    private static final Properties properties = new Properties();

    static {
        // Beim Start properties Datei laden (ähnlich wie DatabaseConfig)
        try {
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

    /**
     * Gibt den Server-Port zurück (Default: 8080).
     * Falls jemand einen ungültigen Wert in die properties schreibt, fallen wir auf 8080 zurück.
     */
    public static int getPort() {
        try {
            return Integer.parseInt(properties.getProperty("server.port", "8080"));
        } catch (NumberFormatException e) {
            return 8080; // Standard-Port
        }
    }

    /**
     * Hostname (Default: localhost).
     * Ist v.a. nützlich, wenn man später mal nicht lokal testen will.
     */
    public static String getHost() {
        return properties.getProperty("server.host", "localhost");
    }

    /**
     * BCrypt-Rounds: steuert wie "aufwendig" das Hashing ist.
     * Mehr Runden = sicherer, aber Login/Register brauchen länger.
     */
    public static int getBcryptRounds() {
        try {
            return Integer.parseInt(properties.getProperty("bcrypt.rounds", "12"));
        } catch (NumberFormatException e) {
            return 12; // Standard
        }
    }

    /**
     * Token-Ablaufzeit in Stunden (Default: 24)
     */
    public static int getTokenExpirationHours() {
        try {
            return Integer.parseInt(properties.getProperty("token.expiration.hours", "24"));
        } catch (NumberFormatException e) {
            return 24; // Standard
        }
    }

    /**
     * Secret/Key für Token-Generierung (hardcoded für den Anfang).
     */
    public static String getTokenSecret() {
        return properties.getProperty("token.secret", "student-project-key");
    }

    /**
     * Debugging-Ausgabe der Server-Settings beim Start.
     */
    public static void printConfig() {
        System.out.println("=== Server Konfiguration ===");
        System.out.println("Host: " + getHost());
        System.out.println("Port: " + getPort());
        System.out.println("BCrypt Runden: " + getBcryptRounds());
        System.out.println("Token Gültigkeit: " + getTokenExpirationHours() + " Stunden");
        System.out.println("============================");
    }
}
