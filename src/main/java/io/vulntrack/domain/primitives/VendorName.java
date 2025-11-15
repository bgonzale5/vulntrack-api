package io.vulntrack.domain.primitives;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;

/**
 * Domain Primitive: Vendor Name
 *
 * Representa el nombre de un fabricante/vendor (Microsoft, Apache, Google, etc.)
 *
 */
public final class VendorName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    private final String value;

    private VendorName(String value) {
        notNull(value, "Vendor name no puede ser null");

        String trimmed = value.trim();

        // Validación de tamaño
        isTrue(trimmed.length() >= MIN_LENGTH && trimmed.length() <= MAX_LENGTH,
                "Vendor name debe tener entre %d y %d caracteres", MIN_LENGTH, MAX_LENGTH);

        // Validación léxica: solo caracteres alfanuméricos, espacios, guiones y puntos
        isTrue(trimmed.matches("^[a-zA-Z0-9\\s.-]+$"),
                "Vendor name solo puede contener letras, números, espacios, guiones y puntos");

        this.value = trimmed;
    }

    public static VendorName of(String value) {
        return new VendorName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorName that = (VendorName) o;
        return value.equals(that.value);
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