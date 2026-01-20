package at.technikum.mrp.service;

import at.technikum.mrp.dto.RatingRequest;
import at.technikum.mrp.model.Rating;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.repository.RatingRepository;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RatingServiceTest {

    private RatingRepository ratingRepository;
    private MediaRepository mediaRepository;
    private UserRepository userRepository;
    private RatingService ratingService;

    @BeforeEach
    void setup() {
        ratingRepository = mock(RatingRepository.class);
        mediaRepository = mock(MediaRepository.class);
        userRepository = mock(UserRepository.class);
        ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);
    }

    @Test
    void rateMedia_invalidStars_throws400() {
        // Prüft: stars außerhalb 1..5 -> 400
        RatingRequest req = new RatingRequest();
        req.setStars(0);

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.rateMedia(1, 10, req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void rateMedia_mediaNotFound_throws404() {
        // Prüft: Media muss existieren -> 404 wenn nicht
        when(mediaRepository.findById(10)).thenReturn(Optional.empty());

        RatingRequest req = new RatingRequest();
        req.setStars(5);

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.rateMedia(1, 10, req));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void rateMedia_duplicateRating_throws409() {
        // Prüft: pro User nur 1 Rating pro Media -> 409
        when(mediaRepository.findById(10)).thenReturn(Optional.of(mock(at.technikum.mrp.model.Media.class)));
        when(ratingRepository.findByMediaAndUser(10, 1)).thenReturn(Optional.of(mock(Rating.class)));

        RatingRequest req = new RatingRequest();
        req.setStars(4);

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.rateMedia(1, 10, req));
        assertEquals(409, ex.getStatus());
    }

    @Test
    void updateRating_notOwner_throws403() {
        // Prüft: nur Ersteller darf updaten -> 403
        Rating existing = Rating.builder().id(5).mediaId(10).userId(999).stars(3)
                .confirmed(false).likesCount(0).createdAt(LocalDateTime.now()).build();

        when(ratingRepository.findById(5)).thenReturn(Optional.of(existing));

        RatingRequest req = new RatingRequest();
        req.setStars(4);

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.updateRating(1, 5, req));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void confirmComment_notOwner_throws403() {
        // Prüft: nur Ersteller darf bestätigen -> 403
        Rating existing = Rating.builder().id(5).mediaId(10).userId(999).stars(3)
                .confirmed(false).likesCount(0).createdAt(LocalDateTime.now()).build();

        when(ratingRepository.findById(5)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.confirmComment(1, 5));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void likeRating_ownRating_throws403() {
        // Prüft: eigenes Rating darf man nicht liken -> 403
        Rating existing = Rating.builder().id(5).mediaId(10).userId(1).stars(3)
                .confirmed(true).likesCount(0).createdAt(LocalDateTime.now()).build();

        when(ratingRepository.findById(5)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.likeRating(1, 5));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void likeRating_duplicate_throws409() {
        // Prüft: 1 Like pro User pro Rating -> 409 bei Doppel-Like
        Rating existing = Rating.builder().id(5).mediaId(10).userId(2).stars(3)
                .confirmed(true).likesCount(0).createdAt(LocalDateTime.now()).build();

        when(ratingRepository.findById(5)).thenReturn(Optional.of(existing));
        when(ratingRepository.likeRating(5, 1)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> ratingService.likeRating(1, 5));
        assertEquals(409, ex.getStatus());
    }

    @Test
    void deleteRating_triggersStatsUpdate() {
        // Prüft: nach delete werden media avg + user stats aktualisiert
        Rating existing = Rating.builder().id(5).mediaId(10).userId(1).stars(3)
                .confirmed(true).likesCount(0).createdAt(LocalDateTime.now()).build();

        when(ratingRepository.findById(5)).thenReturn(Optional.of(existing));
        when(ratingRepository.delete(5, 1)).thenReturn(true);
        when(mediaRepository.updateAverageScore(10)).thenReturn(true);
        when(userRepository.updateUserStatistics(1)).thenReturn(true);

        assertDoesNotThrow(() -> ratingService.deleteRating(1, 5));

        verify(mediaRepository).updateAverageScore(10);
        verify(userRepository).updateUserStatistics(1);
    }
}
