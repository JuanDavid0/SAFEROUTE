-- ======================================================
-- 1. DEFINICIÓN DE DOMINIOS
-- ======================================================

-- Dominio para los tipos de rol en el sistema
CREATE DOMAIN TIPO_ROL_DOM AS VARCHAR(3) NOT NULL
    CHECK (VALUE IN ('ADM', 'SAD', 'CLI')); -- ADM: Administrador, SAD: Super Administrador, CLI: Cliente

-- Dominio para los estados de una solicitud
CREATE DOMAIN ESTADO_SOLICITUD_DOM AS VARCHAR(3) NOT NULL
    CHECK (VALUE IN ('PDP', 'PGD', 'CAN')); -- PDP: Pendiente de pago, PGD: Pagada, CAN: Cancelada

-- Dominio para los estados de un pedido (catálogo)
CREATE DOMAIN ESTADO_PEDIDO_DOM AS VARCHAR(3) NOT NULL
    CHECK (VALUE IN ('CRT', 'ACT', 'CRM', 'CRA', 'PRD', 'RCP', 'RTA', 'ADU', 'ENT'));

-- ======================================================
-- 2. TABLAS MAESTRAS
-- ======================================================

CREATE TABLE ROL (
    id_rol SERIAL PRIMARY KEY,
    tipo_rol TIPO_ROL_DOM UNIQUE,
    descripcion_rol VARCHAR(150)
);

