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
    correo VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(15) NOT NULL UNIQUE,
    cedula VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(150) NOT NULL,
    contrasenia VARCHAR(512)
);

CREATE TABLE PRODUCTO (
    id_producto SERIAL PRIMARY KEY,
    nombre_producto VARCHAR(100) NOT NULL,
    tipo_producto VARCHAR(100) NOT NULL,
    descripcion_producto TEXT NOT NULL,
    precio_unitario NUMERIC(10,2) NOT NULL,
    costo_unitario NUMERIC(10,2) NOT NULL
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
    id_usuario INTEGER NOT NULL,
    accion VARCHAR(50) NOT NULL,
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
    CONSTRAINT PED_FK_ID_ADMIN FOREIGN KEY (id_admin)
        REFERENCES USUARIO(id_usuario)
);

CREATE TABLE SOLICITUD (
    id_solicitud SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL,
    id_pedido INTEGER NOT NULL,
    fecha_solicitud DATE NOT NULL DEFAULT CURRENT_DATE,
    estado_solicitud ESTADO_SOLICITUD_DOM DEFAULT 'PDP',
    direccion_entrega VARCHAR(255) NOT NULL,
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
    modificaciones_restantes INTEGER NOT NULL DEFAULT 3 CHECK (modificaciones_restantes >= 0),
    PRIMARY KEY (id_solicitud, id_producto),
    CONSTRAINT SP_FK_ID_SOL FOREIGN KEY (id_solicitud)
        REFERENCES SOLICITUD(id_solicitud) ON DELETE CASCADE,
    CONSTRAINT SP_FK_ID_PRO FOREIGN KEY (id_producto)
        REFERENCES PRODUCTO(id_producto) ON DELETE CASCADE
);

-- ======================================================
-- 4. ÍNDICES PARA OPTIMIZACIÓN
-- ======================================================

CREATE INDEX idx_usuario_correo ON USUARIO(correo);
CREATE INDEX idx_log_usuario ON LOG(id_usuario);
CREATE INDEX idx_log_fecha ON LOG(fecha_log);
CREATE INDEX idx_pedido_admin ON PEDIDO(id_admin);
CREATE INDEX idx_pedido_estado ON PEDIDO(estado_pedido);
CREATE INDEX idx_solicitud_cliente ON SOLICITUD(id_cliente);
CREATE INDEX idx_solicitud_pedido ON SOLICITUD(id_pedido);
CREATE INDEX idx_solicitud_estado ON SOLICITUD(estado_solicitud);
CREATE INDEX idx_pp_pedido ON PRODUCTO_PEDIDO(id_pedido);
CREATE INDEX idx_pp_producto ON PRODUCTO_PEDIDO(id_producto);
CREATE INDEX idx_sp_solicitud ON SOLICITUD_PRODUCTO(id_solicitud);
CREATE INDEX idx_sp_producto ON SOLICITUD_PRODUCTO(id_producto);