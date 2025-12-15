package at.technikum.mrp;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.config.ServerConfig;
import at.technikum.mrp.controller.AuthController;
import at.technikum.mrp.controller.MediaController;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.server.MrpHttpServer;
import at.technikum.mrp.service.AuthService;
import at.technikum.mrp.service.MediaService;
import at.technikum.mrp.service.TokenService;

public class Main {
    public static void main(String[] args) throws Exception {

        ServerConfig.printConfig();

        if (!DatabaseConfig.testConnection()) {
            System.err.println("Abbruch: DB nicht erreichbar.");
            return;
        }

        // Repos
        UserRepository userRepository = new UserRepository();
        MediaRepository mediaRepository = new MediaRepository();

        // Services
        TokenService tokenService = new TokenService();
        AuthService authService = new AuthService(userRepository, tokenService);
        MediaService mediaService = new MediaService(mediaRepository);

        // Controller
        AuthController authController = new AuthController(authService);
        MediaController mediaController = new MediaController(mediaService, tokenService);

        // Server
        MrpHttpServer server = new MrpHttpServer(ServerConfig.getPort(), authController, mediaController);
        server.start();

        System.out.println("Base URL: http://localhost:" + ServerConfig.getPort() + "/api");
    }
}
