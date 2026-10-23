-- =============================================================================
-- SCRIPT DE ACTIVACIÓN DE RLS (ROW LEVEL SECURITY) PARA SUPABASE
-- Sistema de Gestión de Biblioteca - UTP
-- =============================================================================
-- Este script activa RLS en todas las tablas de Supabase (eliminando la advertencia
-- roja UNRESTRICTED) y crea políticas permisivas para que Spring Boot y la web
-- operen con normalidad sin bloqueos de permisos.
--
-- INSTRUCCIONES:
-- 1. Abre el panel de Supabase: https://supabase.com/dashboard
-- 2. Ve a la sección "SQL Editor" (menú lateral izquierdo).
-- 3. Pega este contenido y presiona "Run".
-- =============================================================================

-- 1. Habilitar Row Level Security en todas las tablas
ALTER TABLE tbl_autor ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_categoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_contenido_digital ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_devolucion ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_historial_busqueda ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_libro ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_libro_autor ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_libro_categoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_prestamo ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_reserva ENABLE ROW LEVEL SECURITY;
ALTER TABLE tbl_usuario ENABLE ROW LEVEL SECURITY;

-- 2. Crear políticas permisivas de lectura y escritura para cada tabla
-- Esto permite que las consultas SELECT, INSERT, UPDATE y DELETE funcionen sin restricción.
DO $$
DECLARE
    tbl text;
    tablas text[] := ARRAY[
        'tbl_autor',
        'tbl_categoria',
        'tbl_contenido_digital',
        'tbl_devolucion',
        'tbl_historial_busqueda',
        'tbl_libro',
        'tbl_libro_autor',
        'tbl_libro_categoria',
        'tbl_prestamo',
        'tbl_reserva',
        'tbl_usuario'
    ];
BEGIN
    FOREACH tbl IN ARRAY tablas LOOP
        EXECUTE format('DROP POLICY IF EXISTS "Politica_acceso_total_%s" ON %I', tbl, tbl);
        EXECUTE format('CREATE POLICY "Politica_acceso_total_%s" ON %I FOR ALL USING (true) WITH CHECK (true)', tbl, tbl);
    END LOOP;
END $$;
