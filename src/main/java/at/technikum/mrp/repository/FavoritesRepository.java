package at.technikum.mrp.repository;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.model.Media;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesRepository {

    // true = neu eingefügt, false = war schon Favorit
    public boolean addFavorite(int userId, int mediaId) {
        String sql =
                "INSERT INTO favorites (user_id, media_id) VALUES (?, ?) " +
                        "ON CONFLICT (user_id, media_id) DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Hinzufügen zu Favorites: " + e.getMessage());
            return false;
        }
    }

    // true = gelöscht, false = war nicht Favorit
    public boolean removeFavorite(int userId, int mediaId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Entfernen aus Favorites: " + e.getMessage());
            return false;
        }
    }

    // Liste aller favorisierten Media-Einträge eines Users
    public List<Media> findFavoritesByUserId(int userId) {
        List<Media> out = new ArrayList<>();

        String sql =
                "SELECT m.* " +
                        "FROM favorites f " +
                        "JOIN media m ON m.id = f.media_id " +
                        "WHERE f.user_id = ? " +
                        "ORDER BY f.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(mapResultSetToMedia(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Favorites: " + e.getMessage());
        }

        return out;
    }

    // --- Helper: Media mappen (ähnlich wie im MediaRepository) ---
    private Media mapResultSetToMedia(ResultSet rs) throws SQLException {
        return Media.builder()
                .id(rs.getInt("id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .type(rs.getString("media_type"))
                .releaseYear((Integer) rs.getObject("release_year"))
                .genres(stringToGenres(rs.getString("genres")))
                .ageRestriction((Integer) rs.getObject("age_restriction"))
                .creatorId(rs.getInt("creator_id"))
                .averageScore(rs.getDouble("average_score"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    private List<String> stringToGenres(String genresString) {
        if (genresString == null || genresString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(genresString.split(","));
    }
}
