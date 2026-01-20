package at.technikum.mrp.model;

import java.time.LocalDateTime;

/**
 * Domain-Model für einen User.
 * Entspricht der Tabelle "users" in der DB.
 * Passwort liegt nur als Hash vor (passwordHash).
 */
public class User {

    private Integer id;
    private String username;
    private String passwordHash;
    private String email;
    private LocalDateTime createdAt;
    private Integer totalRatings;
    private Double averageRating;

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.passwordHash = builder.passwordHash;
        this.email = builder.email;
        this.createdAt = builder.createdAt;
        this.totalRatings = builder.totalRatings;
        this.averageRating = builder.averageRating;
    }

    // Getters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getTotalRatings() { return totalRatings; }
    public Double getAverageRating() { return averageRating; }

    // Setters (z.B. ID/Stats können später durch DB/Updates gesetzt werden)
    public void setId(Integer id) { this.id = id; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public void setEmail(String email) { this.email = email; }

    public static class Builder {
        private Integer id;
        private String username;
        private String passwordHash;
        private String email;
        private LocalDateTime createdAt = LocalDateTime.now();
        private Integer totalRatings = 0;
        private Double averageRating = 0.0;

        public Builder() {}

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder totalRatings(Integer totalRatings) { this.totalRatings = totalRatings; return this; }
        public Builder averageRating(Double averageRating) { this.averageRating = averageRating; return this; }

        public User build() {
            return new User(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
