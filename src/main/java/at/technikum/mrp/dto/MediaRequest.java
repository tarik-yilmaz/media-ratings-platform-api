package at.technikum.mrp.dto;

import java.util.List;

/**
 * DTO für Media Create/Update.
 * Kommt als JSON rein und wird dann im Service in ein echtes Media-Model umgewandelt.
 */
public class MediaRequest {
    private String title;
    private String description;
    private String mediaType;  // z.B. "movie" oder "MOVIE" (wird später normalisiert)
    private Integer releaseYear;
    private List<String> genres;
    private Integer ageRestriction;

    // Getter/Setter für Jackson + weitere Verarbeitung
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public Integer getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(Integer ageRestriction) { this.ageRestriction = ageRestriction; }
}
