package at.technikum.mrp.service;

import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {

    private MediaRepository mediaRepository;
    private RatingRepository ratingRepository;
    private RecommendationService recommendationService;

    @BeforeEach
    void setup() {
        mediaRepository = mock(MediaRepository.class);
        ratingRepository = mock(RatingRepository.class);
        recommendationService = new RecommendationService(mediaRepository, ratingRepository);
    }

    @Test
    void recommendForUser_fallbackWhenNoLiked_returnsTopRated() {
        // Prüft: wenn liked leer -> fallback topRated
        when(mediaRepository.findHighlyRatedByUser(1, 4)).thenReturn(List.of());
        when(mediaRepository.findNotRatedByUser(1)).thenReturn(List.of()); // wird vorher geladen
        when(mediaRepository.findTopRated(2)).thenReturn(List.of(
                Media.builder().id(10).title("A").type("MOVIE").averageScore(4.5).build(),
                Media.builder().id(11).title("B").type("MOVIE").averageScore(4.0).build()
        ));

        List<Map<String, Object>> recs = recommendationService.recommendForUser(1, 2);

        assertEquals(2, recs.size());
        assertEquals("fallback_topRated", recs.get(0).get("reason"));
    }

    @Test
    void recommendForUser_respectsLimit() {
        // Prüft: Ergebnisliste überschreitet limit nicht
        when(mediaRepository.findHighlyRatedByUser(1, 4)).thenReturn(List.of(
                Media.builder().id(1).title("Liked").type("MOVIE").genres(List.of("action")).ageRestriction(16).build()
        ));
        when(mediaRepository.findNotRatedByUser(1)).thenReturn(List.of(
                Media.builder().id(10).title("C1").type("MOVIE").genres(List.of("action")).ageRestriction(16).build(),
                Media.builder().id(11).title("C2").type("MOVIE").genres(List.of("action")).ageRestriction(16).build(),
                Media.builder().id(12).title("C3").type("MOVIE").genres(List.of("action")).ageRestriction(16).build()
        ));

        List<Map<String, Object>> recs = recommendationService.recommendForUser(1, 2);
        assertEquals(2, recs.size());
    }

    @Test
    void recommendForUser_prefersFavoriteGenre() {
        // Prüft: Kandidat mit Lieblingsgenre bekommt Bonus und landet vorne
        when(mediaRepository.findHighlyRatedByUser(1, 4)).thenReturn(List.of(
                Media.builder().id(1).title("Liked1").type("MOVIE").genres(List.of("action")).ageRestriction(16).build(),
                Media.builder().id(2).title("Liked2").type("MOVIE").genres(List.of("action")).ageRestriction(16).build()
        ));

        Media best = Media.builder().id(10).title("Best").type("GAME").genres(List.of("action")).ageRestriction(16).build();
        Media worse = Media.builder().id(11).title("Worse").type("GAME").genres(List.of("drama")).ageRestriction(16).build();

        when(mediaRepository.findNotRatedByUser(1)).thenReturn(List.of(worse, best));

        List<Map<String, Object>> recs = recommendationService.recommendForUser(1, 2);

        assertEquals(2, recs.size());
        assertEquals(10, recs.get(0).get("id")); // "Best" sollte vorne sein
    }
}
