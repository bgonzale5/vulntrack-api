package io.vulntrack.domain.primitives;

/**
 * Domain Primitive: CVE Severity
 *
 * Representa la severidad de una vulnerabilidad según CVSS v3.
 *
 * Niveles:
 * - CRITICAL: 9.0 - 10.0
 * - HIGH: 7.0 - 8.9
 * - MEDIUM: 4.0 - 6.9
 * - LOW: 0.1 - 3.9
 *
 */
public enum Severity {
    CRITICAL("Critical", 9.0, 10.0),
    HIGH("High", 7.0, 8.9),
    MEDIUM("Medium", 4.0, 6.9),
    LOW("Low", 0.1, 3.9);

    private final String displayName;
    private final double minScore;
    private final double maxScore;

    Severity(String displayName, double minScore, double maxScore) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * Determina la severidad a partir de un score CVSS
     */
    public static Severity fromCvssScore(double score) {
        if (score >= CRITICAL.minScore) return CRITICAL;
        if (score >= HIGH.minScore) return HIGH;
        if (score >= MEDIUM.minScore) return MEDIUM;
        if (score >= LOW.minScore) return LOW;
        throw new IllegalArgumentException("CVSS score debe ser mayor a 0: " + score);
    }

    public String displayName() {
        return displayName;
    }

    public double minScore() {
        return minScore;
    }

    public double maxScore() {
        return maxScore;
    }

    public boolean isCriticalOrHigh() {
        return this == CRITICAL || this == HIGH;
    }
}