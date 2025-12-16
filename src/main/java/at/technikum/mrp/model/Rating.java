package at.technikum.mrp.model;

import java.time.LocalDateTime;

/**
 * Domain-Model für eine Bewertung (Rating).
 * Entspricht der Tabelle "ratings" in der DB.
 * Ein Rating gehört zu genau einem Media und einem User.
 */
public class Rating {
    private Integer id;
    private Integer mediaId;
    private Integer userId;
    private Integer stars;  // 1-5
    private String comment;
    private Boolean confirmed = false;  // ob Kommentar bestätigt wurde
    private Integer likesCount = 0;     // Anzahl Likes
    private LocalDateTime createdAt;

    // private Rating über Builder
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

    // Getter
    public Integer getId() { return id; }
    public Integer getMediaId() { return mediaId; }
    public Integer getUserId() { return userId; }
    public Integer getStars() { return stars; }
    public String getComment() { return comment; }
    public Boolean getConfirmed() { return confirmed; }
    public Integer getLikesCount() { return likesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Builder für Rating wie bei Media und User.
     * Validiert Sternebereich und Pflichtfelder (mediaId, userId).
     */
    public static class Builder {
        private Integer id;
        private Integer mediaId;
        private Integer userId;
        private Integer stars;
        private String comment;
        private Boolean confirmed = false;
        private Integer likesCount = 0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder mediaId(Integer mediaId) { this.mediaId = mediaId; return this; }
        public Builder userId(Integer userId) { this.userId = userId; return this; }
        public Builder stars(Integer stars) { this.stars = stars; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder confirmed(Boolean confirmed) { this.confirmed = confirmed; return this; }
        public Builder likesCount(Integer likesCount) { this.likesCount = likesCount; return this; }
        public Builder createdAt(LocalDateTime time) { this.createdAt = time; return this; }

        public Rating build() {
            if (mediaId == null) throw new IllegalArgumentException("MediaId ist erforderlich");
            if (userId == null) throw new IllegalArgumentException("UserId ist erforderlich");

            // Sterne müssen im Bereich 1..5 liegen (passt zur DB-Constraint)
            if (stars == null || stars < 1 || stars > 5) {
                throw new IllegalArgumentException("Sterne müssen zwischen 1 und 5 sein");
            }

            // Defaults
            if (likesCount == null) likesCount = 0;
            if (confirmed == null) confirmed = false;

            return new Rating(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
