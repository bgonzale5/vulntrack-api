-- ============================================
-- VulnTrack API - Initial Schema
-- Fase 1: Baseline (sin seguridad)
-- ============================================

-- Sequences para Panache (requerido por Hibernate)
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE vendors_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE products_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE cves_seq START WITH 1 INCREMENT BY 50;

-- Tabla: users
CREATE TABLE users (
                       id BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
                       username VARCHAR(30) NOT NULL,
                       email VARCHAR(254) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('ANALYST', 'RESEARCHER', 'ADMIN')),
                       active BOOLEAN NOT NULL DEFAULT true,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_users_username UNIQUE (username),
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);

-- Tabla: vendors
CREATE TABLE vendors (
                         id BIGINT PRIMARY KEY DEFAULT nextval('vendors_seq'),
                         name VARCHAR(100) NOT NULL,
                         description VARCHAR(500),
                         website VARCHAR(255),
                         active BOOLEAN NOT NULL DEFAULT true,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT uk_vendors_name UNIQUE (name)
);

CREATE INDEX idx_vendors_active ON vendors(active);

-- Tabla: products
CREATE TABLE products (
                          id BIGINT PRIMARY KEY DEFAULT nextval('products_seq'),
                          vendor_id BIGINT NOT NULL,
                          name VARCHAR(150) NOT NULL,
                          version VARCHAR(50),
                          description VARCHAR(500),
                          active BOOLEAN NOT NULL DEFAULT true,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_products_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
                          CONSTRAINT uk_products_vendor_name UNIQUE (vendor_id, name)
);

CREATE INDEX idx_products_vendor ON products(vendor_id);
CREATE INDEX idx_products_active ON products(active);

-- Tabla: cves
CREATE TABLE cves (
                      id BIGINT PRIMARY KEY DEFAULT nextval('cves_seq'),
                      cve_id VARCHAR(20) NOT NULL,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      severity VARCHAR(20) NOT NULL CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')),
                      cvss_score DOUBLE PRECISION NOT NULL CHECK (cvss_score BETWEEN 0.0 AND 10.0),
                      published_date DATE NOT NULL,
                      last_modified_date DATE,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT uk_cves_cve_id UNIQUE (cve_id)
);

CREATE INDEX idx_cves_severity ON cves(severity);
CREATE INDEX idx_cves_published ON cves(published_date);
CREATE INDEX idx_cves_cvss_score ON cves(cvss_score);

-- Tabla de relación: cve_affected_products (ManyToMany)
CREATE TABLE cve_affected_products (
                                       cve_id BIGINT NOT NULL,
                                       product_id BIGINT NOT NULL,

                                       CONSTRAINT fk_cve_products_cve FOREIGN KEY (cve_id) REFERENCES cves(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_cve_products_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                                       CONSTRAINT uk_cve_product UNIQUE (cve_id, product_id)
);

CREATE INDEX idx_cve_products_cve ON cve_affected_products(cve_id);
CREATE INDEX idx_cve_products_product ON cve_affected_products(product_id);