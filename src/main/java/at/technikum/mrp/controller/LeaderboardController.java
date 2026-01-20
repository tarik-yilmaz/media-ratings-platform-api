package at.technikum.mrp.controller;

import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.HttpUtil;
import at.technikum.mrp.util.QueryUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if (!method.equals("GET")) {
            HttpUtil.sendEmpty(exchange, 405);
            return;
        }

        int limit = 10;
        try {
            String q = exchange.getRequestURI().getQuery();
            if (q != null) {
                Map<String, String> params = QueryUtil.parse(q);
                String s = params.get("limit");
                if (s != null) limit = Integer.parseInt(s);
            }
        } catch (Exception ignored) {}

        List<Map<String, Object>> data = userRepository.findLeaderboard(limit);
        HttpUtil.sendJson(exchange, 200, data);
    }
}
