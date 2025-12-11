package at.technikum.mrp.model;

import java.time.LocalDateTime;
import java.util.List;

public class Media {
    public enum MediaType { MOVIE, SERIES, GAME }

    private Integer id;
    private String title;
    private String description;
    private MediaType type;
    private Integer releaseYear;
    private List<String> genres;
    private Integer ageRestriction;
    private Integer creatorId;
    private Double averageScore = 0.0;
    private LocalDateTime createdAt;

    private Media(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.type = builder.type;
        this.releaseYear = builder.releaseYear;
        this.genres = builder.genres;
        this.ageRestriction = builder.ageRestriction;
        this.creatorId = builder.creatorId;
        this.averageScore = builder.averageScore;
        this.createdAt = builder.createdAt;
    }

    // Getter
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MediaType getType() { return type; }
    public Integer getReleaseYear() { return releaseYear; }
    public List<String> getGenres() { return genres; }
    public Integer getAgeRestriction() { return ageRestriction; }
    public Integer getCreatorId() { return creatorId; }
    public Double getAverageScore() { return averageScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private Integer id;
        private String title;
        private String description;
        private MediaType type;
        private Integer releaseYear;
        private List<String> genres;
        private Integer ageRestriction;
        private Integer creatorId;
        private Double averageScore = 0.0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(MediaType type) { this.type = type; return this; }
        public Builder releaseYear(Integer year) { this.releaseYear = year; return this; }
        public Builder genres(List<String> genres) { this.genres = genres; return this; }
        public Builder ageRestriction(Integer age) { this.ageRestriction = age; return this; }
        public Builder creatorId(Integer creatorId) { this.creatorId = creatorId; return this; }
        public Builder averageScore(Double score) { this.averageScore = score; return this; }
        public Builder createdAt(LocalDateTime time) { this.createdAt = time; return this; }

        public Media build() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Titel ist erforderlich");
            }
            if (type == null) {
                throw new IllegalArgumentException("Medientyp ist erforderlich");
            }
            if (creatorId == null) {
                throw new IllegalArgumentException("Ersteller ist erforderlich");
            }
            if (averageScore == null) {
                averageScore = 0.0;
            }
            return new Media(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}