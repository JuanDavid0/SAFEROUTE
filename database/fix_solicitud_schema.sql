-- ======================================================
-- Script de corrección para alinear la BD con las entidades Java
-- Ejecutar este script en PostgreSQL antes de iniciar la aplicación
-- ======================================================

-- 1. Agregar estado CAN (Cancelada) al dominio ESTADO_SOLICITUD_DOM
ALTER DOMAIN ESTADO_SOLICITUD_DOM DROP CONSTRAINT estado_solicitud_dom_check;
ALTER DOMAIN ESTADO_SOLICITUD_DOM ADD CONSTRAINT estado_solicitud_dom_check 
    CHECK (VALUE IN ('PDP', 'PGD', 'CAN'));

-- 2. Agregar columna direccion_entrega a la tabla SOLICITUD
ALTER TABLE SOLICITUD 
ADD COLUMN IF NOT EXISTS direccion_entrega VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE SOLICITUD_PRODUCTO 
ADD COLUMN IF NOT EXISTS modificaciones_restantes INTEGER NOT NULL DEFAULT 3;

-- 3. Verificar la estructura final
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'solicitud' 
ORDER BY ordinal_position;
