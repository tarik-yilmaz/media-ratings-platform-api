package at.technikum.mrp.controller;

import at.technikum.mrp.dto.RatingRequest;
import at.technikum.mrp.model.Rating;
import at.technikum.mrp.service.RatingService;
import at.technikum.mrp.service.TokenService;
import at.technikum.mrp.util.ApiException;
import at.technikum.mrp.util.HttpUtil;
import at.technikum.mrp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP-Controller für Rating-Endpoints unter /api/ratings
 *
 * Unterstützt:
 * - PUT    /api/ratings/{id}          (Rating updaten)
 * - DELETE /api/ratings/{id}          (Rating löschen)
 * - POST   /api/ratings/{id}/confirm  (Kommentar bestätigen)
 * - POST   /api/ratings/{id}/like     (Rating liken)
 *
 * Alles ist geschützt -> Authorization: Bearer <token>
 */
public class RatingController {

    private final RatingService ratingService;
    private final TokenService tokenService;

    public RatingController(RatingService ratingService, TokenService tokenService) {
        this.ratingService = ratingService;
        this.tokenService = tokenService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            // 1) Auth
            int userId = tokenService.requireUserIdFromAuthHeader(
                    exchange.getRequestHeaders().getFirst("Authorization")
            );

            // 2) Pfad normalisieren (Trailing Slash entfernen)
            String path = exchange.getRequestURI().getPath(); // z.B. /api/ratings/123/confirm
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            String method = exchange.getRequestMethod().toUpperCase();
            String[] parts = path.split("/"); // ["", "api", "ratings", "{id}", "confirm?"]

            // Erwartet mindestens /api/ratings/{id}
            if (parts.length < 4) {
                HttpUtil.sendEmpty(exchange, 404);
                return;
            }

            // /api/ratings/{id}
            int ratingId;
            try {
                ratingId = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                throw ApiException.badRequest("ratingId muss eine Zahl sein");
            }

            // 3) Subroutes: /confirm und /like
            if (parts.length == 5) {
                String action = parts[4];

                if ("confirm".equals(action)) {
                    if (!method.equals("POST")) {
                        HttpUtil.sendEmpty(exchange, 405);
                        return;
                    }

                    Rating updated = ratingService.confirmComment(userId, ratingId);
                    HttpUtil.sendJson(exchange, 200, toRatingJson(updated));
                    return;
                }

                if ("like".equals(action)) {
                    if (!method.equals("POST")) {
                        HttpUtil.sendEmpty(exchange, 405);
                        return;
                    }

                    Rating updated = ratingService.likeRating(userId, ratingId);
                    HttpUtil.sendJson(exchange, 200, Map.of(
                            "ratingId", updated.getId(),
                            "likesCount", updated.getLikesCount()
                    ));
                    return;
                }

                // unbekannte Subroute
                HttpUtil.sendEmpty(exchange, 404);
                return;
            }

            // 4) Nur exakt /api/ratings/{id} erlauben
            if (parts.length != 4) {
                HttpUtil.sendEmpty(exchange, 404);
                return;
            }

            // PUT /api/ratings/{id}
            if (method.equals("PUT")) {
                String body = HttpUtil.readBody(exchange);
                RatingRequest req = JsonUtil.MAPPER.readValue(body, RatingRequest.class);

                Rating updated = ratingService.updateRating(userId, ratingId, req);
                HttpUtil.sendJson(exchange, 200, toRatingJson(updated));
                return;
            }

            // DELETE /api/ratings/{id}
            if (method.equals("DELETE")) {
                ratingService.deleteRating(userId, ratingId);
                HttpUtil.sendEmpty(exchange, 204);
                return;
            }

            // alles andere: nicht erlaubt
            HttpUtil.sendEmpty(exchange, 405);

        } catch (ApiException e) {
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }

    private Map<String, Object> toRatingJson(Rating r) {
        return Map.of(
                "id", r.getId(),
                "mediaId", r.getMediaId(),
                "userId", r.getUserId(),
                "stars", r.getStars(),
                "comment", r.getComment(),
                "confirmed", r.getConfirmed(),
                "likesCount", r.getLikesCount(),
                "createdAt", r.getCreatedAt()
        );
    }
}
