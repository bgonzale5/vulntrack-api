package io.vulntrack.infrastructure.graphql;

import io.vulntrack.domain.model.User;
import io.vulntrack.domain.primitives.Email;
import io.vulntrack.domain.primitives.Role;
import io.vulntrack.domain.primitives.Username;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

/**
 * GraphQL Resource: Users
 *
 */
@GraphQLApi
@ApplicationScoped
public class UserResource {

    /**
     * Query: Obtener todos los usuarios
     *
     */
    @Query("users")
    @Description("Obtiene todos los usuarios del sistema")
    public List<User> getAllUsers() {
        return User.listAll();
    }

    /**
     * Query: Obtener usuario por ID
     */
    @Query("user")
    @Description("Obtiene un usuario por su ID")
    public User getUserById(@Name("id") Long id) {
        return User.findById(id);
    }

    /**
     * Query: Obtener usuario por username
     */
    @Query("userByUsername")
    @Description("Busca un usuario por su nombre de usuario")
    public User getUserByUsername(@Name("username") String username) {
        return User.find("username", username).firstResult();
    }

    /**
     * Query: Obtener usuarios por rol
     */
    @Query("usersByRole")
    @Description("Obtiene todos los usuarios con un rol específico")
    public List<User> getUsersByRole(@Name("role") Role role) {
        return User.list("role", role);
    }

    /**
     * Mutation: Crear nuevo usuario
     *
     */
    @Mutation("createUser")
    @Description("Crea un nuevo usuario")
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

        User user = new User(validUsername, validEmail, fullName, password, role);
        user.persist();
        return user;
    }

    /**
     * Mutation: Actualizar rol de usuario
     *
     */
    @Mutation("updateUserRole")
    @Description("Actualiza el rol de un usuario")
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
     */
    @Mutation("deactivateUser")
    @Description("Desactiva un usuario")
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
     */
    @Mutation("activateUser")
    @Description("Activa un usuario")
    @Transactional
    public User activateUser(@Name("userId") Long userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        user.activate();
        return user;
    }

    @Query("searchUsers")
    @Description("Busca usuarios (VULNERABLE a SQL Injection)")
    public List<User> searchUsers(@Name("query") String query) {
        // VULNERABLE: Native SQL con concatenación directa
        return User.getEntityManager()
                .createNativeQuery(
                        "SELECT * FROM users WHERE username LIKE '%" + query + "%' OR email LIKE '%" + query + "%'",
                        User.class
                )
                .getResultList();
    }
}