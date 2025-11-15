package io.vulntrack.infrastructure.graphql;

import io.vulntrack.domain.model.Vendor;
import io.vulntrack.domain.primitives.VendorName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

/**
 * GraphQL Resource: Vendors
 *
 * FASE 1: API VULNERABLE
 */
@GraphQLApi
@ApplicationScoped
public class VendorResource {

    @Query("vendors")
    @Description("Obtiene todos los fabricantes")
    public List<Vendor> getAllVendors() {
        return Vendor.listAll();
    }

    @Query("vendor")
    @Description("Obtiene un fabricante por su ID")
    public Vendor getVendorById(@Name("id") Long id) {
        return Vendor.findById(id);
    }

    @Query("vendorByName")
    @Description("Busca un fabricante por nombre")
    public Vendor getVendorByName(@Name("name") String name) {
        return Vendor.find("name", name).firstResult();
    }

    @Query("activeVendors")
    @Description("Obtiene solo los fabricantes activos")
    public List<Vendor> getActiveVendors() {
        return Vendor.list("active", true);
    }

    @Mutation("createVendor")
    @Description("Crea un nuevo fabricante")
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

    @Mutation("updateVendor")
    @Description("Actualiza la información de un fabricante")
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

    @Mutation("deleteVendor")
    @Description("Desactiva un fabricante")
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