-- ============================================
-- Fase 2: Migrar passwords a BCrypt
-- ============================================

-- Passwords actuales en texto plano:
-- admin123, research123, analyst123

-- Hashes BCrypt generados (costo 12):
-- admin123    -> $2a$12$GYbx3LjE5CD0q85KBJG8vOEmSSlth20gMIRYR0Dk2XPKAgT9E5tm6
-- research123 -> $2a$12$/vhu62QWhmyWyxyrBCug2uXVr.zmhyrosxYEjyqwDoxUD/716G4kC
-- analyst123  -> $2a$12$vNjs0tlnSJKbQy.peCEPye/FGoVMxJanazdN7m.l4JrdPU2Gcigby

UPDATE users
SET password_hash = CASE username
                        WHEN 'admin' THEN '$2a$12$GYbx3LjE5CD0q85KBJG8vOEmSSlth20gMIRYR0Dk2XPKAgT9E5tm6'
                        WHEN 'researcher' THEN '$2a$12$/vhu62QWhmyWyxyrBCug2uXVr.zmhyrosxYEjyqwDoxUD/716G4kC'
                        WHEN 'analyst' THEN '$2a$12$vNjs0tlnSJKbQy.peCEPye/FGoVMxJanazdN7m.l4JrdPU2Gcigby'
                        ELSE password_hash
    END
WHERE username IN ('admin', 'researcher', 'analyst');

-- Verificar migración
-- SELECT username, LEFT(password_hash, 12) as hash_prefix FROM users;