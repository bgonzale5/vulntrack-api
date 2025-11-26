package io.vulntrack.infrastructure.graphql;

import io.vulntrack.domain.model.Vendor;
import io.vulntrack.domain.primitives.VendorName;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

/**
 * GraphQL Resource: Vendors
 *
 * Fase 2: RBAC implementado
 */
@GraphQLApi
@ApplicationScoped
public class VendorResource {

    /**
     * Query: Obtener todos los vendors
     *
     * RBAC: Todos los roles autenticados pueden leer vendors
     */
    @Query("vendors")
    @Description("Obtiene todos los fabricantes")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<Vendor> getAllVendors() {
        return Vendor.listAll();
    }

    /**
     * Query: Obtener vendor por ID
     *
     * RBAC: Todos los roles autenticados
     */
    @Query("vendor")
    @Description("Obtiene un fabricante por su ID")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public Vendor getVendorById(@Name("id") Long id) {
        return Vendor.findById(id);
    }

    /**
     * Query: Buscar vendor por nombre
     *
     * RBAC: Todos los roles autenticados
     */
    @Query("vendorByName")
    @Description("Busca un fabricante por nombre")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public Vendor getVendorByName(@Name("name") String name) {
        return Vendor.find("name", name).firstResult();
    }

    /**
     * Query: Obtener solo vendors activos
     *
     * RBAC: Todos los roles autenticados
     */
    @Query("activeVendors")
    @Description("Obtiene solo los fabricantes activos")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<Vendor> getActiveVendors() {
        return Vendor.list("active", true);
    }

    /**
     * Mutation: Crear nuevo vendor
     *
     * RBAC: Solo RESEARCHER y ADMIN pueden crear vendors
     */
    @Mutation("createVendor")
    @Description("Crea un nuevo fabricante")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public Vendor createVendor(
            @Name("name") String name,
            @Name("description") String description,
            @Name("website") String website) {

        VendorName validName = VendorName.of(name);

        Vendor vendor = new Vendor(validName, description, website);
        vendor.persist();
        return vendor;
    }

    /**
     * Mutation: Actualizar vendor
     *
     * RBAC: Solo RESEARCHER y ADMIN pueden actualizar vendors
     */
    @Mutation("updateVendor")
    @Description("Actualiza la información de un fabricante")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public Vendor updateVendor(
            @Name("vendorId") Long vendorId,
            @Name("description") String description,
            @Name("website") String website) {

        Vendor vendor = Vendor.findById(vendorId);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor no encontrado: " + vendorId);
        }

        if (description != null) {
            vendor.updateDescription(description);
        }
        if (website != null) {
            vendor.updateWebsite(website);
        }

        return vendor;
    }

    /**
     * Mutation: Desactivar vendor (soft delete)
     *
     * RBAC: Solo ADMIN puede eliminar vendors
     */
    @Mutation("deleteVendor")
    @Description("Desactiva un fabricante")
    @RolesAllowed("ADMIN")
    @Transactional
    public boolean deleteVendor(@Name("vendorId") Long vendorId) {
        Vendor vendor = Vendor.findById(vendorId);
        if (vendor == null) {
            return false;
        }

        vendor.deactivate();
        return true;
    }
}