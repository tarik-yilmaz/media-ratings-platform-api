package at.technikum.mrp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain-Model für ein Medium (Movie/Series/Game).
 * Entspricht grob der Tabelle "media" in der DB.
 */
public class Media {

    private Integer id;
    private Integer creatorId;
    private String title;
    private String description;
    private String type;              // bei dir: type (statt mediaType)
    private Integer releaseYear;
    private Integer ageRestriction;
    private Double averageScore;      // bei dir: averageScore (statt averageRating)
    private LocalDateTime createdAt;
    private List<String> genres;

    private Media(Builder builder) {
        this.id = builder.id;
        this.creatorId = builder.creatorId;
        this.title = builder.title;
        this.description = builder.description;
        this.type = builder.type;
        this.releaseYear = builder.releaseYear;
        this.ageRestriction = builder.ageRestriction;
        this.averageScore = builder.averageScore;
        this.createdAt = builder.createdAt;
        this.genres = builder.genres;
    }

    // Getters
    public Integer getId() { return id; }
    public Integer getCreatorId() { return creatorId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public Integer getReleaseYear() { return releaseYear; }
    public Integer getAgeRestriction() { return ageRestriction; }
    public Double getAverageScore() { return averageScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getGenres() { return genres; }

    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public static class Builder {
        private Integer id;
        private Integer creatorId;
        private String title;
        private String description;
        private String type;
        private Integer releaseYear;
        private Integer ageRestriction;
        private Double averageScore = 0.0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private List<String> genres = new ArrayList<>();

        public Builder() {}

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder creatorId(Integer creatorId) { this.creatorId = creatorId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }

        // Gleiche Normalisierung wie früher, damit DB/Filter stabil bleiben
        public Builder type(String type) {
            this.type = (type == null) ? null : type.trim().toUpperCase();
            return this;
        }

        public Builder releaseYear(Integer releaseYear) { this.releaseYear = releaseYear; return this; }
        public Builder ageRestriction(Integer ageRestriction) { this.ageRestriction = ageRestriction; return this; }
        public Builder averageScore(Double averageScore) { this.averageScore = averageScore; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder genres(List<String> genres) { this.genres = (genres == null) ? new ArrayList<>() : genres; return this; }

        public Media build() {
            return new Media(this);
        }
    }

    // Für Rückwärtskompatibilität: alter Code kann weiter Media.builder() nutzen
    public static Builder builder() {
        return new Builder();
    }
}
