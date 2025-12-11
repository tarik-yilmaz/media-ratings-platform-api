package at.technikum.mrp.dto;

public class RatingRequest {
    private Integer mediaId;
    private Integer stars;
    private String comment;

    public RatingRequest() {}

    public RatingRequest(Integer mediaId, Integer stars, String comment) {
        this.mediaId = mediaId;
        this.stars = stars;
        this.comment = comment;
    }

    // Getter und Setter
    public Integer getMediaId() { return mediaId; }
    public void setMediaId(Integer mediaId) { this.mediaId = mediaId; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}