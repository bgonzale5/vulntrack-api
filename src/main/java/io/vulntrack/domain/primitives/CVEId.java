package io.vulntrack.domain.primitives;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Domain Primitive: CVE Identifier
 *
 * Representa un identificador CVE válido según el formato oficial:
 *
 */
public final class CVEId {

    // Formato oficial CVE: CVE-YYYY-NNNNN+
    private static final Pattern CVE_PATTERN = Pattern.compile("^CVE-\\d{4}-\\d{4,}$");
    private static final int MIN_YEAR = 1999; // CVE comenzó en 1999
    private static final int MAX_YEAR = 2100; // Límite razonable

    private final String value;

    /**
     * Constructor privado - solo se puede crear mediante el factory method of()
     */
    private CVEId(String value) {
        notNull(value, "CVE ID no puede ser null");

        String trimmed = value.trim();

        // Validación léxica: formato CVE-YYYY-NNNNN
        isTrue(CVE_PATTERN.matcher(trimmed).matches(),
                "CVE ID debe seguir el formato CVE-YYYY-NNNNN (ej: CVE-2023-12345)");

        // Validación semántica: año razonable
        int year = extractYear(trimmed);
        isTrue(year >= MIN_YEAR && year <= MAX_YEAR,
                "Año del CVE debe estar entre %d y %d, pero se recibió: %d",
                MIN_YEAR, MAX_YEAR, year);

        this.value = trimmed;
    }

    /**
     * Factory method - única forma pública de crear un CVEId
     */
    public static CVEId of(String value) {
        return new CVEId(value);
    }

    /**
     * Extrae el año del formato CVE-YYYY-NNNNN
     */
    private int extractYear(String cveId) {
        // CVE-2023-12345 -> extraer "2023"
        String[] parts = cveId.split("-");
        return Integer.parseInt(parts[1]);
    }

    /**
     * Retorna el valor como String para persistencia/serialización
     */
    public String value() {
        return value;
    }

    /**
     * Extrae el año como entero
     */
    public int year() {
        return extractYear(value);
    }

    /**
     * Extrae el número secuencial del CVE
     */
    public int sequenceNumber() {
        String[] parts = value.split("-");
        return Integer.parseInt(parts[2]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CVEId cveId = (CVEId) o;
        return value.equals(cveId.value);
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