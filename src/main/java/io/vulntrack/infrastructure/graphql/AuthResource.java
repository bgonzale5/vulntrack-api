package io.vulntrack.infrastructure.graphql;

import io.vulntrack.application.security.AuthenticationService;
import io.vulntrack.domain.model.User;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;

/**
 * GraphQL Resource: Authentication
 * <p>
 * Fase 2: Manejo de autenticación JWT
 */
@GraphQLApi
public class AuthResource {

    @Inject
    AuthenticationService authService;

    /**
     * Mutation: Login
     * <p>
     * Autentica usuario y retorna JWT token
     */
    @Mutation("login")
    @Description("Autentica usuario y retorna JWT token")
    public AuthResponse login(
            @Name("username") String username,
            @Name("password") String password) {

        User user = authService.authenticate(username, password);

        if (user == null) {
            throw new GraphQLException("Credenciales inválidas",
                    GraphQLException.ExceptionType.ExecutionException);
        }

        String token = authService.generateToken(user);

        return new AuthResponse(
                token,
                user.id,
                user.getUsername().value(),
                user.getEmail().value(),
                user.getRole().name()
        );
    }

    /**
     * DTO: Auth Response
     */
    public static class AuthResponse {
        public String token;
        public Long userId;
        public String username;
        public String email;
        public String role;

        public AuthResponse(String token, Long userId, String username,
                            String email, String role) {
            this.token = token;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
        }
    }
}