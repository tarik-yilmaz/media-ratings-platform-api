package at.technikum.mrp.controller;

import at.technikum.mrp.dto.UserProfileUpdate;
import at.technikum.mrp.model.Media;
import at.technikum.mrp.model.Rating;
import at.technikum.mrp.model.User;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.service.MediaService;
import at.technikum.mrp.service.RatingService;
import at.technikum.mrp.service.TokenService;
import at.technikum.mrp.service.RecommendationService;
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
 * User-Endpunkte (spec-nah):
 * - GET/PUT /api/users/{username}/profile          (Profil ansehen/ändern)
 * - GET     /api/users/{username}/ratings          (eigene Rating-History)
 * - GET     /api/users/{username}/recommendations  (Empfehlungen, optional ?limit=10)
 *
 * Zugriff nur auf das eigene Profil -> Username aus URL muss zum Token passen.
 */
public class UserController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final RatingService ratingService;
    private final MediaService mediaService;
    private final RecommendationService recommendationService;

    public UserController(TokenService tokenService,
                          UserRepository userRepository,
                          RatingService ratingService,
                          MediaService mediaService,
                          RecommendationService recommendationService) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.ratingService = ratingService;
        this.mediaService = mediaService;
        this.recommendationService = recommendationService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            int tokenUserId = tokenService.requireUserIdFromAuthHeader(
                    exchange.getRequestHeaders().getFirst("Authorization")
            );

            String path = exchange.getRequestURI().getPath();   // z.B. /api/users/alice/profile
            String method = exchange.getRequestMethod().toUpperCase();

            String[] parts = path.split("/"); // ["", "api", "users", "{username}", "profile|ratings|recommendations"]

            // Wir erwarten: /api/users/{username}/{action}
            if (parts.length != 5) {
                HttpUtil.sendEmpty(exchange, 404);
                return;
            }

            String username = parts[3];
            String action = parts[4];

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> ApiException.notFound("User nicht gefunden"));

            // Security: nur der User selbst darf sein Profil/History sehen
            if (!user.getId().equals(tokenUserId)) {
                throw ApiException.forbidden("Kein Zugriff auf fremde User-Daten");
            }

            if ("profile".equals(action)) {
                handleProfile(exchange, method, user);
                return;
            }

            if ("recommendations".equals(action)) {
                if (!method.equals("GET")) {
                    HttpUtil.sendEmpty(exchange, 405);
                    return;
                }

                // optional: ?limit=10
                int limit = 10;
                try {
                    String q = exchange.getRequestURI().getQuery();
                    if (q != null) {
                        Map<String, String> params = QueryUtil.parse(q);
                        String s = params.get("limit");
                        if (s != null) limit = Integer.parseInt(s);
                    }
                } catch (Exception ignored) {}

                List<Map<String, Object>> recs = recommendationService.recommendForUser(user.getId(), limit);
                HttpUtil.sendJson(exchange, 200, recs);
                return;
            }

            if ("ratings".equals(action)) {
                if (!method.equals("GET")) {
                    HttpUtil.sendEmpty(exchange, 405);
                    return;
                }
                handleRatingHistory(exchange, user.getId());
                return;
            }

            HttpUtil.sendEmpty(exchange, 404);

        } catch (ApiException e) {
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }

    private void handleProfile(HttpExchange exchange, String method, User user) throws IOException {
        if (method.equals("GET")) {
            HttpUtil.sendJson(exchange, 200, Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "createdAt", user.getCreatedAt(),
                    "totalRatings", user.getTotalRatings(),
                    "averageRating", user.getAverageRating()
            ));
            return;
        }

        if (method.equals("PUT")) {
            String body = HttpUtil.readBody(exchange);
            UserProfileUpdate req = JsonUtil.MAPPER.readValue(body, UserProfileUpdate.class);

            // wir erlauben hier nur Email zu ändern
            String newEmail = (req == null) ? null : req.getEmail();

            boolean ok = userRepository.updateEmail(user.getId(), newEmail);
            if (!ok) throw new ApiException(500, "Profil konnte nicht aktualisiert werden");

            User updated = userRepository.findById(user.getId())
                    .orElseThrow(() -> new ApiException(500, "User konnte nicht neu geladen werden"));

            HttpUtil.sendJson(exchange, 200, Map.of(
                    "id", updated.getId(),
                    "username", updated.getUsername(),
                    "email", updated.getEmail(),
                    "createdAt", updated.getCreatedAt(),
                    "totalRatings", updated.getTotalRatings(),
                    "averageRating", updated.getAverageRating()
            ));
            return;
        }

        HttpUtil.sendEmpty(exchange, 405);
    }

    private void handleRatingHistory(HttpExchange exchange, int userId) throws IOException {
        List<Rating> ratings = ratingService.listByUserId(userId);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Rating r : ratings) {
            String mediaTitle = null;
            try {
                Media m = mediaService.getById(r.getMediaId());
                mediaTitle = m.getTitle();
            } catch (Exception ignored) {}

            // In der eigenen History sieht man den Kommentar immer (auch unconfirmed)
            out.add(Map.of(
                    "id", r.getId(),
                    "mediaId", r.getMediaId(),
                    "mediaTitle", mediaTitle,
                    "stars", r.getStars(),
                    "comment", r.getComment(),
                    "confirmed", r.getConfirmed(),
                    "likesCount", r.getLikesCount(),
                    "createdAt", r.getCreatedAt()
            ));
        }

        HttpUtil.sendJson(exchange, 200, out);
    }
}
