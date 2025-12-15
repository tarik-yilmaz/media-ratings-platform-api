package at.technikum.mrp.service;

import at.technikum.mrp.dto.MediaRequest;
import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.util.ApiException;

import java.util.List;
import java.util.Optional;

public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Media create(int userId, MediaRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getTitle() == null || req.getTitle().trim().isEmpty())
            throw ApiException.badRequest("title ist erforderlich");
        if (req.getMediaType() == null || req.getMediaType().trim().isEmpty())
            throw ApiException.badRequest("mediaType ist erforderlich");

        Media media = Media.builder()
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .type(req.getMediaType())
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

    public Media getById(int id) {
        return mediaRepository.findById(id).orElseThrow(() -> ApiException.notFound("Media nicht gefunden"));
    }

    public List<Media> list(Optional<String> title,
                            Optional<String> genre,
                            Optional<String> mediaType,
                            Optional<Integer> releaseYear,
                            Optional<Integer> ageRestriction,
                            Optional<Double> minRating,
                            Optional<String> sortBy) {

        // für die Abgabe: ein brauchbarer Filter reicht
        return mediaRepository.findFiltered(
                title.orElse(null),
                genre.orElse(null),
                mediaType.orElse(null),
                releaseYear.orElse(null),
                ageRestriction.orElse(null),
                minRating.orElse(null),
                sortBy.orElse(null)
        );
    }

    public Media update(int userId, int mediaId, MediaRequest req) {
        Media existing = getById(mediaId);

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
                .createdAt(existing.getCreatedAt())
                .build();

        boolean ok = mediaRepository.update(updated);
        if (!ok) throw new ApiException(500, "Update fehlgeschlagen");
        return getById(mediaId);
    }

    public void delete(int userId, int mediaId) {
        Media existing = getById(mediaId);
        if (!existing.getCreatorId().equals(userId)) {
            throw ApiException.forbidden("Nur der Ersteller darf dieses Media löschen");
        }
        boolean ok = mediaRepository.delete(mediaId, userId);
        if (!ok) throw new ApiException(500, "Delete fehlgeschlagen");
    }
}
