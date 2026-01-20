package at.technikum.mrp.model;

import java.time.LocalDateTime;

/**
 * Domain-Model für eine Bewertung (Rating).
 * Entspricht der Tabelle "ratings" in der DB.
 */
public class Rating {

    private Integer id;
    private Integer mediaId;
    private Integer userId;
    private Integer stars;
    private String comment;
    private Boolean confirmed;
    private Integer likesCount;
    private LocalDateTime createdAt;

    private Rating(Builder builder) {
        this.id = builder.id;
        this.mediaId = builder.mediaId;
        this.userId = builder.userId;
        this.stars = builder.stars;
        this.comment = builder.comment;
        this.confirmed = builder.confirmed;
        this.likesCount = builder.likesCount;
        this.createdAt = builder.createdAt;
    }

    // Getters
    public Integer getId() { return id; }
    public Integer getMediaId() { return mediaId; }
    public Integer getUserId() { return userId; }
    public Integer getStars() { return stars; }
    public String getComment() { return comment; }
    public Boolean getConfirmed() { return confirmed; }
    public Integer getLikesCount() { return likesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }

    public static class Builder {
        private Integer id;
        private Integer mediaId;
        private Integer userId;
        private Integer stars;
        private String comment;
        private Boolean confirmed = false;
        private Integer likesCount = 0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder() {}

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder mediaId(Integer mediaId) { this.mediaId = mediaId; return this; }
        public Builder userId(Integer userId) { this.userId = userId; return this; }
        public Builder stars(Integer stars) { this.stars = stars; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder confirmed(Boolean confirmed) { this.confirmed = confirmed; return this; }
        public Builder likesCount(Integer likesCount) { this.likesCount = likesCount; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Rating build() {
            return new Rating(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
