package at.technikum.mrp.dto;

/**
 * DTO für den Login-Endpoint.
 * Wird von Jackson aus dem JSON Body gebaut: { "username": "...", "password": "..." }.
 */
public class LoginRequest {
    private String username;
    private String password;

    // Default-Konstruktor ist wichtig für Jackson (sonst kann er nicht deserialisieren)
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter/Setter: werden von Jackson und vom Code verwendet
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
