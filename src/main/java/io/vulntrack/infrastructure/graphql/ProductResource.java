package io.vulntrack.infrastructure.graphql;

import io.vulntrack.domain.model.Product;
import io.vulntrack.domain.model.Vendor;
import io.vulntrack.domain.primitives.ProductName;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

/**
 * GraphQL Resource: Products
 *
 * Fase 2: RBAC implementado
 */
@GraphQLApi
@ApplicationScoped
public class ProductResource {

    /**
     * Query: Obtener todos los productos
     *
     * RBAC: Todos los roles autenticados pueden leer productos
     */
    @Query("products")
    @Description("Obtiene todos los productos")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<Product> getAllProducts() {
        return Product.listAll();
    }

    /**
     * Query: Obtener producto por ID
     *
     * RBAC: Todos los roles autenticados
     */
    @Query("product")
    @Description("Obtiene un producto por su ID")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public Product getProductById(@Name("id") Long id) {
        return Product.findById(id);
    }

    /**
     * Query: Obtener productos de un vendor
     *
     * RBAC: Todos los roles autenticados
     */
    @Query("productsByVendor")
    @Description("Obtiene todos los productos de un fabricante")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<Product> getProductsByVendor(@Name("vendorId") Long vendorId) {
        return Product.list("vendor.id", vendorId);
    }

    /**
     * Query: Obtener solo productos activos
     *
     * RBAC: Todos los roles autenticados
     */
    @Query("activeProducts")
    @Description("Obtiene solo los productos activos")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<Product> getActiveProducts() {
        return Product.list("active", true);
    }

    /**
     * Mutation: Crear nuevo producto
     *
     * RBAC: Solo RESEARCHER y ADMIN pueden crear productos
     */
    @Mutation("createProduct")
    @Description("Crea un nuevo producto")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public Product createProduct(
            @Name("vendorId") Long vendorId,
            @Name("name") String name,
            @Name("version") String version,
            @Name("description") String description) {

        Vendor vendor = Vendor.findById(vendorId);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor no encontrado: " + vendorId);
        }

        ProductName validName = ProductName.of(name);

        Product product = new Product(vendor, validName, version, description);
        product.persist();
        return product;
    }

    /**
     * Mutation: Actualizar producto
     *
     * RBAC: Solo RESEARCHER y ADMIN pueden actualizar productos
     */
    @Mutation("updateProduct")
    @Description("Actualiza un producto")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public Product updateProduct(
            @Name("productId") Long productId,
            @Name("version") String version,
            @Name("description") String description) {

        Product product = Product.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product no encontrado: " + productId);
        }

        if (version != null) {
            product.updateVersion(version);
        }
        if (description != null) {
            product.updateDescription(description);
        }

        return product;
    }

    /**
     * Mutation: Desactivar producto (soft delete)
     *
     * RBAC: Solo ADMIN puede eliminar productos
     */
    @Mutation("deleteProduct")
    @Description("Desactiva un producto")
    @RolesAllowed("ADMIN")
    @Transactional
    public boolean deleteProduct(@Name("productId") Long productId) {
        Product product = Product.findById(productId);
        if (product == null) {
            return false;
        }

        product.deactivate();
        return true;
    }
}