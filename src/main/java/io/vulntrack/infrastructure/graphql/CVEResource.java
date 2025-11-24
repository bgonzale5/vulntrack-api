package io.vulntrack.infrastructure.graphql;

import io.vulntrack.domain.model.CVE;
import io.vulntrack.domain.model.Product;
import io.vulntrack.domain.primitives.CVEId;
import io.vulntrack.domain.primitives.Severity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.time.LocalDate;
import java.util.List;

/**
 * GraphQL Resource: CVEs
 *
 * Fase 2: RBAC implementado
 */
@GraphQLApi
@ApplicationScoped
public class CVEResource {

    /**
     * Query: Obtener todos los CVEs
     *
     * RBAC: Todos los roles autenticados pueden leer CVEs
     */
    @Query("cves")
    @Description("Obtiene todos los CVEs")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<CVE> getAllCVEs() {
        return CVE.listAll();
    }

    @Query("cve")
    @Description("Obtiene un CVE por su ID")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public CVE getCVEById(@Name("id") Long id) {
        return CVE.findById(id);
    }

    @Query("cveByCveId")
    @Description("Busca un CVE por su identificador CVE-YYYY-NNNNN")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public CVE getCVEByCveId(@Name("cveId") String cveId) {
        return CVE.find("cveId", cveId).firstResult();
    }

    @Query("cvesBySeverity")
    @Description("Obtiene CVEs filtrados por severidad")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<CVE> getCVEsBySeverity(@Name("severity") Severity severity) {
        return CVE.list("severity", severity);
    }

    @Query("criticalCVEs")
    @Description("Obtiene solo CVEs críticos")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<CVE> getCriticalCVEs() {
        return CVE.list("severity", Severity.CRITICAL);
    }

    @Query("recentCVEs")
    @Description("Obtiene CVEs publicados en los últimos N días")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<CVE> getRecentCVEs(@Name("days") int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return CVE.list("publishedDate >= ?1", cutoffDate);
    }

    @Query("cvesByScoreRange")
    @Description("Obtiene CVEs dentro de un rango de CVSS score")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<CVE> getCVEsByScoreRange(
            @Name("minScore") double minScore,
            @Name("maxScore") double maxScore) {

        return CVE.list("cvssScore BETWEEN ?1 AND ?2", minScore, maxScore);
    }

    /**
     * Mutation: Crear nuevo CVE
     *
     * RBAC: Solo RESEARCHER y ADMIN pueden crear CVEs
     */
    @Mutation("createCVE")
    @Description("Crea un nuevo CVE")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public CVE createCVE(
            @Name("cveId") String cveId,
            @Name("title") String title,
            @Name("description") String description,
            @Name("severity") Severity severity,
            @Name("cvssScore") double cvssScore,
            @Name("publishedDate") String publishedDate) {

        CVEId validCveId = CVEId.of(cveId);
        LocalDate parsedDate = LocalDate.parse(publishedDate);

        CVE cve = new CVE(validCveId, title, description, severity, cvssScore, parsedDate);
        cve.persist();
        return cve;
    }

    /**
     * Mutation: Actualizar descripción de CVE
     *
     * RBAC: Solo RESEARCHER y ADMIN
     */
    @Mutation("updateCVEDescription")
    @Description("Actualiza la descripción de un CVE")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public CVE updateCVEDescription(
            @Name("cveId") Long cveId,
            @Name("description") String description) {

        CVE cve = CVE.findById(cveId);
        if (cve == null) {
            throw new IllegalArgumentException("CVE no encontrado: " + cveId);
        }

        cve.updateDescription(description);
        return cve;
    }

    /**
     * Mutation: Actualizar severidad de CVE
     *
     * RBAC: Solo RESEARCHER y ADMIN
     */
    @Mutation("updateCVESeverity")
    @Description("Actualiza la severidad y score de un CVE")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public CVE updateCVESeverity(
            @Name("cveId") Long cveId,
            @Name("severity") Severity severity,
            @Name("cvssScore") double cvssScore) {

        CVE cve = CVE.findById(cveId);
        if (cve == null) {
            throw new IllegalArgumentException("CVE no encontrado: " + cveId);
        }

        cve.updateSeverity(severity, cvssScore);
        return cve;
    }

    /**
     * Mutation: Añadir producto afectado a un CVE
     *
     * RBAC: Solo RESEARCHER y ADMIN
     */
    @Mutation("addAffectedProduct")
    @Description("Añade un producto a la lista de afectados por un CVE")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public CVE addAffectedProduct(
            @Name("cveId") Long cveId,
            @Name("productId") Long productId) {

        CVE cve = CVE.findById(cveId);
        if (cve == null) {
            throw new IllegalArgumentException("CVE no encontrado: " + cveId);
        }

        Product product = Product.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product no encontrado: " + productId);
        }

        cve.addAffectedProduct(product);
        return cve;
    }

    @Mutation("removeAffectedProduct")
    @Description("Remueve un producto de la lista de afectados por un CVE")
    @RolesAllowed({"RESEARCHER", "ADMIN"})
    @Transactional
    public CVE removeAffectedProduct(
            @Name("cveId") Long cveId,
            @Name("productId") Long productId) {

        CVE cve = CVE.findById(cveId);
        if (cve == null) {
            throw new IllegalArgumentException("CVE no encontrado: " + cveId);
        }

        Product product = Product.findById(productId);
        if (product != null) {
            cve.removeAffectedProduct(product);
        }

        return cve;
    }

    /**
     * Query: Buscar CVEs (VULNERABLE EN FASE 1)
     *
     * MITIGADO EN FASE 2: SQL Injection eliminado
     */
    @Query("searchCVEs")
    @Description("Busca CVEs (SEGURO - usa Panache parametrizado)")
    @RolesAllowed({"ANALYST", "RESEARCHER", "ADMIN"})
    public List<CVE> searchCVEs(@Name("query") String query) {
        // FASE 2: Búsqueda segura con Panache
        String searchPattern = "%" + query + "%";
        return CVE.list("title LIKE ?1 OR description LIKE ?2", searchPattern, searchPattern);
    }
}