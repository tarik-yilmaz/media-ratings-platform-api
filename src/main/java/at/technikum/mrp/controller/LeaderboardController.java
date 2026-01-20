package at.technikum.mrp.controller;

import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Public Endpoint:
 * - GET /api/leaderboard
 */
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            List<Map<String, Object>> top = userRepository.findLeaderboard(10);
            HttpUtil.sendJson(exchange, 200, top);

        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }
}
