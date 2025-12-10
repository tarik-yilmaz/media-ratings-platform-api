package at.technikum.mrp.model;

import java.time.LocalDateTime;

public class Rating {
    private Integer id;
    private Integer mediaId;
    private Integer userId;
    private Integer stars; // 1-5
    private String comment;
    private Boolean commentConfirmed = false;
    private Integer likesCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Rating(Builder builder) {
        this.id = builder.id;
        this.mediaId = builder.mediaId;
        this.userId = builder.userId;
        this.stars = builder.stars;
        this.comment = builder.comment;
        this.commentConfirmed = builder.commentConfirmed;
        this.likesCount = builder.likesCount;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Getter
    public Integer getId() { return id; }
    public Integer getMediaId() { return mediaId; }
    public Integer getUserId() { return userId; }
    public Integer getStars() { return stars; }
    public String getComment() { return comment; }
    public Boolean getCommentConfirmed() { return commentConfirmed; }
    public Integer getLikesCount() { return likesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Builder
    public static class Builder {
        private Integer id;
        private Integer mediaId;
        private Integer userId;
        private Integer stars;
        private String comment;
        private Boolean commentConfirmed = false;
        private Integer likesCount = 0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder mediaId(Integer mediaId) {
            this.mediaId = mediaId;
            return this;
        }

        public Builder userId(Integer userId) {
            this.userId = userId;
            return this;
        }

        public Builder stars(Integer stars) {
            this.stars = stars;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder commentConfirmed(Boolean commentConfirmed) {
            this.commentConfirmed = commentConfirmed;
            return this;
        }

        public Builder likesCount(Integer likesCount) {
            this.likesCount = likesCount;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Rating build() {
            if (mediaId == null) {
                throw new IllegalArgumentException("Media ID is required");
            }
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (stars == null || stars < 1 || stars > 5) {
                throw new IllegalArgumentException("Stars must be between 1 and 5");
            }
            return new Rating(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}