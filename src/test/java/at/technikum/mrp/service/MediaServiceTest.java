package at.technikum.mrp.service;

import at.technikum.mrp.dto.MediaRequest;
import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.util.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MediaServiceTest {

    private MediaRepository mediaRepository;
    private MediaService mediaService;

    @BeforeEach
    void setup() {
        mediaRepository = mock(MediaRepository.class);
        mediaService = new MediaService(mediaRepository);
    }

    @Test
    void create_nullBody_throws400() {
        // Prüft: Body fehlt -> 400
        ApiException ex = assertThrows(ApiException.class, () -> mediaService.create(1, null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void create_missingTitle_throws400() {
        // Prüft: title fehlt -> 400
        MediaRequest req = new MediaRequest();
        req.setTitle(" ");
        req.setMediaType("MOVIE");

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.create(1, req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void create_missingMediaType_throws400() {
        // Prüft: mediaType fehlt -> 400
        MediaRequest req = new MediaRequest();
        req.setTitle("Interstellar");
        req.setMediaType(" ");

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.create(1, req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void create_repoReturnsNull_throws500() {
        // Prüft: repository.save() liefert null -> 500
        MediaRequest req = new MediaRequest();
        req.setTitle("Interstellar");
        req.setMediaType("MOVIE");

        when(mediaRepository.save(any(Media.class))).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.create(1, req));
        assertEquals(500, ex.getStatus());
    }

    @Test
    void getById_notFound_throws404() {
        // Prüft: Media nicht vorhanden -> 404
        when(mediaRepository.findById(99)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.getById(99));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void update_notCreator_throws403() {
        // Prüft: Ownership-Regel -> 403
        Media existing = Media.builder()
                .id(10).title("A").type("MOVIE").creatorId(111)
                .averageScore(0.0).createdAt(LocalDateTime.now())
                .build();

        when(mediaRepository.findById(10)).thenReturn(Optional.of(existing));

        MediaRequest req = new MediaRequest();
        req.setTitle("B");

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.update(222, 10, req));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void update_repoUpdateFalse_throws500() {
        // Prüft: repository.update() liefert false -> 500
        Media existing = Media.builder()
                .id(10).title("A").type("MOVIE").creatorId(1)
                .averageScore(0.0).createdAt(LocalDateTime.now())
                .build();

        when(mediaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(mediaRepository.update(any(Media.class))).thenReturn(false);

        MediaRequest req = new MediaRequest();
        req.setTitle("B");

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.update(1, 10, req));
        assertEquals(500, ex.getStatus());
    }

    @Test
    void delete_notCreator_throws403() {
        // Prüft: nur Creator darf löschen -> 403
        Media existing = Media.builder()
                .id(10).title("A").type("MOVIE").creatorId(111)
                .averageScore(0.0).createdAt(LocalDateTime.now())
                .build();

        when(mediaRepository.findById(10)).thenReturn(Optional.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.delete(222, 10));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void delete_repoDeleteFalse_throws500() {
        // Prüft: repository.delete() liefert false -> 500
        Media existing = Media.builder()
                .id(10).title("A").type("MOVIE").creatorId(1)
                .averageScore(0.0).createdAt(LocalDateTime.now())
                .build();

        when(mediaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(mediaRepository.delete(10, 1)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> mediaService.delete(1, 10));
        assertEquals(500, ex.getStatus());
    }

    @Test
    void list_delegatesToRepository() {
        // Prüft: list() ruft repository.findFiltered() mit denselben Parametern auf
        when(mediaRepository.findFiltered(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mediaService.list("ti", "action", "MOVIE", 2020, 16, 3.5, "score");

        verify(mediaRepository).findFiltered("ti", "action", "MOVIE", 2020, 16, 3.5, "score");
    }
}
