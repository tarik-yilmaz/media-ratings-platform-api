package at.technikum.mrp.dto;

public class LoginRequest {
    private String username;
    private String password;

    // Standard-Konstruktor für JSON
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter/Setter (für JSON Serialisierung)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}