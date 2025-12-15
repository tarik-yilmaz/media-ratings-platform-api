package at.technikum.mrp.service;

import at.technikum.mrp.dto.LoginRequest;
import at.technikum.mrp.dto.RegisterRequest;
import at.technikum.mrp.model.User;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.ApiException;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public User register(RegisterRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getUsername() == null || req.getUsername().trim().isEmpty())
            throw ApiException.badRequest("username ist erforderlich");
        if (req.getPassword() == null || req.getPassword().isEmpty())
            throw ApiException.badRequest("password ist erforderlich");

        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw ApiException.conflict("username ist bereits vergeben");
        }

        String hash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());

        User toSave = User.builder()
                .username(req.getUsername().trim())
                .passwordHash(hash)
                .email(null)
                .build();

        User saved = userRepository.save(toSave);
        if (saved == null) throw new ApiException(500, "User konnte nicht gespeichert werden");
        return saved;
    }

    public String login(LoginRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getUsername() == null || req.getPassword() == null)
            throw ApiException.badRequest("username und password sind erforderlich");

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("Login fehlgeschlagen"));

        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Login fehlgeschlagen");
        }

        return tokenService.issueToken(user.getId());
    }
}
