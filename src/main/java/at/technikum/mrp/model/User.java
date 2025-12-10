package at.technikum.mrp.model;

import java.time.LocalDateTime;

public class User {
    // Attribute
    private Integer id;
    private String username;
    private String passwordHash;
    private String email;
    private LocalDateTime createdAt;
    private Integer totalRatings;
    private Double averageRating;

    // 2. Privater Konstruktor (nur Builder kann ihn aufrufen)
    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.passwordHash = builder.passwordHash;
        this.email = builder.email;
        this.createdAt = builder.createdAt;
        this.totalRatings = builder.totalRatings;
        this.averageRating = builder.averageRating;
    }

    // 3. Getter
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getTotalRatings() { return totalRatings; }
    public Double getAverageRating() { return averageRating; }

    // 4. Builder Klasse
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
            // Validierung
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (passwordHash == null || passwordHash.trim().isEmpty()) {
                throw new IllegalArgumentException("Password hash is required");
            }
            return new User(this);
        }
    }

    // 5. Statischer Builder-Methode
    public static Builder builder() {
        return new Builder();
    }
}