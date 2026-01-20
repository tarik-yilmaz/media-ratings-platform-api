package at.technikum.mrp;

import at.technikum.mrp.config.DatabaseConfig;
import at.technikum.mrp.config.ServerConfig;
import at.technikum.mrp.controller.*;
import at.technikum.mrp.repository.FavoritesRepository;
import at.technikum.mrp.repository.MediaRepository;
import at.technikum.mrp.repository.RatingRepository;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.server.MrpHttpServer;
import at.technikum.mrp.service.*;

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
        RatingRepository ratingRepository = new RatingRepository();
        FavoritesRepository favoritesRepository = new FavoritesRepository();

        // Services
        TokenService tokenService = new TokenService();
        AuthService authService = new AuthService(userRepository, tokenService);
        MediaService mediaService = new MediaService(mediaRepository);
        RatingService ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);
        FavoritesService favoritesService = new FavoritesService(favoritesRepository, mediaRepository);
        RecommendationService recommendationService = new RecommendationService(mediaRepository, ratingRepository);


        // Controller
        AuthController authController = new AuthController(authService);
        MediaController mediaController = new MediaController(mediaService, tokenService, ratingService, favoritesService);
        RatingController ratingController = new RatingController(ratingService, tokenService);
        FavoritesController favoritesController = new FavoritesController(favoritesService, tokenService);
        UserController userController = new UserController(tokenService, userRepository, ratingService, mediaService, recommendationService, favoritesService);
        LeaderboardController leaderboardController = new LeaderboardController(userRepository);

        // Server
        MrpHttpServer server = new MrpHttpServer(
                ServerConfig.getPort(),
                authController,
                mediaController,
                ratingController,
                favoritesController,
                userController,
                leaderboardController
        );

        server.start();

        System.out.println("Base URL: http://localhost:" + ServerConfig.getPort() + "/api");
    }
}
