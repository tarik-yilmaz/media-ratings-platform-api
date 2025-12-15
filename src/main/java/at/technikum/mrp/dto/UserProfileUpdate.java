package at.technikum.mrp.dto;

public class UserProfileUpdate {
    private String email;
    private String favoriteGenre;

    public UserProfileUpdate() {}

    public UserProfileUpdate(String email, String favoriteGenre) {
        this.email = email;
        this.favoriteGenre = favoriteGenre;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFavoriteGenre() { return favoriteGenre; }
    public void setFavoriteGenre(String favoriteGenre) { this.favoriteGenre = favoriteGenre; }
}