package at.technikum.mrp.service;

import at.technikum.mrp.dto.LoginRequest;
import at.technikum.mrp.dto.RegisterRequest;
import at.technikum.mrp.model.User;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.ApiException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Business-Logik für Registrierung und Login.
 * Controller macht nur HTTP/JSON, Service macht Validierung + Hashing + DB-Zugriff.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Registriert einen neuen User.
     * - Validiert Input
     * - prüft ob username schon existiert
     * - hasht Passwort (BCrypt)
     * - speichert in DB
     */
    public User register(RegisterRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getUsername() == null || req.getUsername().trim().isEmpty())
            throw ApiException.badRequest("username ist erforderlich");
        if (req.getPassword() == null || req.getPassword().isEmpty())
            throw ApiException.badRequest("password ist erforderlich");

        // Duplicate username -> 409 Conflict
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw ApiException.conflict("username ist bereits vergeben");
        }

        // BCrypt: sicherer Hash, Passwort wird nie im Klartext gespeichert
        String hash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());

        User toSave = User.builder()
                .username(req.getUsername().trim())
                .passwordHash(hash)
                .email(null) // optional, kommt später über Profil-Update
                .build();

        User saved = userRepository.save(toSave);
        if (saved == null) throw new ApiException(500, "User konnte nicht gespeichert werden");
        return saved;
    }

    /**
     * Login: prüft username + Passwort.
     * Wenn ok -> Token ausstellen, das danach in Authorization Header verwendet wird.
     */
    public String login(LoginRequest req) {
        if (req == null) throw ApiException.badRequest("Body fehlt");
        if (req.getUsername() == null || req.getPassword() == null)
            throw ApiException.badRequest("username und password sind erforderlich");

        // Bei falschem username: absichtlich gleiche Message wie bei falschem Passwort
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("Login fehlgeschlagen"));

        // BCrypt check: Passwort gegen Hash prüfen
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Login fehlgeschlagen");
        }

        // Token generieren + Session speichern
        return tokenService.issueToken(user.getId());
    }
}
