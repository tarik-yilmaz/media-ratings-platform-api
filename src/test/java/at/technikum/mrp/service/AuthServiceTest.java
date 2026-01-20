package at.technikum.mrp.service;

import at.technikum.mrp.dto.LoginRequest;
import at.technikum.mrp.dto.RegisterRequest;
import at.technikum.mrp.model.User;
import at.technikum.mrp.repository.UserRepository;
import at.technikum.mrp.util.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private TokenService tokenService;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        tokenService = mock(TokenService.class);
        authService = new AuthService(userRepository, tokenService);
    }

    @Test
    void register_nullBody_throws400() {
        // Prüft: ohne Body -> 400 Bad Request
        ApiException ex = assertThrows(ApiException.class, () -> authService.register(null));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void register_missingUsername_throws400() {
        // Prüft: username fehlt/leer -> 400
        RegisterRequest req = new RegisterRequest();
        req.setUsername("  ");
        req.setPassword("secret");

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void register_missingPassword_throws400() {
        // Prüft: password fehlt/leer -> 400
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("");

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(req));
        assertEquals(400, ex.getStatus());
    }

    @Test
    void register_duplicateUsername_throws409() {
        // Prüft: username schon vorhanden -> 409 Conflict
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(User.builder().id(1).username("alice").passwordHash("x").build()));

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(req));
        assertEquals(409, ex.getStatus());
    }

    @Test
    void register_success_hashesPassword_andSavesUser() {
        // Prüft: Passwort wird gehasht + UserRepository.save() wird mit Hash aufgerufen
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(User.builder().id(10).username("alice").passwordHash("HASH").build());

        authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("alice", saved.getUsername());
        assertNotNull(saved.getPasswordHash());
        assertNotEquals("secret", saved.getPasswordHash());
        assertTrue(BCrypt.checkpw("secret", saved.getPasswordHash())); // Hash passt wirklich
    }

    @Test
    void register_repoReturnsNull_throws500() {
        // Prüft: DB-Insert liefert null -> 500
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(req));
        assertEquals(500, ex.getStatus());
    }

    @Test
    void login_userNotFound_throws401() {
        // Prüft: unbekannter username -> 401 Unauthorized
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(req));
        assertEquals(401, ex.getStatus());
    }

    @Test
    void login_wrongPassword_throws401() {
        // Prüft: falsches Passwort -> 401 Unauthorized
        String hash = BCrypt.hashpw("correct", BCrypt.gensalt());
        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(User.builder().id(1).username("alice").passwordHash(hash).build()));

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrong");

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(req));
        assertEquals(401, ex.getStatus());
    }

    @Test
    void login_success_issuesToken() {
        // Prüft: bei korrektem Login wird tokenService.issueToken(userId) aufgerufen
        String hash = BCrypt.hashpw("secret", BCrypt.gensalt());
        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(User.builder().id(7).username("alice").passwordHash(hash).build()));

        when(tokenService.issueToken(7)).thenReturn("token-123");

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        String token = authService.login(req);

        assertEquals("token-123", token);
        verify(tokenService).issueToken(7);
    }
}
