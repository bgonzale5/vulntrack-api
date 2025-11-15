package io.vulntrack.infrastructure.graphql;

import io.vulntrack.domain.model.Product;
import io.vulntrack.domain.model.Vendor;
import io.vulntrack.domain.primitives.ProductName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

/**
 * GraphQL Resource: Products
 *
 * FASE 1: API completamente abierta (VULNERABLE)
 */
@GraphQLApi
@ApplicationScoped
public class ProductResource {

    @Query("products")
    @Description("Obtiene todos los productos")
    public List<Product> getAllProducts() {
        return Product.listAll();
    }

    @Query("product")
    @Description("Obtiene un producto por su ID")
    public Product getProductById(@Name("id") Long id) {
        return Product.findById(id);
    }

    @Query("productsByVendor")
    @Description("Obtiene todos los productos de un fabricante")
    public List<Product> getProductsByVendor(@Name("vendorId") Long vendorId) {
        return Product.list("vendor.id", vendorId);
    }

    @Query("activeProducts")
    @Description("Obtiene solo los productos activos")
    public List<Product> getActiveProducts() {
        return Product.list("active", true);
    }

    @Mutation("createProduct")
    @Description("Crea un nuevo producto")
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

    @Mutation("updateProduct")
    @Description("Actualiza un producto")
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

    @Mutation("deleteProduct")
    @Description("Desactiva un producto")
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