package at.technikum.mrp.model;

import java.time.LocalDateTime;
import java.util.List;

public class Media {
    public enum MediaType {
        MOVIE, SERIES, GAME
    }

    private Integer id;
    private String title;
    private String description;
    private MediaType mediaType;
    private Integer releaseYear;
    private List<String> genres;
    private Integer ageRestriction;
    private Integer creatorId;
    private Double averageScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Media(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.mediaType = builder.mediaType;
        this.releaseYear = builder.releaseYear;
        this.genres = builder.genres;
        this.ageRestriction = builder.ageRestriction;
        this.creatorId = builder.creatorId;
        this.averageScore = builder.averageScore;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Getter
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MediaType getMediaType() { return mediaType; }
    public Integer getReleaseYear() { return releaseYear; }
    public List<String> getGenres() { return genres; }
    public Integer getAgeRestriction() { return ageRestriction; }
    public Integer getCreatorId() { return creatorId; }
    public Double getAverageScore() { return averageScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Builder
    public static class Builder {
        private Integer id;
        private String title;
        private String description;
        private MediaType mediaType;
        private Integer releaseYear;
        private List<String> genres;
        private Integer ageRestriction;
        private Integer creatorId;
        private Double averageScore = 0.0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder mediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder releaseYear(Integer releaseYear) {
            this.releaseYear = releaseYear;
            return this;
        }

        public Builder genres(List<String> genres) {
            this.genres = genres;
            return this;
        }

        public Builder ageRestriction(Integer ageRestriction) {
            this.ageRestriction = ageRestriction;
            return this;
        }

        public Builder creatorId(Integer creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public Builder averageScore(Double averageScore) {
            this.averageScore = averageScore;
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

        public Media build() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            if (mediaType == null) {
                throw new IllegalArgumentException("Media type is required");
            }
            if (creatorId == null) {
                throw new IllegalArgumentException("Creator ID is required");
            }
            return new Media(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}