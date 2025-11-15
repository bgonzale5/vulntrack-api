package io.vulntrack.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.vulntrack.domain.primitives.Email;
import io.vulntrack.domain.primitives.Role;
import io.vulntrack.domain.primitives.Username;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity: User
 *
 * Representa un usuario del sistema con roles de acceso.
 *
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_active", columnList = "active")
        })
public class User extends PanacheEntity {

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructor por defecto requerido por JPA
    protected User() {
    }

    /**
     * Constructor usando Domain Primitives
     *
     * NOTA: En Fase 1 guardamos la contraseña en texto plano (VULNERABLE).
     * En Fase 2 implementaremos hashing con BCrypt.
     */
    public User(Username username, Email email, String fullName, String password, Role role) {
        this.username = username.value();
        this.email = email.value();
        this.fullName = Objects.requireNonNull(fullName, "Full name no puede ser null").trim();
        this.passwordHash = password; // FASE 1: texto plano (vulnerable)
        this.role = Objects.requireNonNull(role, "Role no puede ser null");
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters que retornan Domain Primitives

    public Username getUsername() {
        return Username.of(username);
    }

    public Email getEmail() {
        return Email.of(email);
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Métodos de negocio

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String fullName) {
        this.fullName = Objects.requireNonNull(fullName).trim();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasRole(Role role) {
        return this.role == role;
    }

    public boolean isAdmin() {
        return this.role.isAdmin();
    }

    public boolean canViewTechnicalDetails() {
        return this.role.canViewTechnicalDetails();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}