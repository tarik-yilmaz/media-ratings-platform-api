package at.technikum.mrp.repository;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository für User.
 * CRUD + ein paar extra Methoden (findByUsername, updateUserStatistics).
 */
public class UserRepository {

    /**
     * Sucht einen User über username (wichtig für Login).
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
     * Sucht einen User über ID.
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
     * Insert eines neuen Users.
     * RETURNING gibt id + created_at zurück (PostgreSQL).
     */
    public User save(User user) {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?) " +
                "RETURNING id, created_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getEmail()); // email ist optional

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
     * Update eines Users (z.B. Profiländerung / Passwortwechsel).
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
     * Updatet die Email eines Users
     * Email darf auch null sein (dann wird sie gelöscht).
     */
    public boolean updateEmail(int userId, String email) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (email == null || email.isBlank()) {
                stmt.setNull(1, Types.VARCHAR);
            } else {
                stmt.setString(1, email.trim());
            }
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Update der Email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Public Leaderboard: User mit den meisten Ratings (Top N).
     */
    public List<Map<String, Object>> findLeaderboard(int limit) {
        List<Map<String, Object>> out = new ArrayList<>();

        String sql =
                "SELECT username, total_ratings, average_rating " +
                        "FROM users " +
                        "ORDER BY total_ratings DESC, average_rating DESC, username ASC " +
                        "LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(Map.of(
                            "username", rs.getString("username"),
                            "totalRatings", rs.getInt("total_ratings"),
                            "averageRating", rs.getDouble("average_rating")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden des Leaderboards: " + e.getMessage());
        }

        return out;
    }


    /**
     * Löscht einen User per ID.
     * Achtung: In der DB gibt es ON DELETE CASCADE bei ratings/favorites -> die werden dann mitgelöscht.
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
     * Liefert alle Users (z.B. Admin-Ansicht).
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
     * Berechnet total_ratings und average_rating neu.
     * Das ist eine "Denormalisierung": wir speichern Stats in users, damit man sie schnell abfragen kann.
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
     * Mapping: ResultSet -> User.
     * Wichtig: password_hash kommt mit, weil wir ihn für Login brauchen.
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
