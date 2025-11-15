package io.vulntrack.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.vulntrack.domain.primitives.ProductName;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity: Product
 *
 * Representa un producto de software (Windows 10, Apache Tomcat, Chrome, etc.)
 *
 */
@Entity
@Table(name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_vendor_name",
                        columnNames = {"vendor_id", "name"})
        },
        indexes = {
                @Index(name = "idx_products_vendor", columnList = "vendor_id"),
                @Index(name = "idx_products_active", columnList = "active")
        })
public class Product extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_vendor"))
    private Vendor vendor;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String version;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructor por defecto para JPA
    protected Product() {
    }

    /**
     * Constructor usando Domain Primitive
     */
    public Product(Vendor vendor, ProductName name, String version, String description) {
        this.vendor = Objects.requireNonNull(vendor, "Vendor no puede ser null");
        this.name = name.value();
        this.version = version != null ? version.trim() : null;
        this.description = description != null ? description.trim() : null;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters

    public Vendor getVendor() {
        return vendor;
    }

    public ProductName getName() {
        return ProductName.of(name);
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
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

    public void updateVersion(String version) {
        this.version = version != null ? version.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDescription(String description) {
        this.description = description != null ? description.trim() : null;
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

    /**
     * Retorna el nombre completo del producto (Vendor + Product)
     * Ejemplo: "Microsoft Windows 10"
     */
    public String getFullName() {
        return vendor.getName().value() + " " + name;
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
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(vendor.id, product.vendor.id) &&
                Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor.id, name);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", vendor=" + vendor.getName().value() +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", active=" + active +
                '}';
    }
}