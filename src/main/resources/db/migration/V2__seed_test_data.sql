-- ============================================
-- VulnTrack API - Seed Test Data (CORREGIDO)
-- ============================================

-- Usuarios de prueba
INSERT INTO users (username, email, full_name, password_hash, role, active, created_at, updated_at) VALUES
                                                                                                        ('admin', 'admin@vulntrack.io', 'Administrator User', 'admin123', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                        ('researcher', 'researcher@vulntrack.io', 'Security Researcher', 'research123', 'RESEARCHER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                        ('analyst', 'analyst@vulntrack.io', 'Security Analyst', 'analyst123', 'ANALYST', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Vendors
INSERT INTO vendors (name, description, website, active, created_at, updated_at) VALUES
                                                                                     ('Microsoft', 'Microsoft Corporation - Software and cloud services', 'https://www.microsoft.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                     ('Apache', 'Apache Software Foundation - Open source software', 'https://www.apache.org', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                     ('Google', 'Google LLC - Internet services and products', 'https://www.google.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                     ('Oracle', 'Oracle Corporation - Database and enterprise software', 'https://www.oracle.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Products
INSERT INTO products (vendor_id, name, version, description, active, created_at, updated_at) VALUES
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Microsoft'), 'Windows 10', '22H2', 'Windows 10 Operating System', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Microsoft'), 'Windows 11', '23H2', 'Windows 11 Operating System', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Microsoft'), 'Microsoft Edge', '120.0', 'Chromium-based web browser', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Apache'), 'Apache Tomcat', '9.0.83', 'Java Servlet Container', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Apache'), 'Apache HTTP Server', '2.4.58', 'Web Server', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Google'), 'Chrome', '120.0.6099', 'Web Browser', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                 ((SELECT id FROM vendors WHERE name = 'Oracle'), 'Oracle Database', '19c', 'Relational Database', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CVEs
INSERT INTO cves (cve_id, title, description, severity, cvss_score, published_date, last_modified_date, created_at, updated_at) VALUES
                                                                                                                                    ('CVE-2023-12345', 'Remote Code Execution in Windows 10', 'A remote code execution vulnerability exists in Windows 10 when the Windows Adobe Type Manager Library improperly handles a specially-crafted multi-master font', 'CRITICAL', 9.8, '2023-11-15', '2023-11-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                    ('CVE-2023-23456', 'Apache Tomcat Request Smuggling', 'Apache Tomcat is vulnerable to HTTP request smuggling attacks when used with a reverse proxy', 'HIGH', 7.5, '2023-10-10', '2023-10-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                    ('CVE-2023-34567', 'Chrome V8 Use-After-Free', 'Use after free in V8 in Google Chrome prior to version 120.0 allowed a remote attacker to potentially exploit heap corruption', 'CRITICAL', 9.6, '2023-12-01', '2023-12-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                    ('CVE-2023-45678', 'Oracle Database SQL Injection', 'Vulnerability in Oracle Database Server component allows authenticated attacker to compromise database', 'MEDIUM', 6.5, '2023-09-20', '2023-09-20', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                                                                                    ('CVE-2023-56789', 'Microsoft Edge CORS Bypass', 'CORS bypass vulnerability in Microsoft Edge could allow information disclosure', 'MEDIUM', 5.3, '2023-11-25', '2023-11-25', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Relación CVE
INSERT INTO cve_affected_products (cve_id, product_id) VALUES
                                                           ((SELECT id FROM cves WHERE cve_id = 'CVE-2023-12345'), (SELECT id FROM products WHERE name = 'Windows 10')),
                                                           ((SELECT id FROM cves WHERE cve_id = 'CVE-2023-12345'), (SELECT id FROM products WHERE name = 'Windows 11')),
                                                           ((SELECT id FROM cves WHERE cve_id = 'CVE-2023-23456'), (SELECT id FROM products WHERE name = 'Apache Tomcat')),
                                                           ((SELECT id FROM cves WHERE cve_id = 'CVE-2023-34567'), (SELECT id FROM products WHERE name = 'Chrome')),
                                                           ((SELECT id FROM cves WHERE cve_id = 'CVE-2023-45678'), (SELECT id FROM products WHERE name = 'Oracle Database')),
                                                           ((SELECT id FROM cves WHERE cve_id = 'CVE-2023-56789'), (SELECT id FROM products WHERE name = 'Microsoft Edge'));