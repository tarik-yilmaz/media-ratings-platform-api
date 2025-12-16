package at.technikum.mrp.service;

import at.technikum.mrp.dto.MediaRequest;
import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.util.ApiException;

import java.util.List;
import java.util.Optional;

/**
 * Business-Logik rund um Media.
 * - Validierung der Requests
 * - Ownership-Regeln (nur creator darf update/delete)
 * - ruft Repository für DB-Zugriffe auf
 */
public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    /**
     * Erstellt ein neues Media.
     * creatorId kommt aus dem Token (userId), nicht aus dem Request.
     */
    public Media create(int userId, MediaRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getTitle() == null || req.getTitle().trim().isEmpty())
            throw ApiException.badRequest("title ist erforderlich");
        if (req.getMediaType() == null || req.getMediaType().trim().isEmpty())
            throw ApiException.badRequest("mediaType ist erforderlich");

        // Builder macht nochmal Validierung (z.B. Typ MOVIE/SERIES/GAME)
        Media media = Media.builder()
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .type(req.getMediaType())
                // releaseYear kann null sein (optional)
                .releaseYear(req.getReleaseYear())
                .genres(req.getGenres())
                .ageRestriction(req.getAgeRestriction())
                .creatorId(userId)
                .averageScore(0.0)
                .build();

        Media saved = mediaRepository.save(media);
        if (saved == null) throw new ApiException(500, "Media konnte nicht gespeichert werden");
        return saved;
    }

    /**
     * Holt ein Media oder wirft 404 wenn nicht vorhanden.
     */
    public Media getById(int id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Media nicht gefunden"));
    }

    /**
     * Liste + Filter. Die eigentliche Filter-SQL liegt im Repository (findFiltered).
     */
    public List<Media> list(String title,
                            String genre,
                            String mediaType,
                            Integer releaseYear,
                            Integer ageRestriction,
                            Double minRating,
                            String sortBy) {

        return mediaRepository.findFiltered(
                title,
                genre,
                mediaType,
                releaseYear,
                ageRestriction,
                minRating,
                sortBy
        );
}

    /**
     * Update: nur creator darf ändern.
     * Wir bauen ein neues Media-Objekt, das die bestehenden Werte übernimmt, wenn im Request null ist.
     */
    public Media update(int userId, int mediaId, MediaRequest req) {
        Media existing = getById(mediaId);

        // Ownership-Check (wichtig für CRUD-Security)
        if (!existing.getCreatorId().equals(userId)) {
            throw ApiException.forbidden("Nur der Ersteller darf dieses Media ändern");
        }

        Media updated = Media.builder()
                .id(existing.getId())
                .title(req.getTitle() != null ? req.getTitle() : existing.getTitle())
                .description(req.getDescription() != null ? req.getDescription() : existing.getDescription())
                .type(req.getMediaType() != null ? req.getMediaType() : existing.getType())
                .releaseYear(req.getReleaseYear() != null ? req.getReleaseYear() : existing.getReleaseYear())
                .genres(req.getGenres() != null ? req.getGenres() : existing.getGenres())
                .ageRestriction(req.getAgeRestriction() != null ? req.getAgeRestriction() : existing.getAgeRestriction())
                .creatorId(existing.getCreatorId())
                .averageScore(existing.getAverageScore())
                .createdAt(existing.getCreatedAt()) // createdAt bleibt gleich
                .build();

        boolean ok = mediaRepository.update(updated);
        if (!ok) throw new ApiException(500, "Update fehlgeschlagen");

        // Nochmal aus DB lesen, damit man sicher die DB-Version zurückgibt
        return getById(mediaId);
    }

    /**
     * Delete: nur creator darf löschen.
     */
    public void delete(int userId, int mediaId) {
        Media existing = getById(mediaId);

        if (!existing.getCreatorId().equals(userId)) {
            throw ApiException.forbidden("Nur der Ersteller darf dieses Media löschen");
        }

        boolean ok = mediaRepository.delete(mediaId, userId);
        if (!ok) throw new ApiException(500, "Delete fehlgeschlagen");
    }
}
