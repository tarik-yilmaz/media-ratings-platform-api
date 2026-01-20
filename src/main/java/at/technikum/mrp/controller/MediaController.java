package at.technikum.mrp.controller;

import at.technikum.mrp.dto.MediaRequest;
import at.technikum.mrp.dto.RatingRequest;
import at.technikum.mrp.model.Media;
import at.technikum.mrp.model.Rating;
import at.technikum.mrp.service.FavoritesService;
import at.technikum.mrp.service.MediaService;
import at.technikum.mrp.service.RatingService;
import at.technikum.mrp.service.TokenService;
import at.technikum.mrp.util.ApiException;
import at.technikum.mrp.util.HttpUtil;
import at.technikum.mrp.util.JsonUtil;
import at.technikum.mrp.util.QueryUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP-Controller für Media-Endpoints.
 * Unterstützt:
 * - GET  /api/media              (Liste + Filter)
 * - POST /api/media              (Media erstellen)
 * - GET  /api/media/{id}         (ein Media holen)
 * - PUT  /api/media/{id}         (Media updaten)
 * - DELETE /api/media/{id}       (Media löschen)
 * - POST /api/media/{id}/rate    (Media bewerten)
 *
 * Alle Media-Endpoints sind geschützt -> Authorization: Bearer <token> ist Pflicht.
 */
public class MediaController {

    private final MediaService mediaService;
    private final TokenService tokenService;
    private final RatingService ratingService;
    private final FavoritesService favoritesService;

