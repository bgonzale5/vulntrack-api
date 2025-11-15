package io.vulntrack.domain.primitives;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;

/**
 * Domain Primitive: Username
 *
 * Representa un nombre de usuario para autenticación.
 *
 */
public final class Username {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;

    private final String value;

    private Username(String value) {
        notNull(value, "Username no puede ser null");

        String trimmed = value.trim().toLowerCase(); // Normalizamos a lowercase

        // Validación de tamaño
        isTrue(trimmed.length() >= MIN_LENGTH && trimmed.length() <= MAX_LENGTH,
                "Username debe tener entre %d y %d caracteres", MIN_LENGTH, MAX_LENGTH);

        // Validación léxica: debe empezar con letra, luego letras/números/guiones/underscores
        isTrue(trimmed.matches("^[a-z][a-z0-9_-]*$"),
                "Username debe empezar con letra y solo contener letras, números, _ o -");

        this.value = trimmed;
    }

    public static Username of(String value) {
        return new Username(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return value.equals(username.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}