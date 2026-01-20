package at.technikum.mrp.server;

import at.technikum.mrp.controller.AuthController;
import at.technikum.mrp.controller.FavoritesController;
import at.technikum.mrp.controller.MediaController;
import at.technikum.mrp.controller.RatingController;
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
            FavoritesController favoritesController
    ) throws IOException {

        // Bindet auf den Port (localhost:port)
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth-Endpunkte
        server.createContext("/api/users/register", authController::handleRegister);
        server.createContext("/api/users/login", authController::handleLogin);

        // Media-Endpunkte: ein Context für /api/media und alles darunter
        // Controller unterscheidet dann /api/media vs /api/media/{id}
        server.createContext("/api/media", mediaController::handle);


        server.createContext("/api/ratings", ratingController::handle);
        server.createContext("/api/ratings", ratingController::handle);
        server.createContext("/api/users/favorites", favoritesController::handle);

        // Einfacher Threadpool, damit Requests parallel verarbeitet werden können
        server.setExecutor(Executors.newFixedThreadPool(16));
    }

    /**
     * Startet den HTTP Server.
     */
    public void start() {
        server.start();
        System.out.println("✓ HTTP Server läuft");
    }

    /**
     * Stoppt den Server (delaySeconds = Zeit zum "auslaufen lassen" von Requests).
     */
    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }
}