CREATE TABLE USUARIO (
    id_usuario SERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(15) NOT NULL UNIQUE,
    cedula VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(150) NOT NULL,
    contrasenia VARCHAR(512),
    estado_usuario VARCHAR(10) NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE PRODUCTO (
    id_producto SERIAL PRIMARY KEY,
    nombre_producto VARCHAR(100) NOT NULL,
    tipo_producto VARCHAR(100) NOT NULL,
    descripcion_producto TEXT NOT NULL,
    precio_unitario NUMERIC(10,2) NOT NULL,
    costo_unitario NUMERIC(10,2) NOT NULL,
    url_imagen VARCHAR(255),
    estado_producto VARCHAR(10) NOT NULL DEFAULT 'ACTIVO'
);

-- ======================================================
-- 3. TABLAS RELACIONALES Y TRANSACCIONALES
-- ======================================================

CREATE TABLE USUARIO_ROL (
    id_rol INTEGER NOT NULL,
    id_usuario INTEGER NOT NULL,
    PRIMARY KEY (id_rol, id_usuario),
    CONSTRAINT USROL_FK_ID_ROL FOREIGN KEY (id_rol)
        REFERENCES ROL(id_rol) ON DELETE CASCADE,
    CONSTRAINT USROL_FK_ID_USR FOREIGN KEY (id_usuario)
        REFERENCES USUARIO(id_usuario) ON DELETE CASCADE
);

CREATE TABLE LOG (
    id_log SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    accion VARCHAR(255) NOT NULL,
    fecha_log TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT LOG_FK_IDUS FOREIGN KEY (id_usuario)
        REFERENCES USUARIO(id_usuario) ON DELETE SET NULL
);

CREATE TABLE PEDIDO (
    id_pedido SERIAL PRIMARY KEY,
    id_admin INTEGER NOT NULL,
    estado_pedido ESTADO_PEDIDO_DOM DEFAULT 'CRT',
    fecha_creado DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_cierre DATE,
    url_hash VARCHAR(64) UNIQUE,
    CONSTRAINT PED_FK_ID_ADMIN FOREIGN KEY (id_admin)
        REFERENCES USUARIO(id_usuario)
);

CREATE TABLE SOLICITUD (
    id_solicitud SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL,
    id_pedido INTEGER NOT NULL,
    fecha_solicitud DATE NOT NULL DEFAULT CURRENT_DATE,
    estado_solicitud ESTADO_SOLICITUD_DOM DEFAULT 'PDP',
    direccion_entrega VARCHAR(255) NOT NULL DEFAULT '',
    modificaciones_restantes INTEGER NOT NULL DEFAULT 3,
    CONSTRAINT SOL_FK_ID_CLI FOREIGN KEY (id_cliente)
        REFERENCES USUARIO(id_usuario),
    CONSTRAINT SOL_FK_ID_PED FOREIGN KEY (id_pedido)
        REFERENCES PEDIDO(id_pedido)
);

CREATE TABLE PRODUCTO_PEDIDO (
    id_pedido INTEGER NOT NULL,
    id_producto INTEGER NOT NULL,
    cantidad_min INTEGER NOT NULL DEFAULT 1 CHECK (cantidad_min > 0),
    cantidad_max INTEGER CHECK (cantidad_max > 0),
    PRIMARY KEY (id_pedido, id_producto),
    CONSTRAINT PP_FK_ID_PED FOREIGN KEY (id_pedido)
        REFERENCES PEDIDO(id_pedido) ON DELETE CASCADE,
    CONSTRAINT PP_FK_ID_PRO FOREIGN KEY (id_producto)
        REFERENCES PRODUCTO(id_producto) ON DELETE CASCADE,
    CONSTRAINT chk_cantidad_max_min CHECK (cantidad_max IS NULL OR cantidad_max >= cantidad_min) 
);

CREATE TABLE SOLICITUD_PRODUCTO (
    id_solicitud INTEGER NOT NULL,
    id_producto INTEGER NOT NULL,
    cantidad_solicitada INTEGER NOT NULL CHECK (cantidad_solicitada > 0),
    precio NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (id_solicitud, id_producto),
    CONSTRAINT SP_FK_ID_SOL FOREIGN KEY (id_solicitud)
        REFERENCES SOLICITUD(id_solicitud) ON DELETE CASCADE,
    CONSTRAINT SP_FK_ID_PRO FOREIGN KEY (id_producto)
        REFERENCES PRODUCTO(id_producto) ON DELETE CASCADE
);

-- ======================================================
-- 4. TABLA OTP (TOKEN DE VERIFICACIÓN)
-- ======================================================

CREATE TABLE OTP_TOKEN (
    id_otp SERIAL PRIMARY KEY,
    cedula VARCHAR(10) NOT NULL,
    telefono VARCHAR(10) NOT NULL,
    codigo_otp VARCHAR(6) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    verificado BOOLEAN NOT NULL DEFAULT FALSE,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_fechas_otp CHECK (fecha_expiracion > fecha_creacion)
);

-- Índices para OTP
CREATE INDEX idx_otp_cedula_usado_expiracion
    ON OTP_TOKEN(cedula, usado, fecha_expiracion);

CREATE INDEX idx_otp_fecha_expiracion
    ON OTP_TOKEN(fecha_expiracion);

-- ======================================================
-- 5. ÍNDICES PARA OPTIMIZACIÓN GENERAL
-- ======================================================

CREATE INDEX idx_log_usuario ON LOG(id_usuario);
CREATE INDEX idx_log_fecha ON LOG(fecha_log);

CREATE INDEX idx_pedido_admin ON PEDIDO(id_admin);
CREATE INDEX idx_pedido_estado ON PEDIDO(estado_pedido);
CREATE INDEX idx_pedido_url_hash ON PEDIDO(url_hash);

CREATE INDEX idx_solicitud_cliente ON SOLICITUD(id_cliente);
CREATE INDEX idx_solicitud_pedido ON SOLICITUD(id_pedido);
CREATE INDEX idx_solicitud_estado ON SOLICITUD(estado_solicitud);

CREATE INDEX idx_pp_pedido ON PRODUCTO_PEDIDO(id_pedido);
CREATE INDEX idx_pp_producto ON PRODUCTO_PEDIDO(id_producto);

CREATE INDEX idx_sp_solicitud ON SOLICITUD_PRODUCTO(id_solicitud);
CREATE INDEX idx_sp_producto ON SOLICITUD_PRODUCTO(id_producto);

-- ======================================================
-- 6. VALORES POR DEFECTO (ACTUALIZACIONES INICIALES)
-- ======================================================

UPDATE USUARIO SET estado_usuario = 'ACTIVO' WHERE estado_usuario IS NULL;
UPDATE PRODUCTO SET estado_producto = 'ACTIVO' WHERE estado_producto IS NULL;

-- ======================================================
-- Insertar datos iniciales en la tabla ROL
-- ======================================================
INSERT INTO ROL (tipo_rol, descripcion_rol) VALUES
    ('ADM', 'Administrador del sistema con permisos limitados'),
    ('SAD', 'Super Administrador con todos los permisos'),
    ('CLI', 'Cliente que realiza pedidos');
-- ======================================================


-- ======================================================
-- 7. INSERCIÓN DE USUARIO SUPER ADMINISTRADOR (SAD)
-- ======================================================

INSERT INTO USUARIO (
    nombres, apellidos, telefono, cedula, direccion, contrasenia, estado_usuario
) VALUES (
    'Super',
    'Usuario',
    '3133121509',
    '9999999999',
    'Oficina SafeRoute',
    '$2a$10$wyuI6cOYhEWUapU5aLrhKeymRXrjW9t/dy4gkggyhMICnra98Rwh2',
    'ACTIVO'
);

-- ======================================================
-- 8. ASIGNACIÓN DE ROL SUPER ADMINISTRADOR AL USUARIO
-- ======================================================

INSERT INTO USUARIO_ROL (id_rol, id_usuario)
SELECT 
    r.id_rol, u.id_usuario
FROM ROL r, USUARIO u
WHERE r.tipo_rol = 'SAD' AND u.cedula = '9999999999';

-- ======================================================
-- 9. INSERCIÓN DE PRODUCTOS DE IMPORTACIONES
-- ======================================================

INSERT INTO PRODUCTO (
    nombre_producto, tipo_producto, descripcion_producto, 
    precio_unitario, costo_unitario, url_imagen, estado_producto
) VALUES
    ('Smartwatch Pro X10', 'Electrónicos', 'Reloj inteligente con monitor cardíaco y GPS integrado', 480000, 320000, 'https://example.com/img/smartwatch.jpg', 'ACTIVO'),
    ('Auriculares Inalámbricos AirBeats', 'Electrónicos', 'Auriculares Bluetooth con cancelación de ruido', 250000, 160000, 'https://example.com/img/airbeats.jpg', 'ACTIVO'),
    ('Cámara de Seguridad 360°', 'Seguridad', 'Cámara IP con visión nocturna y detección de movimiento', 370000, 250000, 'https://example.com/img/camara360.jpg', 'ACTIVO'),
    ('Cargador Rápido Universal', 'Accesorios', 'Cargador con puerto USB-C y compatibilidad universal', 90000, 50000, 'https://example.com/img/cargador.jpg', 'ACTIVO'),
    ('Power Bank 20000mAh', 'Accesorios', 'Batería portátil con doble salida USB', 150000, 95000, 'https://example.com/img/powerbank.jpg', 'ACTIVO'),
    ('Teclado Mecánico RGB', 'Computación', 'Teclado mecánico retroiluminado con switches azules', 280000, 190000, 'https://example.com/img/teclado.jpg', 'ACTIVO'),
    ('Mouse Inalámbrico Pro', 'Computación', 'Mouse ergonómico con sensor óptico de alta precisión', 120000, 70000, 'https://example.com/img/mouse.jpg', 'ACTIVO'),
    ('Mini Proyector LED', 'Electrónicos', 'Proyector portátil HD compatible con HDMI y USB', 550000, 380000, 'https://example.com/img/proyector.jpg', 'ACTIVO'),
    ('Balanza Digital Portátil', 'Hogar', 'Báscula precisa para maletas y paquetes', 85000, 50000, 'https://example.com/img/balanza.jpg', 'ACTIVO'),
    ('Altavoz Bluetooth Portátil', 'Electrónicos', 'Altavoz resistente al agua con sonido envolvente', 200000, 130000, 'https://example.com/img/altavoz.jpg', 'ACTIVO');

-- ======================================================
-- FIN DE INSERCIONES INICIALES
-- ======================================================
-- ======================================================
-- 10. INSERCIÓN DE USUARIOS CLIENTE (ROL CLI)
-- ======================================================

-- Usuarios tipo Cliente
INSERT INTO USUARIO (
    nombres, apellidos, telefono, cedula, direccion, estado_usuario
) VALUES
    ('Andres', 'Maldonado', '3103442878', '1001001001', 'Calle 12 #45-67, Paipa', 'ACTIVO'),

    ('William', 'Cely', '3133620731', '1002002002', 'Carrera 8 #23-90, Samaca','ACTIVO');

-- ======================================================
-- 11. ASIGNACIÓN DE ROL CLIENTE A LOS USUARIOS
-- ======================================================

INSERT INTO USUARIO_ROL (id_rol, id_usuario)
SELECT
    r.id_rol, u.id_usuario
FROM ROL r
JOIN USUARIO u ON u.cedula IN ('1001001001', '1002002002')
WHERE r.tipo_rol = 'CLI';

-- ======================================================
-- FIN DE INSERCIÓN DE CLIENTES
-- ======================================================

-- ======================================================
-- 12. INSERCIÓN DE UN PEDIDO DE PRUEBA
-- ======================================================

-- Crear pedido asociado al Super Administrador (SAD)
INSERT INTO PEDIDO (
    id_admin, estado_pedido, fecha_creado, fecha_cierre, url_hash
)
SELECT
    u.id_usuario,
    'ACT',
    DATE '2025-10-01',
    DATE '2025-10-28',
    'SafeRtQX9zPdK123'
FROM USUARIO u
WHERE u.cedula = '9999999999';


-- ======================================================
-- 13. INSERCIÓN DE SOLICITUDES RELACIONADAS AL PEDIDO
-- ======================================================

-- Solicitud 1 - Cliente Laura Martínez (pagada)
INSERT INTO SOLICITUD (
    id_cliente, id_pedido, fecha_solicitud, estado_solicitud, direccion_entrega, modificaciones_restantes
)
SELECT
    u.id_usuario,
    p.id_pedido,
    DATE '2025-10-29',
    'PGD',
    'Paipa',
    3
FROM USUARIO u, PEDIDO p
WHERE u.cedula = '1001001001' AND p.url_hash = 'SafeRtQX9zPdK123';

-- Solicitud 2 - Cliente Carlos García (pendiente de pago)
INSERT INTO SOLICITUD (
    id_cliente, id_pedido, fecha_solicitud, estado_solicitud, direccion_entrega, modificaciones_restantes
)
SELECT
    u.id_usuario,
    p.id_pedido,
    DATE '2025-10-29',
    'PDP',
    'Tunja',
    3
FROM USUARIO u, PEDIDO p
WHERE u.cedula = '1002002002' AND p.url_hash = 'SafeRtQX9zPdK123';


-- ======================================================
-- 14. INSERCIÓN DE PRODUCTOS ASOCIADOS AL PEDIDO
-- ======================================================

-- Relacionar algunos productos del catálogo con el pedido
INSERT INTO PRODUCTO_PEDIDO (id_pedido, id_producto, cantidad_min, cantidad_max)
SELECT
    p.id_pedido, pr.id_producto, 10, 500
FROM PEDIDO p, PRODUCTO pr
WHERE p.url_hash = 'SafeRtQX9zPdK123'
  AND pr.id_producto IN (
      (SELECT id_producto FROM PRODUCTO WHERE nombre_producto = 'Altavoz Bluetooth Portátil' LIMIT 1),
      (SELECT id_producto FROM PRODUCTO WHERE nombre_producto = 'Cámara de Seguridad 360°' LIMIT 1)
  );


-- ======================================================
-- 15. INSERCIÓN DE PRODUCTOS SOLICITADOS POR CLIENTES
-- ======================================================

-- Solicitud de Laura Martínez (Paipa)
INSERT INTO SOLICITUD_PRODUCTO (id_solicitud, id_producto, cantidad_solicitada, precio)
SELECT
    s.id_solicitud,
    pr.id_producto,
    150,
    pr.precio_unitario
FROM SOLICITUD s, PRODUCTO pr
WHERE s.direccion_entrega = 'Paipa'
  AND pr.nombre_producto = 'Altavoz Bluetooth Portátil';

INSERT INTO SOLICITUD_PRODUCTO (id_solicitud, id_producto, cantidad_solicitada, precio)
SELECT
    s.id_solicitud,
    pr.id_producto,
    120,
    pr.precio_unitario
FROM SOLICITUD s, PRODUCTO pr
WHERE s.direccion_entrega = 'Paipa'
  AND pr.nombre_producto = 'Cámara de Seguridad 360°';


-- Solicitud de Carlos García (Tunja)
INSERT INTO SOLICITUD_PRODUCTO (id_solicitud, id_producto, cantidad_solicitada, precio)
SELECT
    s.id_solicitud,
    pr.id_producto,
    90,
    pr.precio_unitario
FROM SOLICITUD s, PRODUCTO pr
WHERE s.direccion_entrega = 'Tunja'
  AND pr.nombre_producto = 'Altavoz Bluetooth Portátil';

-- ======================================================
-- FIN DE INSERCIÓN DE PEDIDO Y SOLICITUDES
-- ======================================================