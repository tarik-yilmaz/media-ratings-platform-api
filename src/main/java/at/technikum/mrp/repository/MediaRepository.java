package at.technikum.mrp.repository;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.model.Media;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Repository für Media-Operationen.
 * Nur SQL/JDBC-Logik, damit Services/Controller sauber bleiben.
 */
public class MediaRepository {

    /**
     * Holt ein Media per ID.
     * Optional.empty() wenn nicht gefunden oder SQL-Fehler.
     */
    public Optional<Media> findById(Integer id) {
        String sql = "SELECT * FROM media WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMedia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Finden des Media: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Insert eines neuen Media.
     * RETURNING liefert die generierte id und created_at zurück (PostgreSQL Feature).
     */
    public Media save(Media media) {
        String sql = "INSERT INTO media (title, description, media_type, release_year, " +
                "genres, age_restriction, creator_id, average_score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING id, created_at";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getType());
            stmt.setObject(4, media.getReleaseYear(), Types.INTEGER);
            stmt.setString(5, genresToString(media.getGenres()));
            stmt.setObject(6, media.getAgeRestriction(), Types.INTEGER);
            stmt.setInt(7, media.getCreatorId());
            stmt.setDouble(8, media.getAverageScore());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Wir bauen ein neues Media mit den DB-Feldern (id, created_at)
                return Media.builder()
                        .id(rs.getInt("id"))
                        .title(media.getTitle())
                        .description(media.getDescription())
                        .type(media.getType())
                        .releaseYear(media.getReleaseYear())
                        .genres(media.getGenres())
                        .ageRestriction(media.getAgeRestriction())
                        .creatorId(media.getCreatorId())
                        .averageScore(media.getAverageScore())
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build();
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern des Media: " + e.getMessage());
        }
        return null; // Service macht daraus dann einen 500er
    }

    /**
     * Update eines Media.
     * Security-Check über SQL: update nur wenn id UND creator_id passen.
     */
    public boolean update(Media media) {
        String sql = "UPDATE media SET title = ?, description = ?, media_type = ?, " +
                "release_year = ?, genres = ?, age_restriction = ?, " +
                "average_score = ? WHERE id = ? AND creator_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getType());
            stmt.setObject(4, media.getReleaseYear(), Types.INTEGER);
            stmt.setString(5, genresToString(media.getGenres()));
            stmt.setObject(6, media.getAgeRestriction(), Types.INTEGER);
            stmt.setDouble(7, media.getAverageScore());
            stmt.setInt(8, media.getId());
            stmt.setInt(9, media.getCreatorId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Aktualisieren des Media: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete eines Media.
     * Nur möglich wenn creator_id passt (damit nicht jeder alles löschen kann).
     */
    public boolean delete(Integer mediaId, Integer creatorId) {
        String sql = "DELETE FROM media WHERE id = ? AND creator_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            stmt.setInt(2, creatorId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen des Media: " + e.getMessage());
            return false;
        }
    }

    /**
     * Suche nach Titel (case-insensitive).
     */
    public List<Media> searchByTitle(String searchTerm) {
        List<Media> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE LOWER(title) LIKE LOWER(?) ORDER BY title";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler bei der Suche nach Media: " + e.getMessage());
        }
        return mediaList;
    }

    /**
     * Gibt alle Media zurück (unfiltered).
     */
    public List<Media> findAll() {
        List<Media> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media ORDER BY title";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden aller Media: " + e.getMessage());
        }
        return mediaList;
    }

    /**
     * Media eines bestimmten Creators (z.B. "meine Einträge").
     */
    public List<Media> findByCreatorId(Integer creatorId) {
        List<Media> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE creator_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, creatorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Media des Creators: " + e.getMessage());
        }
        return mediaList;
    }

    /**
     * Rechnet average_score neu aus (AVG aller ratings für dieses Media).
     */
    public boolean updateAverageScore(Integer mediaId) {
        String sql = "UPDATE media SET average_score = " +
                "(SELECT COALESCE(AVG(stars), 0.0) FROM ratings WHERE media_id = ?) " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            stmt.setInt(2, mediaId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Fehler beim Aktualisieren des average_score: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapping: ResultSet -> Media.
     * Hier passiert die "Übersetzung" DB-Spaltennamen -> Java-Felder.
     */
    private Media mapResultSetToMedia(ResultSet rs) throws SQLException {
        Integer releaseYear = (Integer) rs.getObject("release_year");
        Integer ageRestriction = (Integer) rs.getObject("age_restriction");

        return Media.builder()
                .id(rs.getInt("id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .type(rs.getString("media_type"))
                .releaseYear(releaseYear)
                .genres(stringToGenres(rs.getString("genres")))
                .ageRestriction(ageRestriction)
                .creatorId((Integer) rs.getObject("creator_id")) // sicherer als getInt()
                .averageScore(rs.getDouble("average_score"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    /**
     * Speichert Genres als "Action,Drama,...".
     */
    private String genresToString(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return String.join(",", genres);
    }

    /**
     * Liest Genres aus "Action,Drama,..." und macht daraus wieder eine Liste.
     */
    private List<String> stringToGenres(String genresString) {
        if (genresString == null || genresString.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts = genresString.split(",");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String g = p.trim();
            if (!g.isEmpty()) out.add(g);
        }
        return out;
    }

    /**
     * Kombinierte Filter-Suche + Sortierung.
     * Baut SQL dynamisch zusammen, je nachdem welche Filter gesetzt sind.
     */
    public List<Media> findFiltered(String title,
                                    String genre,
                                    String mediaType,
                                    Integer releaseYear,
                                    Integer ageRestriction,
                                    Double minRating,
                                    String sortBy) {

        List<Media> mediaList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM media WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // "WHERE 1=1" macht es einfacher, immer nur "AND ..." anzuhängen
        if (title != null && !title.isBlank()) {
            sql.append("AND LOWER(title) LIKE LOWER(?) ");
            params.add("%" + title + "%");
        }
        if (genre != null && !genre.isBlank()) {
            sql.append("AND LOWER(genres) LIKE LOWER(?) ");
            params.add("%" + genre + "%");
        }
        if (mediaType != null && !mediaType.isBlank()) {
            sql.append("AND media_type = ? ");
            params.add(mediaType.trim().toUpperCase());
        }
        if (releaseYear != null) {
            sql.append("AND release_year = ? ");
            params.add(releaseYear);
        }
        if (ageRestriction != null) {
            sql.append("AND age_restriction = ? ");
            params.add(ageRestriction);
        }
        if (minRating != null) {
            sql.append("AND average_score >= ? ");
            params.add(minRating);
        }

        // Sortierung: wir erlauben nur bestimmte Spalten -> Schutz gegen SQL Injection
        String order = "title";
        if (sortBy != null) {
            switch (sortBy) {
                case "year" -> order = "release_year";
                case "score" -> order = "average_score DESC";
                case "title" -> order = "title";
            }
        }
        sql.append(" ORDER BY ").append(order);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Parameter in derselben Reihenfolge setzen, wie wir sie oben gesammelt haben
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Filtern von Media: " + e.getMessage());
        }

        return mediaList;
    }

    public List<Media> findHighlyRatedByUser(int userId, int minStars) {
        List<Media> out = new ArrayList<>();

        String sql =
                "SELECT m.* " +
                        "FROM ratings r " +
                        "JOIN media m ON m.id = r.media_id " +
                        "WHERE r.user_id = ? AND r.stars >= ? " +
                        "ORDER BY r.stars DESC, m.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, minStars);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(mapResultSetToMedia(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der hoch bewerteten Media: " + e.getMessage());
        }

        return out;
    }

    public List<Media> findNotRatedByUser(int userId) {
        List<Media> out = new ArrayList<>();

        String sql =
                "SELECT * FROM media " +
                        "WHERE id NOT IN (SELECT media_id FROM ratings WHERE user_id = ?) " +
                        "ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(mapResultSetToMedia(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden nicht bewerteter Media: " + e.getMessage());
        }

        return out;
    }

    public List<Media> findTopRated(int limit) {
        List<Media> out = new ArrayList<>();

        String sql =
                "SELECT * FROM media " +
                        "ORDER BY average_score DESC, created_at DESC, title ASC " +
                        "LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(mapResultSetToMedia(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden Top-Rated Media: " + e.getMessage());
        }

        return out;
    }

}
