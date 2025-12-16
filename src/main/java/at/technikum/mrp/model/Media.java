package at.technikum.mrp.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain-Model für ein Medium (Movie/Series/Game).
 * Wird in Services/Repositores verwendet und entspricht grob der Tabelle "media" in der DB.
 * Builder-Pattern, damit man viele Felder sauber setzen (Übersichtlichkeit Parameter).
 */
public class Media {

    private Integer id;
    private String title;
    private String description;
    private String type;
    private Integer releaseYear;
    private List<String> genres;
    private Integer ageRestriction;
    private Integer creatorId;
    private Double averageScore = 0.0;
    private LocalDateTime createdAt;

    // Konstruktor ist privat -> Media wird nur über Builder gebaut
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
    public String getType() { return type; }
    public Integer getReleaseYear() { return releaseYear; }
    public List<String> getGenres() { return genres; }
    public Integer getAgeRestriction() { return ageRestriction; }
    public Integer getCreatorId() { return creatorId; }
    public Double getAverageScore() { return averageScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }


    // Builder-Kontstruktor
    public static class Builder {
        private Integer id;
        private String title;
        private String description;
        private String type;
        private Integer releaseYear;
        private List<String> genres;
        private Integer ageRestriction;
        private Integer creatorId;
        private Double averageScore = 0.0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }

        /**
         * type wird normalisiert (trim + uppercase), damit DB-Check und Vergleich einfach bleiben.
         */
        public Builder type(String type) {
            this.type = (type == null) ? null : type.trim().toUpperCase();
            return this;
        }

        public Builder releaseYear(Integer year) { this.releaseYear = year; return this; }
        public Builder genres(List<String> genres) { this.genres = genres; return this; }
        public Builder ageRestriction(Integer age) { this.ageRestriction = age; return this; }
        public Builder creatorId(Integer creatorId) { this.creatorId = creatorId; return this; }
        public Builder averageScore(Double score) { this.averageScore = score; return this; }
        public Builder createdAt(LocalDateTime time) { this.createdAt = time; return this; }

        /**
         * Baut das Media-Objekt und macht dabei die wichtigsten Checks.
         * (Titel, Typ, Creator müssen vorhanden sein.)
         */
        public Media build() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Titel ist erforderlich");
            }
            if (type == null) {
                throw new IllegalArgumentException("Medientyp ist erforderlich");
            }
            // Wir lassen nur die drei erlaubten Typen zu
            if (!type.equals("MOVIE") && !type.equals("SERIES") && !type.equals("GAME")) {
                throw new IllegalArgumentException("Medientyp muss MOVIE, SERIES oder GAME sein");
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

    // Entry-Point für Builder: Media.builder().title(...).type(...).build()
    public static Builder builder() {
        return new Builder();
    }
}
