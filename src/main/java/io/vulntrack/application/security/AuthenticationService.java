package io.vulntrack.application.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import io.vulntrack.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Service: Authentication
 *
 * Gestiona autenticación JWT y hashing de passwords.
 *
 * Fase 2: Security Hardening
 */
@ApplicationScoped
public class AuthenticationService {

    /**
     * Genera token JWT para un usuario autenticado
     */
    public String generateToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add(user.getRole().name());

        return Jwt.issuer("https://vulntrack.io")
                .upn(user.getUsername().value())
                .groups(roles)
                .claim("userId", user.id)
                .claim("email", user.getEmail().value())
                .claim("fullName", user.getFullName())
                .expiresIn(Duration.ofHours(1))
                .sign();
    }

    /**
     * Autentica usuario con username y password
     *
     * @return User si credenciales válidas, null si inválidas
     */
    @Transactional
    public User authenticate(String username, String password) {
        User user = User.find("username", username).firstResult();

        if (user == null) {
            return null;
        }

        if (!user.isActive()) {
            return null;
        }

        // Verificar password con BCrypt
        if (!BcryptUtil.matches(password, user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    /**
     * Hash password con BCrypt
     */
    public String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    /**
     * Verifica si password coincide con hash
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}