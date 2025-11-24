package io.vulntrack.infrastructure.graphql;

import io.vulntrack.application.security.AuthenticationService;
import io.vulntrack.domain.model.User;
import io.vulntrack.domain.primitives.Email;
import io.vulntrack.domain.primitives.Role;
import io.vulntrack.domain.primitives.Username;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

/**
 * GraphQL Resource: Users
 *
 * Fase 2: RBAC implementado con @RolesAllowed
 */
@GraphQLApi
@ApplicationScoped
public class UserResource {

    @Inject
    AuthenticationService authService;

    /**
     * Query: Obtener todos los usuarios
     *
     * RBAC: Solo ADMIN puede ver lista completa de usuarios
     */
    @Query("users")
    @Description("Obtiene todos los usuarios del sistema")
    @RolesAllowed("ADMIN")
    public List<User> getAllUsers() {
        return User.listAll();
    }

    /**
     * Query: Obtener usuario por ID
     *
     * RBAC: Solo ADMIN
     */
    @Query("user")
    @Description("Obtiene un usuario por su ID")
    @RolesAllowed("ADMIN")
    public User getUserById(@Name("id") Long id) {
        return User.findById(id);
    }

    /**
     * Query: Obtener usuario por username
     *
     * RBAC: Solo ADMIN
     */
    @Query("userByUsername")
    @Description("Busca un usuario por su nombre de usuario")
    @RolesAllowed("ADMIN")
    public User getUserByUsername(@Name("username") String username) {
        return User.find("username", username).firstResult();
    }

    /**
     * Query: Obtener usuarios por rol
     *
     * RBAC: Solo ADMIN
     */
    @Query("usersByRole")
    @Description("Obtiene todos los usuarios con un rol específico")
    @RolesAllowed("ADMIN")
    public List<User> getUsersByRole(@Name("role") Role role) {
        return User.list("role", role);
    }

    /**
     * Mutation: Crear nuevo usuario
     *
     * RBAC: Solo ADMIN puede crear usuarios
     *
     * CAMBIO FASE 2: Password se hashea con BCrypt
     */
    @Mutation("createUser")
    @Description("Crea un nuevo usuario")
    @RolesAllowed("ADMIN")
    @Transactional
    public User createUser(
            @Name("username") String username,
            @Name("email") String email,
            @Name("fullName") String fullName,
            @Name("password") String password,
            @Name("role") Role role) {

        // Validación usando Domain Primitives
        Username validUsername = Username.of(username);
        Email validEmail = Email.of(email);

        // FASE 2: Hash password con BCrypt
        String hashedPassword = authService.hashPassword(password);

        User user = new User(validUsername, validEmail, fullName, hashedPassword, role);
        user.persist();
        return user;
    }

    /**
     * Mutation: Actualizar rol de usuario
     *
     * RBAC: Solo ADMIN puede cambiar roles
     *
     * VULNERABLE EN FASE 1: Cualquiera podía hacerse ADMIN
     * MITIGADO EN FASE 2: Requiere autenticación + rol ADMIN
     */
    @Mutation("updateUserRole")
    @Description("Actualiza el rol de un usuario")
    @RolesAllowed("ADMIN")
    @Transactional
    public User updateUserRole(
            @Name("userId") Long userId,
            @Name("newRole") Role newRole) {

        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        user.changeRole(newRole);
        return user;
    }

    /**
     * Mutation: Desactivar usuario
     *
     * RBAC: Solo ADMIN
     */
    @Mutation("deactivateUser")
    @Description("Desactiva un usuario")
    @RolesAllowed("ADMIN")
    @Transactional
    public User deactivateUser(@Name("userId") Long userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        user.deactivate();
        return user;
    }

    /**
     * Mutation: Activar usuario
     *
     * RBAC: Solo ADMIN
     */
    @Mutation("activateUser")
    @Description("Activa un usuario")
    @RolesAllowed("ADMIN")
    @Transactional
    public User activateUser(@Name("userId") Long userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        user.activate();
        return user;
    }

    /**
     * Query: Buscar usuarios (VULNERABLE EN FASE 1)
     *
     * ELIMINADA EN FASE 2: SQL Injection mitigado
     *
     * Reemplazada por búsqueda segura con Panache
     */
    @Query("searchUsers")
    @Description("Busca usuarios (SEGURO - usa Panache parametrizado)")
    @RolesAllowed("ADMIN")
    public List<User> searchUsers(@Name("query") String query) {
        // FASE 2: Búsqueda segura con Panache (parametrizada)
        String searchPattern = "%" + query + "%";
        return User.list("username LIKE ?1 OR email LIKE ?2", searchPattern, searchPattern);
    }
}