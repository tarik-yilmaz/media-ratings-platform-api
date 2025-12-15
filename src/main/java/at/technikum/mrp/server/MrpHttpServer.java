package at.technikum.mrp.server;

import at.technikum.mrp.controller.AuthController;
import at.technikum.mrp.controller.MediaController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MrpHttpServer {
    private final HttpServer server;

    public MrpHttpServer(int port, AuthController authController, MediaController mediaController) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/users/register", authController::handleRegister);
        server.createContext("/api/users/login", authController::handleLogin);

        // Prefix-Context: handle in controller ("/api/media" und "/api/media/{id}")
        server.createContext("/api/media", mediaController::handle);

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
