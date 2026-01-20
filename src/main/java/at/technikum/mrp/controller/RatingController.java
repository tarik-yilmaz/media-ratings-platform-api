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
 * Controller für Ratings
 * Kümmert sich um Update/Delete sowie /like und /confirm.
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
            int userId = tokenService.requireUserIdFromAuthHeader(
                    exchange.getRequestHeaders().getFirst("Authorization")
            );

            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

            String method = exchange.getRequestMethod().toUpperCase();
            String[] parts = path.split("/"); // ["", "api", "ratings", "{id}", "like|confirm"]

            if (parts.length < 4) {
                HttpUtil.sendEmpty(exchange, 404);
                return;
            }

            int ratingId;
            try {
                ratingId = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ex) {
                throw ApiException.badRequest("ratingId muss eine Zahl sein");
            }

            // /api/ratings/{id}
            if (parts.length == 4) {
                if (method.equals("PUT")) {
                    String body = HttpUtil.readBody(exchange);
                    RatingRequest req = JsonUtil.MAPPER.readValue(body, RatingRequest.class);

                    Rating updated = ratingService.updateRating(userId, ratingId, req);
                    HttpUtil.sendJson(exchange, 200, toRatingJson(updated));
                    return;
                }

                if (method.equals("DELETE")) {
                    ratingService.deleteRating(userId, ratingId);
                    HttpUtil.sendEmpty(exchange, 204);
                    return;
                }

                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            // /api/ratings/{id}/like oder /confirm
            if (parts.length == 5) {
                String action = parts[4];

                if (method.equals("POST") && action.equals("like")) {
                    Rating liked = ratingService.likeRating(userId, ratingId);
                    HttpUtil.sendJson(exchange, 200, toRatingJson(liked));
                    return;
                }

                if (method.equals("POST") && action.equals("confirm")) {
                    Rating confirmed = ratingService.confirmComment(userId, ratingId);
                    HttpUtil.sendJson(exchange, 200, toRatingJson(confirmed));
                    return;
                }
            }

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
