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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MediaController {

    private final MediaService mediaService;
    private final TokenService tokenService;

    public MediaController(MediaService mediaService, TokenService tokenService) {
        this.mediaService = mediaService;
        this.tokenService = tokenService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            int userId = tokenService.requireUserIdFromAuthHeader(exchange.getRequestHeaders().getFirst("Authorization"));

            String path = exchange.getRequestURI().getPath(); // /api/media or /api/media/{id}
            String method = exchange.getRequestMethod().toUpperCase();

            String[] parts = path.split("/");
            boolean hasId = parts.length == 4; // ["", "api", "media", "{id}"]

            if (!hasId) {
                // Collection: /api/media
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

            // Item: /api/media/{id}
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

        Optional<String> title = Optional.ofNullable(q.get("title"));
        Optional<String> genre = Optional.ofNullable(q.get("genre"));
        Optional<String> mediaType = Optional.ofNullable(q.get("mediaType"));
        Optional<Integer> releaseYear = parseIntOpt(q.get("releaseYear"));
        Optional<Integer> ageRestriction = parseIntOpt(q.get("ageRestriction"));
        Optional<Double> rating = parseDoubleOpt(q.get("rating"));
        Optional<String> sortBy = Optional.ofNullable(q.get("sortBy"));

        List<Media> list = mediaService.list(title, genre, mediaType, releaseYear, ageRestriction, rating, sortBy);
        HttpUtil.sendJson(exchange, 200, list.stream().map(this::toMediaJson).toList());
    }

    private Optional<Integer> parseIntOpt(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try { return Optional.of(Integer.parseInt(s)); } catch (Exception e) { return Optional.empty(); }
    }

    private Optional<Double> parseDoubleOpt(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try { return Optional.of(Double.parseDouble(s)); } catch (Exception e) { return Optional.empty(); }
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
