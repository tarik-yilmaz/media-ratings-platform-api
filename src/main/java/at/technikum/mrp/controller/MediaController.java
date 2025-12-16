package at.technikum.mrp.controller;

import at.technikum.mrp.dto.MediaRequest;
import at.technikum.mrp.model.Media;
import at.technikum.mrp.service.MediaService;
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
 *
 * Alle Media-Endpoints sind geschützt -> Authorization: Bearer <token> ist Pflicht.
 */
public class MediaController {

    private final MediaService mediaService;
    private final TokenService tokenService;

    public MediaController(MediaService mediaService, TokenService tokenService) {
        this.mediaService = mediaService;
        this.tokenService = tokenService;
    }

    /**
     * Zentraler Handler für alle Requests unter /api/media.
     * Wir unterscheiden hier anhand von Pfad und HTTP-Methode, was gemacht werden soll.
     */
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Auth check: wenn Token fehlt/ungültig -> ApiException (401)
            int userId = tokenService.requireUserIdFromAuthHeader(
                    exchange.getRequestHeaders().getFirst("Authorization")
            );

            String path = exchange.getRequestURI().getPath(); // z.B. /api/media oder /api/media/123
            String method = exchange.getRequestMethod().toUpperCase();

            // Pfad in Teile splitten: ["", "api", "media", "{id}"]
            String[] parts = path.split("/");
            boolean hasId = parts.length == 4;

            if (!hasId) {
                // Collection-Endpoint: /api/media
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

            // Item-Endpoint: /api/media/{id}
            int mediaId;
            try {
                mediaId = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ex) {
                // z.B. /api/media/abc
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

                // Service checkt z.B. ownership (nur creator darf updaten)
                Media updated = mediaService.update(userId, mediaId, req);
                HttpUtil.sendJson(exchange, 200, toMediaJson(updated));
                return;
            }

            if (method.equals("DELETE")) {
                // Service checkt ownership (nur creator darf löschen)
                mediaService.delete(userId, mediaId);
                HttpUtil.sendEmpty(exchange, 204);
                return;
            }

            HttpUtil.sendEmpty(exchange, 405);

        } catch (ApiException e) {
            // geplante Fehler (400/401/403/404/...)
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // ungeplante Fehler
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }

    /**
     * POST /api/media
     * Liest JSON Body, baut MediaRequest und ruft Service für Create auf.
     */
    private void handleCreate(HttpExchange exchange, int userId) throws IOException {
        String body = HttpUtil.readBody(exchange);
        MediaRequest req = JsonUtil.MAPPER.readValue(body, MediaRequest.class);

        // creatorId kommt aus dem Token (userId)
        Media created = mediaService.create(userId, req);
        HttpUtil.sendJson(exchange, 201, toMediaJson(created));
    }

    /**
     * GET /api/media
     */
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

    /**
     * Baut ein "API-Response JSON" aus einem Media-Objekt.
     * (Wir schicken hier bewusst nur Felder, die für die API relevant sind.)
     */
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
