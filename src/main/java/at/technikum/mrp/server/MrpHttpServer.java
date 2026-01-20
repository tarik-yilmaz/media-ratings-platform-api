package at.technikum.mrp.server;

import at.technikum.mrp.controller.AuthController;
import at.technikum.mrp.controller.FavoritesController;
import at.technikum.mrp.controller.LeaderboardController;
import at.technikum.mrp.controller.MediaController;
import at.technikum.mrp.controller.RatingController;
import at.technikum.mrp.controller.UserController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Kapselt den Java HttpServer (com.sun.net.httpserver).
 * Hier werden die API-Routen (Contexts) registriert und der Threadpool gesetzt.
 * Main erstellt diese Klasse und startet dann den Server.
 */
public class MrpHttpServer {
    private final HttpServer server;

    public MrpHttpServer(
            int port,
            AuthController authController,
            MediaController mediaController,
            RatingController ratingController,
            FavoritesController favoritesController,
            UserController userController,
            LeaderboardController leaderboardController
    ) throws IOException {

        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth
        server.createContext("/api/users/register", authController::handleRegister);
        server.createContext("/api/users/login", authController::handleLogin);

        // Media (inkl. /api/media/{id}, /rate, /ratings, /favorite über Controller-Parsing)
        server.createContext("/api/media", mediaController::handle);

        // Ratings
        server.createContext("/api/ratings", ratingController::handle);

        // Favorites list (GET /api/users/favorites)
        server.createContext("/api/users/favorites", favoritesController::handle);

        // User profile, history, recommendations (GET/PUT /api/users/{username}/profile etc.)
        server.createContext("/api/users", userController::handle);

        // Leaderboard
        server.createContext("/api/leaderboard", leaderboardController::handle);

        // Threadpool
        server.setExecutor(Executors.newFixedThreadPool(16));
    }

    public void start() {
        server.start();
        System.out.println("✓ HTTP Server läuft");
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }
}
