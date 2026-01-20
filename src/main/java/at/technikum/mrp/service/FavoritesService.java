package at.technikum.mrp.service;

import at.technikum.mrp.model.Media;
import at.technikum.mrp.repository.FavoritesRepository;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.util.ApiException;

import java.util.List;

public class FavoritesService {

    private final FavoritesRepository favoritesRepository;
    private final MediaRepository mediaRepository;

    public FavoritesService(FavoritesRepository favoritesRepository, MediaRepository mediaRepository) {
        this.favoritesRepository = favoritesRepository;
        this.mediaRepository = mediaRepository;
    }

    public void addFavorite(int userId, int mediaId) {
        // Media muss existieren (saubere API)
        mediaRepository.findById(mediaId)
                .orElseThrow(() -> ApiException.notFound("Media nicht gefunden"));

        boolean ok = favoritesRepository.addFavorite(userId, mediaId);
        if (!ok) {
            throw ApiException.conflict("Dieses Media ist bereits als Favorit markiert");
        }
    }

    public void removeFavorite(int userId, int mediaId) {
        // Media muss existieren (optional, aber sauber)
        mediaRepository.findById(mediaId)
                .orElseThrow(() -> ApiException.notFound("Media nicht gefunden"));

        boolean ok = favoritesRepository.removeFavorite(userId, mediaId);
        if (!ok) {
            throw ApiException.notFound("Dieses Media ist nicht in deinen Favoriten");
        }
    }

    public List<Media> listFavorites(int userId) {
        return favoritesRepository.findFavoritesByUserId(userId);
    }
}
