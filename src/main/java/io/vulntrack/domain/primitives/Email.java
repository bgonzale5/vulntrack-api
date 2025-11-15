package io.vulntrack.domain.primitives;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Domain Primitive: Email Address
 *
 * Representa una dirección de email válida.
 *
 */
public final class Email {

    // Regex simplificado pero robusto para emails comunes
    // Acepta: usuario@dominio.com, usuario.nombre@sub.dominio.co.uk, etc.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final int MAX_LENGTH = 254; // Según RFC 5321

    private final String value;

    private Email(String value) {
        notNull(value, "Email no puede ser null");

        String trimmed = value.trim().toLowerCase(); // Normalizamos a lowercase

        // Validación de tamaño
        isTrue(trimmed.length() <= MAX_LENGTH,
                "Email no puede exceder %d caracteres", MAX_LENGTH);

        // Validación léxica
        isTrue(EMAIL_PATTERN.matcher(trimmed).matches(),
                "Email inválido: %s", trimmed);

        this.value = trimmed;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String value() {
        return value;
    }

    /**
     * Retorna el dominio del email (parte después del @)
     */
    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }

    /**
     * Retorna la parte local del email (parte antes del @)
     */
    public String localPart() {
        return value.substring(0, value.indexOf('@'));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return value.equals(email.value);
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