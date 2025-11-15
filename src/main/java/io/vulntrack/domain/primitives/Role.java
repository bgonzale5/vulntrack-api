package io.vulntrack.domain.primitives;

/**
 * Domain Primitive: User Role
 *
 * Roles disponibles en el sistema:
 * - ANALYST: Puede ver CVEs básicos
 * - RESEARCHER: Puede ver CVEs + detalles técnicos
 * - ADMIN: Acceso completo + gestión de usuarios
 *
 */
public enum Role {
    ANALYST("Analyst", "Puede consultar CVEs básicos"),
    RESEARCHER("Researcher", "Puede consultar CVEs con detalles técnicos"),
    ADMIN("Administrator", "Acceso completo y gestión de usuarios");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    /**
     * Verifica si el rol tiene privilegios administrativos
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Verifica si el rol puede ver información técnica detallada
     */
    public boolean canViewTechnicalDetails() {
        return this == RESEARCHER || this == ADMIN;
    }
}