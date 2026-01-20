package at.technikum.mrp.service;

import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.FavoritesRepository;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.util.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FavoritesServiceTest {

    private FavoritesRepository favoritesRepository;
    private MediaRepository mediaRepository;
    private FavoritesService favoritesService;

    @BeforeEach
    void setup() {
        favoritesRepository = mock(FavoritesRepository.class);
        mediaRepository = mock(MediaRepository.class);
        favoritesService = new FavoritesService(favoritesRepository, mediaRepository);
    }

    @Test
    void addFavorite_mediaNotFound_throws404() {
        // Prüft: Media muss existieren -> 404 wenn nicht
        when(mediaRepository.findById(10)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> favoritesService.addFavorite(1, 10));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void addFavorite_alreadyFavorite_throws409() {
        // Prüft: wenn bereits Favorit -> 409
        when(mediaRepository.findById(10)).thenReturn(Optional.of(mock(Media.class)));
        when(favoritesRepository.addFavorite(1, 10)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> favoritesService.addFavorite(1, 10));
        assertEquals(409, ex.getStatus());
    }

    @Test
    void addFavorite_success_noException() {
        // Prüft: ok -> keine Exception
        when(mediaRepository.findById(10)).thenReturn(Optional.of(mock(Media.class)));
        when(favoritesRepository.addFavorite(1, 10)).thenReturn(true);

        assertDoesNotThrow(() -> favoritesService.addFavorite(1, 10));
    }

    @Test
    void removeFavorite_notInFavorites_throws404() {
        // Prüft: wenn nicht Favorit -> 404
        when(mediaRepository.findById(10)).thenReturn(Optional.of(mock(Media.class)));
        when(favoritesRepository.removeFavorite(1, 10)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> favoritesService.removeFavorite(1, 10));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void listFavorites_returnsRepositoryList() {
        // Prüft: listFavorites gibt Repository-Result zurück
        when(favoritesRepository.findFavoritesByUserId(1)).thenReturn(List.of(mock(Media.class)));

        List<Media> out = favoritesService.listFavorites(1);
        assertEquals(1, out.size());
    }
}
