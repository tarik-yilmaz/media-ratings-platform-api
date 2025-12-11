package at.technikum.mrp.model;

import java.time.LocalDateTime;

public class Rating {
    private Integer id;
    private Integer mediaId;
    private Integer userId;
    private Integer stars;  // 1-5
    private String comment;
    private Boolean confirmed = false;  // Kommentar-Bestätigung
    private LocalDateTime createdAt;

    private Rating(Builder builder) {
        this.id = builder.id;
        this.mediaId = builder.mediaId;
        this.userId = builder.userId;
        this.stars = builder.stars;
        this.comment = builder.comment;
        this.confirmed = builder.confirmed;
        this.createdAt = builder.createdAt;
    }

    // Getter
    public Integer getId() { return id; }
    public Integer getMediaId() { return mediaId; }
    public Integer getUserId() { return userId; }
    public Integer getStars() { return stars; }
    public String getComment() { return comment; }
    public Boolean getConfirmed() { return confirmed; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private Integer id;
        private Integer mediaId;
        private Integer userId;
        private Integer stars;
        private String comment;
        private Boolean confirmed = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder mediaId(Integer mediaId) { this.mediaId = mediaId; return this; }
        public Builder userId(Integer userId) { this.userId = userId; return this; }
        public Builder stars(Integer stars) { this.stars = stars; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder confirmed(Boolean confirmed) { this.confirmed = confirmed; return this; }
        public Builder createdAt(LocalDateTime time) { this.createdAt = time; return this; }

        public Rating build() {
            if (mediaId == null) throw new IllegalArgumentException("MediaId ist erforderlich");
            if (userId == null) throw new IllegalArgumentException("UserId ist erforderlich");
            if (stars == null || stars < 1 || stars > 5) {
                throw new IllegalArgumentException("Sterne müssen zwischen 1 und 5 sein");
            }
            return new Rating(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}