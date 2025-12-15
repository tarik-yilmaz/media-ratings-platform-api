package at.technikum.mrp.dto;

public class RatingRequest {
    private Integer stars; // 1-5
    private String comment;

    public RatingRequest() {}

    public RatingRequest(Integer stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    // Getter und Setter
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}