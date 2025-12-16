package at.technikum.mrp.model;

import java.time.LocalDateTime;

/**
 * Domain-Model für einen User.
 * Entspricht  der Tabelle "users" in der DB.
 * Wichtig: kein klartext password, sondern passwordHash.
 */
public class User {
    private Integer id;
    private String username;
    private String passwordHash;
    private String email;
    private LocalDateTime createdAt;
    private Integer totalRatings = 0;
    private Double averageRating = 0.0;

    // private User über Builder
    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.passwordHash = builder.passwordHash;
        this.email = builder.email;
        this.createdAt = builder.createdAt;
        this.totalRatings = builder.totalRatings;
        this.averageRating = builder.averageRating;
    }

    // Getter
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getTotalRatings() { return totalRatings; }
    public Double getAverageRating() { return averageRating; }

  // Builder für User
    public static class Builder {
        private Integer id;
        private String username;
        private String passwordHash;
        private String email;
        private LocalDateTime createdAt = LocalDateTime.now();
        private Integer totalRatings = 0;
        private Double averageRating = 0.0;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder totalRatings(Integer totalRatings) {
            this.totalRatings = totalRatings;
            return this;
        }

        public Builder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public User build() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username ist erforderlich");
            }
            if (passwordHash == null) {
                throw new IllegalArgumentException("PasswordHash ist erforderlich");
            }
            // Defaults absichern, falls DB mal NULL liefern würde
            if (totalRatings == null) totalRatings = 0;
            if (averageRating == null) averageRating = 0.0;
            return new User(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
