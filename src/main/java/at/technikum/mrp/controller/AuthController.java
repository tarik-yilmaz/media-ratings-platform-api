package at.technikum.mrp.controller;

import at.technikum.mrp.dto.LoginRequest;
import at.technikum.mrp.dto.RegisterRequest;
import at.technikum.mrp.model.User;
import at.technikum.mrp.service.AuthService;
import at.technikum.mrp.util.ApiException;
import at.technikum.mrp.util.HttpUtil;
import at.technikum.mrp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void handleRegister(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            String body = HttpUtil.readBody(exchange);
            RegisterRequest req = JsonUtil.MAPPER.readValue(body, RegisterRequest.class);

            User created = authService.register(req);

            HttpUtil.sendJson(exchange, 201, Map.of(
                    "id", created.getId(),
                    "username", created.getUsername()
            ));

        } catch (ApiException e) {
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            String body = HttpUtil.readBody(exchange);
            LoginRequest req = JsonUtil.MAPPER.readValue(body, LoginRequest.class);

            String token = authService.login(req);

            HttpUtil.sendJson(exchange, 200, Map.of(
                    "token", token,
                    "tokenType", "Bearer"
            ));

        } catch (ApiException e) {
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }
}
