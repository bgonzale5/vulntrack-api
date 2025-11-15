package io.vulntrack.domain.primitives;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;

/**
 * Domain Primitive: Product Name
 *
 * Representa el nombre de un producto (Windows 10, Apache Tomcat, Chrome, etc.)
 *
 */
public final class ProductName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 150;

    private final String value;

    private ProductName(String value) {
        notNull(value, "Product name no puede ser null");

        String trimmed = value.trim();

        // Validación de tamaño
        isTrue(trimmed.length() >= MIN_LENGTH && trimmed.length() <= MAX_LENGTH,
                "Product name debe tener entre %d y %d caracteres", MIN_LENGTH, MAX_LENGTH);

        // Validación léxica: alfanuméricos, espacios, guiones, puntos, paréntesis
        isTrue(trimmed.matches("^[a-zA-Z0-9\\s.()/-]+$"),
                "Product name solo puede contener letras, números, espacios y caracteres: . ( ) / -");

        this.value = trimmed;
    }

    public static ProductName of(String value) {
        return new ProductName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductName that = (ProductName) o;
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