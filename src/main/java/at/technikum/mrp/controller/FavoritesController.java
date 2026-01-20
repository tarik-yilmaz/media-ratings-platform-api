package at.technikum.mrp.controller;

import at.technikum.mrp.model.Media;
import at.technikum.mrp.service.FavoritesService;
import at.technikum.mrp.service.TokenService;
import at.technikum.mrp.util.ApiException;
import at.technikum.mrp.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesController {

    private final FavoritesService favoritesService;
    private final TokenService tokenService;

    public FavoritesController(FavoritesService favoritesService, TokenService tokenService) {
        this.favoritesService = favoritesService;
        this.tokenService = tokenService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            int userId = tokenService.requireUserIdFromAuthHeader(
                    exchange.getRequestHeaders().getFirst("Authorization")
            );

            String method = exchange.getRequestMethod().toUpperCase();
            if (!method.equals("GET")) {
                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            List<Media> favorites = favoritesService.listFavorites(userId);

            List<Map<String, Object>> out = new ArrayList<>();
            for (Media m : favorites) {
                out.add(Map.of(
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
                ));
            }

            HttpUtil.sendJson(exchange, 200, out);

        } catch (ApiException e) {
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }
}
