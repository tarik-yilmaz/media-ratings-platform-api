package at.technikum.mrp.repository;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.model.Rating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository für Ratings.
 * Enthält die JDBC/SQL-Logik für Insert/Update/Delete und einfache Abfragen.
 */
public class RatingRepository {

    /**
     * Rating per ID holen.
     */
    public Optional<Rating> findById(Integer id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Finden des Ratings: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Rating anhand (mediaId, userId) holen.
     * Praktisch weil in der DB UNIQUE(media_id, user_id) gilt.
     */
    public Optional<Rating> findByMediaAndUser(Integer mediaId, Integer userId) {
        String sql = "SELECT * FROM ratings WHERE media_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Finden des Ratings: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Speichert ein neues Rating.
     * Achtung: Spaltennamen müssen zur schema.sql passen.
     */
    public Rating save(Rating rating) {
        String sql = "INSERT INTO ratings (media_id, user_id, stars, comment, confirmed) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "RETURNING id, created_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rating.getMediaId());
            stmt.setInt(2, rating.getUserId());
            stmt.setInt(3, rating.getStars());
            stmt.setString(4, rating.getComment());
            stmt.setBoolean(5, rating.getConfirmed());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Neu gebautes Rating mit DB-Feldern
                return Rating.builder()
                        .id(rs.getInt("id"))
                        .mediaId(rating.getMediaId())
                        .userId(rating.getUserId())
                        .stars(rating.getStars())
                        .comment(rating.getComment())
                        .confirmed(rating.getConfirmed())
                        .likesCount(0)
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build();
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern des Ratings: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update eines Ratings.
     * Sicherheitscheck: WHERE id AND user_id, damit nur der Ersteller updaten darf.
     */
    public boolean update(Rating rating) {
        String sql = "UPDATE ratings SET stars = ?, comment = ?, confirmed = ? " +
                "WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rating.getStars());
            stmt.setString(2, rating.getComment());
            stmt.setBoolean(3, rating.getConfirmed());
            stmt.setInt(4, rating.getId());
            stmt.setInt(5, rating.getUserId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Aktualisieren des Ratings: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete eines Ratings.
     * Sicherheitscheck: nur Ersteller (user_id) darf löschen.
     */
    public boolean delete(Integer ratingId, Integer userId) {
        String sql = "DELETE FROM ratings WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen des Ratings: " + e.getMessage());
            return false;
        }
    }

    /**
     * Alle Ratings zu einem Media (neueste zuerst).
     */
    public List<Rating> findByMediaId(Integer mediaId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE media_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Ratings: " + e.getMessage());
        }
        return ratings;
    }

    /**
     * Alle Ratings eines Users (z.B. Profil/History).
     */
    public List<Rating> findByUserId(Integer userId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der User-Ratings: " + e.getMessage());
        }
        return ratings;
    }

    /**
     * Setzt confirmed = true.
     * Für die Bestätigung des Kommentars.
     */
    public boolean confirmComment(Integer ratingId, Integer userId) {
        String sql = "UPDATE ratings SET confirmed = TRUE WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Bestätigen des Kommentars: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapping: ResultSet -> Rating (DB -> Java Objekt).
     */
    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        return Rating.builder()
                .id(rs.getInt("id"))
                .mediaId(rs.getInt("media_id"))
                .userId(rs.getInt("user_id"))
                .stars(rs.getInt("stars"))
                .comment(rs.getString("comment"))
                .confirmed(rs.getBoolean("confirmed"))
                .likesCount(rs.getInt("likes_count"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
