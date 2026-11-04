-- =============================================================================
-- SCRIPT DE MIGRACIÓN PARA SUPABASE
-- Sistema de Gestión de Biblioteca - UTP
-- =============================================================================

-- 1. Modificar campo 'estado' en la tabla de usuarios a tipo BOOLEAN (0: inactivo, 1: activo)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'tbl_usuario') THEN
        ALTER TABLE tbl_usuario ALTER COLUMN estado DROP DEFAULT;
        ALTER TABLE tbl_usuario ALTER COLUMN estado TYPE BOOLEAN USING (
            CASE 
                WHEN estado::text IN ('1', 'true', 'TRUE', 'ACTIVO', 'activo', 't') THEN true 
                ELSE false 
            END
        );
        ALTER TABLE tbl_usuario ALTER COLUMN estado SET DEFAULT true;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'usuarios') THEN
        ALTER TABLE usuarios ALTER COLUMN estado DROP DEFAULT;
        ALTER TABLE usuarios ALTER COLUMN estado TYPE BOOLEAN USING (
            CASE 
                WHEN estado::text IN ('1', 'true', 'TRUE', 'ACTIVO', 'activo', 't') THEN true 
                ELSE false 
            END
        );
        ALTER TABLE usuarios ALTER COLUMN estado SET DEFAULT true;
    END IF;
END $$;


-- 2. Crear nueva tabla rol (tbl_rol) con los 3 roles oficiales
CREATE TABLE IF NOT EXISTS tbl_rol (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

INSERT INTO tbl_rol (nombre, descripcion) VALUES 
('ADMIN', 'Administrador del sistema de biblioteca'),
('LECTOR', 'Usuario lector de la biblioteca'),
('BIBLIOTECARIO', 'Personal bibliotecario')
ON CONFLICT (nombre) DO NOTHING;

-- 2.1. Restricción en tbl_usuario para que el campo rol SOLO tome los 3 estados de tbl_rol
UPDATE tbl_usuario SET rol = 'ADMIN' WHERE UPPER(TRIM(rol)) = 'ADMINISTRADOR';
UPDATE tbl_usuario SET rol = UPPER(TRIM(rol));

DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_usuario_rol') THEN
        ALTER TABLE tbl_usuario DROP CONSTRAINT fk_usuario_rol;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_usuario_rol') THEN
        ALTER TABLE tbl_usuario DROP CONSTRAINT chk_usuario_rol;
    END IF;

    ALTER TABLE tbl_usuario 
        ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (rol) REFERENCES tbl_rol(nombre) 
        ON UPDATE CASCADE ON DELETE RESTRICT;

    ALTER TABLE tbl_usuario 
        ADD CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN', 'LECTOR', 'BIBLIOTECARIO'));
END $$;


-- 3. ELIMINACIÓN DE LA TABLA INTERMEDIA (tbl_libro_categoria) Y RELACIÓN DIRECTA 1 A N
-- 3.1. Asegurar columna id_categoria en tbl_libro
ALTER TABLE IF EXISTS tbl_libro ADD COLUMN IF NOT EXISTS id_categoria INTEGER;

-- 3.2. Migrar relaciones de tbl_libro_categoria hacia tbl_libro (si la tabla intermedia aún existe)
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'tbl_libro_categoria') THEN
        UPDATE tbl_libro l
        SET id_categoria = lc.id_categoria
        FROM tbl_libro_categoria lc
        WHERE l.id_libro = lc.id_libro AND l.id_categoria IS NULL;
    END IF;
END $$;

-- 3.3. ELIMINAR definitivamente la tabla intermedia tbl_libro_categoria
DROP TABLE IF EXISTS tbl_libro_categoria CASCADE;

-- 3.4. Restricción Foreign Key directa entre tbl_libro y tbl_categoria (1 a N)
DO $$
BEGIN
    ALTER TABLE tbl_libro DROP CONSTRAINT IF EXISTS fkk0qwejyncoxt5ltr95tx2g57m;
    ALTER TABLE tbl_libro DROP CONSTRAINT IF EXISTS fk_libro_categoria;
    ALTER TABLE tbl_libro 
        ADD CONSTRAINT fk_libro_categoria 
        FOREIGN KEY (id_categoria) REFERENCES tbl_categoria(id_categoria) 
        ON DELETE SET NULL;
END $$;

-- 3.5. Restricciones de borrado en cascada en las entidades hijas de tbl_libro
ALTER TABLE IF EXISTS tbl_libro_autor DROP CONSTRAINT IF EXISTS fk7lixlvd7mqftcgtnnaoyxf8rq;
ALTER TABLE IF EXISTS tbl_libro_autor ADD CONSTRAINT fk7lixlvd7mqftcgtnnaoyxf8rq 
    FOREIGN KEY (id_libro) REFERENCES tbl_libro(id_libro) ON DELETE CASCADE;

ALTER TABLE IF EXISTS tbl_contenido_digital DROP CONSTRAINT IF EXISTS fkd9w0u64lk8yfimd8nb9r9nndo;
ALTER TABLE IF EXISTS tbl_contenido_digital ADD CONSTRAINT fkd9w0u64lk8yfimd8nb9r9nndo 
    FOREIGN KEY (id_libro) REFERENCES tbl_libro(id_libro) ON DELETE CASCADE;

ALTER TABLE IF EXISTS tbl_prestamo DROP CONSTRAINT IF EXISTS fk94c3eav5pgwu6s9abdf0h5c56;
ALTER TABLE IF EXISTS tbl_prestamo ADD CONSTRAINT fk94c3eav5pgwu6s9abdf0h5c56 
    FOREIGN KEY (id_libro) REFERENCES tbl_libro(id_libro) ON DELETE CASCADE;

ALTER TABLE IF EXISTS tbl_reserva DROP CONSTRAINT IF EXISTS fk4u90v1h9sg5cyrkn12h37over;
ALTER TABLE IF EXISTS tbl_reserva ADD CONSTRAINT fk4u90v1h9sg5cyrkn12h37over 
    FOREIGN KEY (id_libro) REFERENCES tbl_libro(id_libro) ON DELETE CASCADE;

-- 3.6. Restricción de devolución hacia préstamo
ALTER TABLE IF EXISTS tbl_devolucion DROP CONSTRAINT IF EXISTS fkh0prf0lujjmeykxjrmr6if1q5;
ALTER TABLE IF EXISTS tbl_devolucion ADD CONSTRAINT fkh0prf0lujjmeykxjrmr6if1q5 
    FOREIGN KEY (id_prestamo) REFERENCES tbl_prestamo(id_prestamo) ON DELETE CASCADE;

-- 4. Habilitar RLS en tbl_rol
ALTER TABLE IF EXISTS tbl_rol ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Politica_acceso_total_tbl_rol" ON tbl_rol;
CREATE POLICY "Politica_acceso_total_tbl_rol" ON tbl_rol FOR ALL USING (true) WITH CHECK (true);
