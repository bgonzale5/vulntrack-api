package io.vulntrack.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.vulntrack.domain.primitives.VendorName;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity: Vendor
 *
 * Representa un fabricante de software (Microsoft, Apache, Google, etc.)
 *
 */
@Entity
@Table(name = "vendors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vendors_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_vendors_active", columnList = "active")
        })
public class Vendor extends PanacheEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String website;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "vendor", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructor por defecto para JPA
    protected Vendor() {
    }

    /**
     * Constructor usando Domain Primitive
     */
    public Vendor(VendorName name, String description, String website) {
        this.name = name.value();
        this.description = description != null ? description.trim() : null;
        this.website = website != null ? website.trim() : null;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters

    public VendorName getName() {
        return VendorName.of(name);
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
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

    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateWebsite(String website) {
        this.website = website != null ? website.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vendor)) return false;
        Vendor vendor = (Vendor) o;
        return Objects.equals(name, vendor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}