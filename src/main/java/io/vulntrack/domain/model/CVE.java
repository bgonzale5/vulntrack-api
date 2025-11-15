package io.vulntrack.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.vulntrack.domain.primitives.CVEId;
import io.vulntrack.domain.primitives.Severity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity: CVE (Common Vulnerabilities and Exposures)
 *
 * Representa una vulnerabilidad de seguridad identificada oficialmente.
 *
 */
@Entity
@Table(name = "cves",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cves_cve_id", columnNames = "cve_id")
        },
        indexes = {
                @Index(name = "idx_cves_severity", columnList = "severity"),
                @Index(name = "idx_cves_published", columnList = "published_date"),
                @Index(name = "idx_cves_cvss_score", columnList = "cvss_score")
        })
public class CVE extends PanacheEntity {

    @Column(name = "cve_id", nullable = false, unique = true, length = 20)
    private String cveId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "cvss_score", nullable = false)
    private double cvssScore;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate;

    /**
     * Productos afectados por este CVE
     *
     * NOTA: En Fase 1 usamos EAGER para simplificar (no óptimo).
     * En un sistema real, usar LAZY + DTO projections.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cve_affected_products",
            joinColumns = @JoinColumn(name = "cve_id",
                    foreignKey = @ForeignKey(name = "fk_cve_products_cve")),
            inverseJoinColumns = @JoinColumn(name = "product_id",
                    foreignKey = @ForeignKey(name = "fk_cve_products_product")),
            uniqueConstraints = @UniqueConstraint(name = "uk_cve_product",
                    columnNames = {"cve_id", "product_id"})
    )
    private List<Product> affectedProducts = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructor por defecto para JPA
    protected CVE() {
    }

    /**
     * Constructor usando Domain Primitives
     */
    public CVE(CVEId cveId, String title, String description,
               Severity severity, double cvssScore, LocalDate publishedDate) {
        this.cveId = cveId.value();
        this.title = Objects.requireNonNull(title, "Title no puede ser null").trim();
        this.description = description != null ? description.trim() : null;
        this.severity = Objects.requireNonNull(severity, "Severity no puede ser null");
        this.cvssScore = validateCvssScore(cvssScore);
        this.publishedDate = Objects.requireNonNull(publishedDate, "Published date no puede ser null");
        this.lastModifiedDate = publishedDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validación: CVSS score debe estar entre 0.0 y 10.0
     */
    private double validateCvssScore(double score) {
        if (score < 0.0 || score > 10.0) {
            throw new IllegalArgumentException(
                    "CVSS score debe estar entre 0.0 y 10.0, recibido: " + score);
        }
        return score;
    }

    // Getters

    public CVEId getCveId() {
        return CVEId.of(cveId);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public double getCvssScore() {
        return cvssScore;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public List<Product> getAffectedProducts() {
        return new ArrayList<>(affectedProducts); // Defensive copy
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Métodos de negocio

    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : null;
        this.lastModifiedDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSeverity(Severity severity, double cvssScore) {
        this.severity = Objects.requireNonNull(severity);
        this.cvssScore = validateCvssScore(cvssScore);
        this.lastModifiedDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addAffectedProduct(Product product) {
        Objects.requireNonNull(product, "Product no puede ser null");
        if (!affectedProducts.contains(product)) {
            affectedProducts.add(product);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeAffectedProduct(Product product) {
        if (affectedProducts.remove(product)) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean isCriticalOrHigh() {
        return severity.isCriticalOrHigh();
    }

    public int getAffectedProductCount() {
        return affectedProducts.size();
    }

    /**
     * Verifica si este CVE afecta a un producto específico
     */
    public boolean affectsProduct(Product product) {
        return affectedProducts.contains(product);
    }

    /**
     * Verifica si este CVE afecta a algún producto de un vendor específico
     */
    public boolean affectsVendor(Vendor vendor) {
        return affectedProducts.stream()
                .anyMatch(p -> p.getVendor().equals(vendor));
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
        lastModifiedDate = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CVE)) return false;
        CVE cve = (CVE) o;
        return Objects.equals(cveId, cve.cveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cveId);
    }

    @Override
    public String toString() {
        return "CVE{" +
                "id=" + id +
                ", cveId='" + cveId + '\'' +
                ", title='" + title + '\'' +
                ", severity=" + severity +
                ", cvssScore=" + cvssScore +
                ", affectedProducts=" + affectedProducts.size() +
                '}';
    }
}