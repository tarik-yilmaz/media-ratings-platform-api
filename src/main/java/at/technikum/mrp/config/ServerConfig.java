package at.technikum.mrp.config;

/**
 * Server-Konfiguration.
 * Liest Werte aus application.properties über DatabaseConfig
 */
public class ServerConfig {

    // Server-Port (Standard: 8080)
    public static int getPort() {
        try {
            return Integer.parseInt(DatabaseConfig.getProperty("server.port", "8080"));
        } catch (NumberFormatException e) {
            return 8080;
        }
    }

    // BCrypt Runden für Passwort-Hashing (Standard: 12)
    public static int getBcryptRounds() {
        try {
            return Integer.parseInt(DatabaseConfig.getProperty("bcrypt.rounds", "12"));
        } catch (NumberFormatException e) {
            return 12;
        }
    }

    // Token Ablaufzeit in Stunden (Standard: 24)
    public static int getTokenExpirationHours() {
        try {
            return Integer.parseInt(DatabaseConfig.getProperty("token.expiration.hours", "24"));
        } catch (NumberFormatException e) {
            return 24;
        }
    }

    public static void printConfig() {
        System.out.println("=== Server Konfiguration ===");
        System.out.println("Port: " + getPort());
        System.out.println("BCrypt Runden: " + getBcryptRounds());
        System.out.println("Token Gültigkeit: " + getTokenExpirationHours() + " Stunden");
        System.out.println("============================");
    }
}
