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

/**
 * HTTP-Controller für Authentifizierung:
 * - /users/register (User erstellen)
 * - /users/login (Token holen)
 *
 * Controller nur für HTTP/JSON. Logik (Hashing, DB, Token) liegt im AuthService.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        // Service wird im Main "zusammengesteckt" (manuelles Dependency Injection)
        this.authService = authService;
    }

    /**
     * Handler für POST /api/users/register
     * Erwartet JSON: { "username": "...", "password": "..." }
     */
    public void handleRegister(HttpExchange exchange) throws IOException {
        try {
            // Wir akzeptieren hier nur POST, alles andere ist "Method Not Allowed"
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            // Body lesen und JSON -> RegisterRequest parsen
            String body = HttpUtil.readBody(exchange);
            RegisterRequest req = JsonUtil.MAPPER.readValue(body, RegisterRequest.class);

            // Business-Logik: User anlegen (inkl. Passwort-Hashing)
            User created = authService.register(req);

            // Response: 201 Created + kleines JSON (wir geben nicht das komplette User-Objekt zurück)
            HttpUtil.sendJson(exchange, 201, Map.of(
                    "id", created.getId(),
                    "username", created.getUsername()
            ));

        } catch (ApiException e) {
            // ApiException enthält schon passenden Statuscode (z.B. 400, 409)
            HttpUtil.sendJson(exchange, e.getStatus(), Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Alles andere ist ein echter Serverfehler
            HttpUtil.sendJson(exchange, 500, Map.of("message", "Internal Server Error"));
        }
    }

    /**
     * Handler für POST /api/users/login
     * Erwartet JSON: { "username": "...", "password": "..." }
     * Gibt Token zurück, der dann als Bearer Token verwendet wird.
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.sendEmpty(exchange, 405);
                return;
            }

            // Body lesen und JSON -> LoginRequest
            String body = HttpUtil.readBody(exchange);
            LoginRequest req = JsonUtil.MAPPER.readValue(body, LoginRequest.class);

            // Login prüfen (Passwort gegen Hash) und Token ausgeben
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
