package at.technikum.mrp.repository;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository für User-Operationen in der Datenbank.
 * Implementiert CRUD (Create, Read, Update, Delete) für User.
 */
public class UserRepository {

    /**
     * Findet einen User anhand des Usernames.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Finden des Users: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Findet einen User anhand der ID.
     */
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Finden des Users: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Speichert einen neuen User in der Datenbank.
     * Gibt den gespeicherten User mit generierter ID zurück.
     */
    public User save(User user) {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?) " +
                "RETURNING id, created_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getEmail());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return User.builder()
                        .id(rs.getInt("id"))
                        .username(user.getUsername())
                        .passwordHash(user.getPasswordHash())
                        .email(user.getEmail())
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .totalRatings(0)
                        .averageRating(0.0)
                        .build();
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern des Users: " + e.getMessage());
        }
        return null;
    }

    /**
     * Aktualisiert einen bestehenden User.
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, email = ?, " +
                "total_ratings = ?, average_rating = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, user.getTotalRatings());
            stmt.setDouble(5, user.getAverageRating());
            stmt.setInt(6, user.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Aktualisieren des Users: " + e.getMessage());
            return false;
        }
    }

    /**
     * Löscht einen User anhand der ID.
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen des Users: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gibt alle Users zurück (für Admin-Funktionen).
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden aller Users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Aktualisiert die User-Statistiken (Anzahl und Durchschnitt der Ratings).
     */
    public boolean updateUserStatistics(Integer userId) {
        String sql = "UPDATE users SET " +
                "total_ratings = (SELECT COUNT(*) FROM ratings WHERE user_id = ?), " +
                "average_rating = (SELECT COALESCE(AVG(stars), 0.0) FROM ratings WHERE user_id = ?) " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Aktualisieren der User-Statistiken: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hilfsmethode: Konvertiert ResultSet zu User-Objekt.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .email(rs.getString("email"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .totalRatings(rs.getInt("total_ratings"))
                .averageRating(rs.getDouble("average_rating"))
                .build();
    }
}