    public MediaController(MediaService mediaService, TokenService tokenService, RatingService ratingService, FavoritesService favoritesService) {
        this.mediaService = mediaService;
        this.tokenService = tokenService;
        this.ratingService = ratingService;
        this.favoritesService = favoritesService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            int userId = tokenService.requireUserIdFromAuthHeader(
                    exchange.getRequestHeaders().getFirst("Authorization")
            );

            String path = exchange.getRequestURI().getPath(); // z.B. /api/media oder /api/media/123 oder /api/media/123/rate
            String method = exchange.getRequestMethod().toUpperCase();

            String[] parts = path.split("/");

            // -----------------------------
            // Spezialfall: /api/media/{id}/rate
            // -----------------------------
            if (parts.length == 5 && "rate".equals(parts[4])) {
                int mediaId;
                try {
                    mediaId = Integer.parseInt(parts[3]);
                } catch (NumberFormatException ex) {
                    throw ApiException.badRequest("mediaId muss eine Zahl sein");
                }

                if (!method.equals("POST")) {
                    HttpUtil.sendEmpty(exchange, 405);
                    return;
                }

                String body = HttpUtil.readBody(exchange);
                RatingRequest req = JsonUtil.MAPPER.readValue(body, RatingRequest.class);

                Rating created = ratingService.rateMedia(userId, mediaId, req);

                HttpUtil.sendJson(exchange, 201, Map.of(
                        "id", created.getId(),
                        "mediaId", created.getMediaId(),
                        "userId", created.getUserId(),
                        "stars", created.getStars(),
                        "comment", created.getComment(),
                        "confirmed", created.getConfirmed(),
                        "likesCount", created.getLikesCount(),
                        "createdAt", created.getCreatedAt()
                ));
                return;
            }

            // Spezialfall: /api/media/{id}/ratings
            if (parts.length == 5 && "ratings".equals(parts[4])) {
                int mediaId;
                try {
                    mediaId = Integer.parseInt(parts[3]);
                } catch (NumberFormatException ex) {
                    throw ApiException.badRequest("mediaId muss eine Zahl sein");
                }

                if (!method.equals("GET")) {
                    HttpUtil.sendEmpty(exchange, 405);
                    return;
                }

                // 404 wenn Media nicht existiert (für saubere API)
                mediaService.getById(mediaId);

                List<Rating> ratings = ratingService.listByMediaId(mediaId);

                // Kommentar-Regel:
                // - öffentlich nur wenn confirmed == true
                // - der Autor selbst sieht seinen Kommentar immer (auch unconfirmed)
                List<Map<String, Object>> out = new ArrayList<>();
                for (Rating r : ratings) {
                    String commentOut = null;
                    if (Boolean.TRUE.equals(r.getConfirmed()) || r.getUserId().equals(userId)) {
                        commentOut = r.getComment();
                    }

                    out.add(Map.of(
                            "id", r.getId(),
                            "mediaId", r.getMediaId(),
                            "userId", r.getUserId(),
                            "stars", r.getStars(),
                            "comment", commentOut,
                            "confirmed", r.getConfirmed(),
                            "likesCount", r.getLikesCount(),
                            "createdAt", r.getCreatedAt()
                    ));
                }

                HttpUtil.sendJson(exchange, 200, out);
                return;
            }

            // Spezialfall: /api/media/{id}/favorite
            if (parts.length == 5 && "favorite".equals(parts[4])) {
                int mediaId;
                try {
                    mediaId = Integer.parseInt(parts[3]);
                } catch (NumberFormatException ex) {
                    throw ApiException.badRequest("mediaId muss eine Zahl sein");
                }

                if (method.equals("POST")) {
                    favoritesService.addFavorite(userId, mediaId);
                    HttpUtil.sendJson(exchange, 201, Map.of("message", "Favorit hinzugefügt"));
                    return;
                }

                if (method.equals("DELETE")) {
                    favoritesService.removeFavorite(userId, mediaId);
                    HttpUtil.sendEmpty(exchange, 204);
                    return;
                }

                HttpUtil.sendEmpty(exchange, 405);
                return;
            }


            // Normalfall: /api/media oder /api/media/{id}
            boolean hasId = parts.length == 4;

            if (!hasId) {
                if (method.equals("GET")) {
                    handleList(exchange);
                    return;
                }
                if (method.equals("POST")) {
                    handleCreate(exchange, userId);
                    return;
                }

                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            // /api/media/{id}
            int mediaId;
            try {
                mediaId = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ex) {
                throw ApiException.badRequest("mediaId muss eine Zahl sein");
            }

            if (method.equals("GET")) {
                Media m = mediaService.getById(mediaId);
                HttpUtil.sendJson(exchange, 200, toMediaJson(m));
                return;
            }

            if (method.equals("PUT")) {
                String body = HttpUtil.readBody(exchange);
                MediaRequest req = JsonUtil.MAPPER.readValue(body, MediaRequest.class);

                Media updated = mediaService.update(userId, mediaId, req);
                HttpUtil.sendJson(exchange, 200, toMediaJson(updated));
                return;
            }

            if (method.equals("DELETE")) {
                mediaService.delete(userId, mediaId);
                HttpUtil.sendEmpty(exchange, 204);
                return;
            }

            HttpUtil.sendEmpty(exchange, 405);

        } catch (ApiException e) {
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }

    private void handleCreate(HttpExchange exchange, int userId) throws IOException {
        String body = HttpUtil.readBody(exchange);
        MediaRequest req = JsonUtil.MAPPER.readValue(body, MediaRequest.class);

        Media created = mediaService.create(userId, req);
        HttpUtil.sendJson(exchange, 201, toMediaJson(created));
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, String> q = QueryUtil.parse(exchange.getRequestURI().getQuery());

        String title = q.get("title");
        String genre = q.get("genre");
        String mediaType = q.get("mediaType");
        Integer releaseYear = parseIntOrNull(q.get("releaseYear"));
        Integer ageRestriction = parseIntOrNull(q.get("ageRestriction"));
        Double rating = parseDoubleOrNull(q.get("rating"));
        String sortBy = q.get("sortBy");

        List<Media> list = mediaService.list(title, genre, mediaType, releaseYear, ageRestriction, rating, sortBy);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Media m : list) {
            out.add(toMediaJson(m));
        }

        HttpUtil.sendJson(exchange, 200, out);
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private Map<String, Object> toMediaJson(Media m) {
        return Map.of(
                "id", m.getId(),
                "title", m.getTitle(),
                "description", m.getDescription(),
                "mediaType", m.getType(),
                "releaseYear", m.getReleaseYear(),
                "genres", m.getGenres(),
                "ageRestriction", m.getAgeRestriction(),
                "creatorId", m.getCreatorId(),
                "averageScore", m.getAverageScore(),
                "createdAt", m.getCreatedAt()
        );
    }
}
