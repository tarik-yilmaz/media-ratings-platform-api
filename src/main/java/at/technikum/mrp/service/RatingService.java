package at.technikum.mrp.service;

import at.technikum.mrp.dto.RatingRequest;
import at.technikum.mrp.model.Rating;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.repository.RatingRepository;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.ApiException;

import java.util.List;

/**
 * Business-Logik rund um Ratings.
 * Macht Validierung, Ownership-Checks und triggert Updates von Statistiken (Average Scores).
 */
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository,
                         MediaRepository mediaRepository,
                         UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    public Rating rateMedia(int userId, int mediaId, RatingRequest req) {
        validate(req);

        // Media muss existieren
        mediaRepository.findById(mediaId)
                .orElseThrow(() -> ApiException.notFound("Media nicht gefunden"));

        // pro User nur 1 Rating pro Media
        if (ratingRepository.findByMediaAndUser(mediaId, userId).isPresent()) {
            throw ApiException.conflict("Du hast dieses Media bereits bewertet");
        }

        Rating toSave = Rating.builder()
                .mediaId(mediaId)
                .userId(userId)
                .stars(req.getStars())
                .comment(req.getComment())
                .confirmed(false)
                .likesCount(0)
                .build();

        Rating saved = ratingRepository.save(toSave);
        if (saved == null) throw new ApiException(500, "Rating konnte nicht gespeichert werden");

        afterRatingChanged(userId, mediaId);
        return saved;
    }

    public Rating updateRating(int userId, int ratingId, RatingRequest req) {
        validate(req);

        Rating existing = ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));

        if (!existing.getUserId().equals(userId)) {
            throw ApiException.forbidden("Nur der Ersteller darf dieses Rating ändern");
        }

        Rating updated = Rating.builder()
                .id(existing.getId())
                .mediaId(existing.getMediaId())
                .userId(existing.getUserId())
                .stars(req.getStars())
                .comment(req.getComment())
                .confirmed(existing.getConfirmed())
                .likesCount(existing.getLikesCount())
                .createdAt(existing.getCreatedAt())
                .build();

        boolean ok = ratingRepository.update(updated);
        if (!ok) throw new ApiException(500, "Update fehlgeschlagen");

        afterRatingChanged(userId, existing.getMediaId());

        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));
    }

    public void deleteRating(int userId, int ratingId) {
        Rating existing = ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));

        if (!existing.getUserId().equals(userId)) {
            throw ApiException.forbidden("Nur der Ersteller darf dieses Rating löschen");
        }

        boolean ok = ratingRepository.delete(ratingId, userId);
        if (!ok) throw new ApiException(500, "Delete fehlgeschlagen");

        afterRatingChanged(userId, existing.getMediaId());
    }

    public Rating confirmComment(int userId, int ratingId) {
        Rating existing = ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));

        if (!existing.getUserId().equals(userId)) {
            throw ApiException.forbidden("Nur der Ersteller darf den Kommentar bestätigen");
        }

        boolean ok = ratingRepository.confirmComment(ratingId, userId);
        if (!ok) throw new ApiException(500, "Bestätigung fehlgeschlagen");

        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));
    }

    public Rating likeRating(int userId, int ratingId) {
        Rating existing = ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));

        // Spezifikation: "like other users' ratings"
        if (existing.getUserId().equals(userId)) {
            throw ApiException.forbidden("Du kannst dein eigenes Rating nicht liken");
        }

        boolean ok = ratingRepository.addLike(ratingId, userId);
        if (!ok) {
            throw ApiException.conflict("Du hast dieses Rating bereits geliked");
        }

        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> ApiException.notFound("Rating nicht gefunden"));
    }

    public List<Rating> listByMediaId(int mediaId) {
        return ratingRepository.findByMediaId(mediaId);
    }

    private void validate(RatingRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getStars() == null || req.getStars() < 1 || req.getStars() > 5) {
            throw ApiException.badRequest("stars muss zwischen 1 und 5 sein");
        }
        // comment ist optional
    }

    public List<Rating> listByUserId(int userId) {
        return ratingRepository.findByUserId(userId);
    }


    private void afterRatingChanged(int userId, int mediaId) {
        mediaRepository.updateAverageScore(mediaId);
        userRepository.updateUserStatistics(userId);
    }
}
