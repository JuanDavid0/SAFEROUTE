# 📚 SafeRoute - Documentación de API

> **Versión**: 1.0.0  
> **Base URL**: `http://localhost:8080/api`  
> **Última actualización**: Octubre 2025

---

## Tabla de Contenidos

1. [Autenticación](#-autenticación)
2. [Gestión de Usuarios](#-gestión-de-usuarios)
3. [Gestión de Productos](#-gestión-de-productos)
4. [Gestión de Pedidos](#-gestión-de-pedidos)
5. [Gestión de Solicitudes](#-gestión-de-solicitudes)
6. [Consolidación](#-consolidación)
7. [Reportes y Estadísticas](#-reportes-y-estadísticas)
8. [Informes Excel](#-informes-excel)
9. [Etiquetas](#-etiquetas)
10. [Autenticación OTP (Twilio)](#-autenticación-otp-twilio)
11. [Códigos de Respuesta](#-códigos-de-respuesta)

---

## Autenticación

### Registro de Superadministrador

Crea el primer usuario del sistema con rol de Superadministrador.

**Endpoint**: `POST /auth/registro`

**Autenticación**: No requerida

**Request Body**:
```json
{
  "nombres": "Super",
  "apellidos": "Usuario",
  "telefono": "3133121509",
  "cedula": "9999999999",
  "direccion": "Oficina SafeRoute",
  "contrasenia": "SuperAdmin123!"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Usuario registrado exitosamente",
  "data": {
    "idUsuario": 1,
    "nombres": "Super",
    "apellidos": "Usuario",
    "telefono": "3133121509",
    "cedula": "9999999999",
    "direccion": "Oficina SafeRoute",
    "estadoUsuario": "ACTIVO"
  },
  "timestamp": "2025-10-30 10:00:00"
}
```

---

### Crear Administrador

Solo superadministradores pueden crear nuevos administradores.

**Endpoint**: `POST /auth/crear-administrador`

**Autenticación**: Bearer Token (SAD)

**Request Body**:
```json
{
  "nombres": "Nuevo",
  "apellidos": "Admin",
  "telefono": "3213456621",
  "cedula": "9638527410",
  "direccion": "N/A",
  "contrasenia": "Admin123!"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Administrador creado exitosamente",
  "data": {
    "idUsuario": 2,
    "nombres": "Nuevo",
    "apellidos": "Admin",
    "telefono": "3213456621",
    "cedula": "9638527410",
    "direccion": "N/A",
    "estadoUsuario": "ACTIVO"
  },
  "timestamp": "2025-10-30 10:00:00"
}
```

---

### Login

Autenticación de usuarios (SAD/ADM).

**Endpoint**: `POST /auth/login`

**Autenticación**: No requerida

**Request Body**:
```json
{
  "cedula": "9999999999",
  "contrasenia": "SuperAdmin123!"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Inicio de sesión exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "rol": "SAD",
    "idUsuario": 1
  },
  "timestamp": "2025-10-30 10:00:00"
}
```

---

### Cambiar Contraseña

Permite al usuario autenticado cambiar su contraseña.

**Endpoint**: `POST /auth/cambiar-contrasenia`

**Autenticación**: Bearer Token (SAD/ADM)

**Request Body**:
```json
{
  "cedula": "9638527410",
  "contraseniaActual": "Admin123!",
  "contraseniaNueva": "PasswordNueva!"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Contraseña cambiada exitosamente",
  "timestamp": "2025-10-30 10:00:00"
}
```

---

##  Gestión de Usuarios

### Obtener Todos los Usuarios

Lista todos los usuarios del sistema (solo SAD).

**Endpoint**: `GET /usuarios`

**Autenticación**: Bearer Token (SAD)

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Usuarios obtenidos exitosamente",
  "data": [
    {
      "idUsuario": 1,
      "cedula": "9999999999",
      "nombres": "Super",
      "apellidos": "Usuario",
      "telefono": "3133121509",
      "direccion": "Oficina SafeRoute",
      "rol": "SAD"
    },
    {
      "idUsuario": 2,
      "cedula": "9638527410",
      "nombres": "Nuevo",
      "apellidos": "Admin",
      "telefono": "3213456621",
      "direccion": "N/A",
      "rol": "ADM"
    }
  ]
}
```

---

### Eliminar Administrador

Elimina un administrador del sistema (solo SAD).

**Endpoint**: `DELETE /usuarios/{id}`

**Autenticación**: Bearer Token (SAD)

**Parámetros URL**:
- `id` (number): ID del usuario a eliminar

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Usuario eliminado exitosamente"
}
```

---

## Gestión de Productos

### Crear Producto

Crea un nuevo producto con imagen.

**Endpoint**: `POST /productos`

**Autenticación**: Bearer Token (SAD/ADM)

**Content-Type**: `multipart/form-data`

**Form Data**:
- `producto` (JSON string):
  ```json
  {
    "nombreProducto": "Mesa",
    "tipoProducto": "casa",
    "descripcionProducto": "muy comoda",
    "precioUnitario": 100,
    "costoUnitario": 150
  }
  ```
- `imagen` (file): Archivo de imagen (JPG, PNG)

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto creado exitosamente",
  "data": {
    "idProducto": 1,
    "nombreProducto": "Mesa",
    "tipoProducto": "casa",
    "descripcionProducto": "muy comoda",
    "precioUnitario": 100.0,
    "costoUnitario": 150.0,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  }
}
```

---

### Listar Productos

Obtiene todos los productos disponibles.

**Endpoint**: `GET /productos`

**Autenticación**: Bearer Token (SAD/ADM)

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Productos obtenidos exitosamente",
  "data": [
    {
      "idProducto": 1,
      "nombreProducto": "Mesa",
      "tipoProducto": "casa",
      "descripcionProducto": "muy comoda",
      "precioUnitario": 100.0,
      "costoUnitario": 150.0,
      "urlImagen": "https://firebasestorage.googleapis.com/..."
    }
  ]
}
```

---

### Obtener Producto por ID

Obtiene los detalles de un producto específico.

**Endpoint**: `GET /productos/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del producto

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto obtenido exitosamente",
  "data": {
    "idProducto": 20,
    "nombreProducto": "Mesa",
    "tipoProducto": "casa",
    "descripcionProducto": "muy comoda",
    "precioUnitario": 100.0,
    "costoUnitario": 150.0,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  }
}
```

---

### Actualizar Producto

Actualiza un producto existente (incluye imagen opcional).

**Endpoint**: `PUT /productos/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Content-Type**: `multipart/form-data`

**Parámetros URL**:
- `id` (number): ID del producto a actualizar

**Form Data**:
- `producto` (JSON string):
  ```json
  {
    "nombreProducto": "Leche Premium",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche premium 1L",
    "precioUnitario": 6.50,
    "costoUnitario": 4.00
  }
  ```
- `imagen` (file, opcional): Nueva imagen del producto

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto actualizado exitosamente",
  "data": {
    "idProducto": 13,
    "nombreProducto": "Leche Premium",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche premium 1L",
    "precioUnitario": 6.50,
    "costoUnitario": 4.00,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  }
}
```

---

### Eliminar Producto

Elimina un producto del sistema.

**Endpoint**: `DELETE /productos/{id}`

**Autenticación**: Bearer Token (SAD)

**Parámetros URL**:
- `id` (number): ID del producto a eliminar

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto eliminado exitosamente"
}
```

---

### Buscar Productos por Nombre

Busca productos que coincidan con el nombre especificado.

**Endpoint**: `GET /productos/buscar`

**Autenticación**: Bearer Token (SAD/ADM)

**Query Parameters**:
- `nombre` (string): Término de búsqueda

**Ejemplo**: `GET /productos/buscar?nombre=Mesa`

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Productos encontrados",
  "data": [
    {
      "idProducto": 1,
      "nombreProducto": "Mesa",
      "tipoProducto": "casa",
      "precioUnitario": 100.0
    }
  ]
}
```

---

## Gestión de Pedidos

### Crear Pedido

Crea un nuevo pedido con productos y fecha de cierre.

**Endpoint**: `POST /pedidos/{idAdmin}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `idAdmin` (number): ID del administrador que crea el pedido

**Request Body**:
```json
{
  "productos": [
    {
      "idProducto": 9,
      "cantidadMin": 100,
      "cantidadMax": 500
    }
  ],
  "fechaCierre": "2025-12-30"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Pedido creado exitosamente",
  "data": {
    "idPedido": 1,
    "idAdmin": 22,
    "estadoPedido": "CRT",
    "fechaCreado": "2025-10-30 10:00:00",
    "fechaCierre": "2025-12-30 23:59:59",
    "urlHash": "Y0p1QOGXRmlzE76",
    "productos": [
      {
        "idProducto": 9,
        "cantidadMin": 100,
        "cantidadMax": 500,
        "nombreProducto": "Producto Ejemplo",
        "precioUnitario": 100.0
      }
    ]
  }
}
```

---

### Listar Todos los Pedidos

Obtiene todos los pedidos del sistema.

**Endpoint**: `GET /pedidos`

**Autenticación**: Bearer Token (SAD/ADM)

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Pedidos obtenidos exitosamente",
  "data": [
    {
      "idPedido": 1,
      "idAdmin": 22,
      "estadoPedido": "ACT",
      "fechaCreado": "2025-10-30 10:00:00",
      "fechaCierre": "2025-12-30 23:59:59",
      "urlHash": "Y0p1QOGXRmlzE76",
      "productos": [...]
    }
  ]
}
```

---

### Obtener Pedido por ID

Obtiene los detalles de un pedido específico.

**Endpoint**: `GET /pedidos/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Pedido obtenido exitosamente",
  "data": {
    "idPedido": 39,
    "idAdmin": 22,
    "estadoPedido": "ACT",
    "fechaCreado": "2025-10-30 10:00:00",
    "fechaCierre": "2025-12-30 23:59:59",
    "urlHash": "Y0p1QOGXRmlzE76",
    "productos": [...]
  }
}
```

---

### Obtener Pedido por Hash (Público)

Obtiene información de un pedido disponible usando su hash (no requiere autenticación).

**Endpoint**: `GET /pedidos/pedido-disponible/{hash}`

**Autenticación**: No requerida

**Parámetros URL**:
- `hash` (string): Hash único del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Pedido disponible obtenido exitosamente",
  "data": {
    "idPedido": 1,
    "idAdmin": 22,
    "estadoPedido": "ACT",
    "fechaCreado": "2025-10-30 10:00:00",
    "fechaCierre": "2025-12-30 23:59:59",
    "urlHash": "Y0p1QOGXRmlzE76",
    "productos": [
      {
        "idProducto": 9,
        "cantidadMin": 100,
        "cantidadMax": 500
      }
    ]
  }
}
```

---

### Actualizar Estado del Pedido

Cambia el estado de un pedido según las transiciones permitidas.

**Endpoint**: `PUT /pedidos/{id}/estado/{nuevoEstado}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido
- `nuevoEstado` (string): Nuevo estado del pedido

**Estados Válidos**:
- `CRT`: Creado
- `ACT`: Activo
- `CRM`: Cerrado Manual
- `CRA`: Cerrado Automático
- `PRD`: Perdido
- `RCP`: Recuperado
- `RTA`: En Ruta
- `ADU`: Aduana
- `ENT`: Entregado

**Ejemplo**: `PUT /pedidos/34/estado/ACT`

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Estado del pedido actualizado exitosamente",
  "data": {
    "idPedido": 34,
    "estadoPedido": "ACT",
    ...
  }
}
```

---

### Actualizar Fecha de Cierre

Actualiza la fecha de cierre de un pedido activo.

**Endpoint**: `PUT /pedidos/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Request Body**:
```json
{
  "fechaCierre": "2026-01-15"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Fecha de cierre actualizada exitosamente",
  "data": {
    "idPedido": 13,
    "fechaCierre": "2026-01-15 23:59:59",
    ...
  }
}
```

---

### Cerrar Pedido Manualmente

Cierra un pedido de forma manual.

**Endpoint**: `DELETE /pedidos/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Pedido cerrado exitosamente"
}
```

---

### Agregar Producto al Pedido

Agrega un nuevo producto a un pedido existente.

**Endpoint**: `POST /pedidos/{id}/productos`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Request Body**:
```json
{
  "idProducto": 11,
  "cantidadMin": 50,
  "cantidadMax": 500
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto agregado al pedido exitosamente",
  "data": {
    "idPedido": 16,
    "productos": [...]
  }
}
```

---

### Actualizar Cantidades de un Producto en Pedido

Modifica las cantidades mínimas y/o máximas de un producto en el pedido.

**Endpoint**: `PUT /pedidos/{idPedido}/productos/{idProducto}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `idPedido` (number): ID del pedido
- `idProducto` (number): ID del producto

**Request Body**:
```json
{
  "cantidadMin": 250
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Cantidades actualizadas exitosamente",
  "data": {
    "idPedido": 23,
    "productos": [...]
  }
}
```

---

### Eliminar Producto del Pedido

Elimina un producto de un pedido.

**Endpoint**: `DELETE /pedidos/{idPedido}/productos/{idProducto}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `idPedido` (number): ID del pedido
- `idProducto` (number): ID del producto

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto eliminado del pedido exitosamente"
}
```

---

## Gestión de Solicitudes

### Crear Solicitud (Cliente Nuevo)

Crea una nueva solicitud sin necesidad de autenticación (público).

**Endpoint**: `POST /solicitudes/public/nueva`

**Autenticación**: No requerida

**Request Body**:
```json
{
  "nombres": "don",
  "apellidos": "Maldito",
  "direccion": "Calle 123 #45-67",
  "cedula": "4512639685",
  "telefono": "1524658865",
  "idPedido": 28,
  "productos": [
    {
      "idProducto": 9,
      "cantidadSolicitada": 222
    }
  ]
}
```

**Response Success** (200):
```json
{
  "exito": true,
  "mensaje": "Solicitud creada exitosamente",
  "datos": {
    "idSolicitud": 1,
    "idCliente": 25,
    "nombreCliente": "don Maldito",
    "idPedido": 28,
    "estadoSolicitud": "PDP",
    "direccionEntrega": "Calle 123 #45-67",
    "fechaSolicitud": "2025-10-30 10:00:00",
    "fechaLimitePago": "2025-11-06 10:00:00",
    "modificacionesRestantes": 2,
    "productos": [
      {
        "idProducto": 9,
        "nombreProducto": "Producto Ejemplo",
        "cantidadSolicitada": 222,
        "precio": 100.0
      }
    ]
  }
}
```

---

### Obtener Solicitudes por Cliente

Obtiene todas las solicitudes de un cliente específico.

**Endpoint**: `GET /solicitudes/{idCliente}`

**Autenticación**: No requerida

**Parámetros URL**:
- `idCliente` (number): ID del cliente

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Solicitudes obtenidas exitosamente",
  "data": [
    {
      "idSolicitud": 1,
      "idCliente": 21,
      "estadoSolicitud": "PDP",
      "direccionEntrega": "Calle 123",
      "fechaSolicitud": "2025-10-30 10:00:00",
      "productos": [...]
    }
  ]
}
```

---

### Obtener Solicitudes por Pedido

Obtiene todas las solicitudes de un pedido específico.

**Endpoint**: `GET /solicitudes/pedido/{idPedido}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `idPedido` (number): ID del pedido

**Query Parameters (opcional)**:
- `estadoSolicitud` (string): Filtrar por estado

**Ejemplo**: `GET /solicitudes/pedido/15?estadoSolicitud=PGD`

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Solicitudes obtenidas exitosamente",
  "data": [
    {
      "idSolicitud": 1,
      "estadoSolicitud": "PGD",
      "nombreCliente": "Juan Pérez",
      "productos": [...]
    }
  ]
}
```

---

### Obtener Mis Solicitudes con OTP

Obtiene las solicitudes del cliente autenticado con OTP.

**Endpoint**: `GET /solicitudes/mis-solicitudes/{hashPedido}`

**Autenticación**: Bearer Token OTP

**Parámetros URL**:
- `hashPedido` (string): Hash del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Solicitudes obtenidas exitosamente",
  "data": [
    {
      "idSolicitud": 1,
      "estadoSolicitud": "PDP",
      "direccionEntrega": "Calle 123",
      "productos": [...]
    }
  ]
}
```

---

### Modificar Solicitud

Modifica la dirección y/o cantidades de productos en una solicitud.

**Endpoint**: `PUT /solicitudes/{id}/modificar`

**Autenticación**: Bearer Token OTP o Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID de la solicitud

**Request Body (Dirección)**:
```json
{
  "direccionEntrega": "Nueva Dirección #123"
}
```

**Request Body (Productos)**:
```json
{
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 123
    }
  ]
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Solicitud modificada exitosamente",
  "data": {
    "idSolicitud": 16,
    "modificacionesRestantes": 1,
    ...
  }
}
```

---

### Cambiar Estado de Solicitud

Actualiza el estado de una solicitud (solo administradores).

**Endpoint**: `PUT /solicitudes/{id}/estado`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID de la solicitud

**Request Body**:
```json
{
  "nuevoEstado": "PGD"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Estado actualizado exitosamente",
  "data": {
    "idSolicitud": 15,
    "estadoSolicitud": "PGD",
    ...
  }
}
```

---

### Cancelar Solicitud

Cancela una solicitud específica.

**Endpoint**: `DELETE /solicitudes/{id}`

**Autenticación**: Bearer Token OTP o No requerida

**Parámetros URL**:
- `id` (number): ID de la solicitud

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Solicitud cancelada exitosamente"
}
```

---

### Agregar Producto a Solicitud

Agrega un nuevo producto a una solicitud existente.

**Endpoint**: `POST /solicitudes/{id}/productos`

**Autenticación**: Bearer Token OTP

**Parámetros URL**:
- `id` (number): ID de la solicitud

**Request Body**:
```json
{
  "cantidadSolicitada": 170
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto agregado a la solicitud"
}
```

---

### Cambiar Cantidad de Producto en Solicitud

Modifica la cantidad solicitada de un producto específico.

**Endpoint**: `PUT /solicitudes/{idSolicitud}/productos/{idProducto}`

**Autenticación**: Bearer Token OTP

**Parámetros URL**:
- `idSolicitud` (number): ID de la solicitud
- `idProducto` (number): ID del producto

**Request Body**:
```json
{
  "cantidad": 300
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Cantidad actualizada exitosamente"
}
```

---

### Eliminar Producto de Solicitud

Elimina un producto de una solicitud.

**Endpoint**: `DELETE /solicitudes/{idSolicitud}/productos/{idProducto}`

**Autenticación**: Bearer Token OTP

**Parámetros URL**:
- `idSolicitud` (number): ID de la solicitud
- `idProducto` (number): ID del producto

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Producto eliminado de la solicitud"
}
```

---

## 🔄 Consolidación

### Cancelar Solicitudes Vencidas de un Pedido

Cancela todas las solicitudes vencidas de un pedido específico.

**Endpoint**: `POST /cancelar-solicitudes/cancelar-vencidas/{idPedido}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `idPedido` (number): ID del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Solicitudes vencidas canceladas exitosamente",
  "data": {
    "solicitudesCanceladas": 5
  }
}
```

---

### Cancelar Todas las Solicitudes Vencidas

Cancela todas las solicitudes vencidas del sistema.

**Endpoint**: `POST /cancelar-solicitudes/cancelar-todas-vencidas`

**Autenticación**: Bearer Token (SAD/ADM)

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Todas las solicitudes vencidas han sido canceladas",
  "data": {
    "solicitudesCanceladas": 12
  }
}
```

---

### Consolidar Pedido

Consolida un pedido cambiando a estado "En Ruta" las solicitudes pagadas.

**Endpoint**: `POST /consolidacion/pedido/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Pedido consolidado exitosamente",
  "data": {
    "idPedido": 28,
    "solicitudesConsolidadas": 8
  }
}
```

---

### Obtener Histórico de Pedidos

Obtiene el histórico de solicitudes de un pedido con filtros opcionales.

**Endpoint**: `GET /historico/pedidos/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Query Parameters (opcional)**:
- `estadoSolicitud` (string): Filtrar por estado

**Ejemplo**: `GET /historico/pedidos/28?estadoSolicitud=PGD`

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Histórico obtenido exitosamente",
  "data": [
    {
      "idSolicitud": 1,
      "nombreCliente": "Juan Pérez",
      "estadoSolicitud": "PGD",
      "fechaSolicitud": "2025-10-30 10:00:00",
      "productos": [...]
    }
  ]
}
```

---

### Obtener Logs del Sistema

Obtiene todos los logs del sistema (solo SAD).

**Endpoint**: `GET /logs`

**Autenticación**: Bearer Token (SAD)

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Logs obtenidos exitosamente",
  "data": [
    {
      "idLog": 1,
      "accion": "LOGIN",
      "idUsuario": 1,
      "nombreUsuario": "Super Usuario",
      "detalles": "Login exitoso",
      "timestamp": "2025-10-30 10:00:00"
    }
  ]
}
```

---

### Obtener Logs por Usuario

Obtiene los logs de un usuario específico (solo SAD).

**Endpoint**: `GET /logs/usuario/{id}`

**Autenticación**: Bearer Token (SAD)

**Parámetros URL**:
- `id` (number): ID del usuario

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Logs del usuario obtenidos exitosamente",
  "data": [
    {
      "idLog": 1,
      "accion": "LOGIN",
      "detalles": "Login exitoso",
      "timestamp": "2025-10-30 10:00:00"
    }
  ]
}
```

---

## Reportes y Estadísticas

### Resumen General

Obtiene un resumen general del sistema.

**Endpoint**: `GET /reportes/resumen`

**Autenticación**: Bearer Token (SAD/ADM)

**Response Success** (200):
```json
{
  "totalPedidosActivos": 5,
  "totalSolicitudesPendientes": 12,
  "totalSolicitudesPagadas": 8,
  "ingresosEsteMes": 5000.0,
  "productosEnStock": 25
}
```

---

### Productos Más Vendidos

Obtiene los productos más vendidos con límite opcional.

**Endpoint**: `GET /reportes/productos-mas-vendidos`

**Autenticación**: Bearer Token (SAD/ADM)

**Query Parameters (opcional)**:
- `limite` (number): Número máximo de productos (default: 10)

**Ejemplo**: `GET /reportes/productos-mas-vendidos?limite=5`

**Response Success** (200):
```json
[
  {
    "idProducto": 9,
    "nombreProducto": "Producto A",
    "cantidadVendida": 1500,
    "ingresoTotal": 150000.0
  },
  {
    "idProducto": 12,
    "nombreProducto": "Producto B",
    "cantidadVendida": 1200,
    "ingresoTotal": 120000.0
  }
]
```

**Response Error** (400):
```json
{
  "error": "El límite debe estar entre 1 y 100"
}
```

---

### Ingresos por Período

Obtiene los ingresos agrupados por período.

**Endpoint**: `GET /reportes/ingresos`

**Autenticación**: Bearer Token (SAD/ADM)

**Query Parameters**:
- `fechaInicio` (string, required): Fecha de inicio (YYYY-MM-DD)
- `fechaFin` (string, required): Fecha de fin (YYYY-MM-DD)
- `agrupacion` (string, optional): Tipo de agrupación (default: TRIMESTRAL)
  - Valores permitidos: `TRIMESTRAL`, `ANUAL`

**Ejemplo**: `GET /reportes/ingresos?fechaInicio=2025-01-01&fechaFin=2025-12-31&agrupacion=TRIMESTRAL`

**Response Success** (200):
```json
{
  "periodos": [
    {
      "periodo": "Q1 2025",
      "ingresos": 45000.0,
      "costos": 30000.0,
      "ganancia": 15000.0
    },
    {
      "periodo": "Q2 2025",
      "ingresos": 52000.0,
      "costos": 35000.0,
      "ganancia": 17000.0
    }
  ],
  "resumen": {
    "totalIngresos": 97000.0,
    "totalCostos": 65000.0,
    "gananciaTotal": 32000.0
  }
}
```

**Response Error** (400):
```json
{
  "error": "La fecha de inicio no puede ser posterior a la fecha final"
}
```

o

```json
{
  "error": "La agrupación debe ser 'TRIMESTRAL' o 'ANUAL'"
}
```

---

### Productos con Mayor Ganancia

Obtiene los productos ordenados por ganancia.

**Endpoint**: `GET /reportes/productos-mayor-ganancia`

**Autenticación**: Bearer Token (SAD/ADM)

**Query Parameters (opcional)**:
- `limite` (number): Número máximo de productos (default: 10)

**Response Success** (200):
```json
[
  {
    "idProducto": 9,
    "nombreProducto": "Producto A",
    "gananciaTotal": 50000.0,
    "margenGanancia": 33.33
  }
]
```

**Response Error** (400):
```json
{
  "error": "El límite debe estar entre 1 y 100"
}
```

---

### Clientes Frecuentes

Obtiene los clientes más frecuentes.

**Endpoint**: `GET /reportes/clientes-frecuentes`

**Autenticación**: Bearer Token (SAD/ADM)

**Query Parameters (opcional)**:
- `limite` (number): Número máximo de clientes (default: 10)

**Response Success** (200):
```json
[
  {
    "idCliente": 25,
    "nombreCliente": "Juan Pérez",
    "totalSolicitudes": 15,
    "totalGastado": 75000.0
  }
]
```

**Response Error** (400):
```json
{
  "error": "El límite debe estar entre 1 y 100"
}
```

---

### Pedidos en Curso

Obtiene todos los pedidos actualmente en curso.

**Endpoint**: `GET /reportes/pedidos-en-curso`

**Autenticación**: Bearer Token (SAD/ADM)

**Response Success** (200):
```json
[
  {
    "idPedido": 28,
    "estadoPedido": "ACT",
    "fechaCierre": "2025-12-30",
    "totalSolicitudes": 12
  }
]
```

---

## Informes Excel

### Informe por Cliente

Genera y descarga un informe Excel de las compras de un cliente.

**Endpoint**: `GET /excel/cliente/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del cliente

**Response Success** (200):
- **Content-Type**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Content-Disposition**: `attachment; filename="Informe_Cliente_{id}.xlsx"`
- Archivo Excel descargable

**Response Error** (400):
```json
{
  "status": "fail",
  "message": "Error en operación de Excel",
  "error": {
    "code": "EXCEL_BUSINESS_ERROR",
    "details": "El cliente no tiene solicitudes completadas"
  }
}
```

---

### Informe por Pedido

Genera y descarga un informe Excel de las solicitudes de un pedido.

**Endpoint**: `GET /excel/pedido/{id}`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Response Success** (200):
- **Content-Type**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Content-Disposition**: `attachment; filename="Informe_Pedido_{id}.xlsx"`
- Archivo Excel descargable

**Response Error** (400):
```json
{
  "status": "fail",
  "message": "Error en operación de Excel",
  "error": {
    "code": "EXCEL_BUSINESS_ERROR",
    "details": "El pedido no tiene solicitudes pagadas"
  }
}
```

---

##  Etiquetas

### Obtener Etiquetas en JSON

Obtiene las etiquetas de envío de un pedido en formato JSON.

**Endpoint**: `GET /etiquetas/pedido/{id}/json`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Etiquetas obtenidas exitosamente",
  "data": [
    {
      "numeroEtiqueta": "ETQ-001",
      "nombreCliente": "Juan Pérez",
      "direccion": "Calle 123 #45-67",
      "telefono": "3001234567",
      "productos": [
        {
          "nombreProducto": "Producto A",
          "cantidad": 10
        }
      ]
    }
  ]
}
```

---

### Descargar Etiquetas en PDF

Genera y descarga las etiquetas de envío en formato PDF.

**Endpoint**: `GET /etiquetas/pedido/{id}/pdf`

**Autenticación**: Bearer Token (SAD/ADM)

**Parámetros URL**:
- `id` (number): ID del pedido

**Response Success** (200):
- **Content-Type**: `application/pdf`
- **Content-Disposition**: `attachment; filename="Etiquetas_Pedido_{id}.pdf"`
- Archivo PDF descargable

---

## Autenticación OTP (Twilio)

### Solicitar Código OTP

Solicita el envío de un código OTP al teléfono del cliente.

**Endpoint**: `POST /auth/otp/solicitar`

**Autenticación**: No requerida

**Request Body**:
```json
{
  "cedula": "9152556873"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Código OTP enviado exitosamente al teléfono registrado"
}
```

**Response Error** (404):
```json
{
  "status": "fail",
  "message": "Cliente no encontrado"
}
```

---

### Verificar Código OTP

Verifica el código OTP y devuelve un token de autenticación temporal.

**Endpoint**: `POST /auth/otp/verificar`

**Autenticación**: No requerida

**Request Body**:
```json
{
  "cedula": "9152556873",
  "codigoOtp": "788402"
}
```

**Response Success** (200):
```json
{
  "status": "success",
  "message": "Código OTP verificado correctamente. Token de autorización generado",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "idCliente": 25,
    "nombreCompleto": "Juan Pérez",
    "cedula": "9152556873",
    "telefono": "3001234567"
  },
  "timestamp": "2025-10-30 10:00:00"
}
```

**Response Error** (400):
```json
{
  "status": "fail",
  "message": "Código OTP inválido o expirado",
  "timestamp": "2025-10-30 10:00:00"
}
```

---

## Códigos de Respuesta

### Códigos HTTP

| Código | Significado | Descripción |
|--------|-------------|-------------|
| 200 | OK | Solicitud exitosa |
| 201 | Created | Recurso creado exitosamente |
| 400 | Bad Request | Error en la solicitud (validación) |
| 401 | Unauthorized | No autenticado o token inválido |
| 403 | Forbidden | No autorizado para esta operación |
| 404 | Not Found | Recurso no encontrado |
| 409 | Conflict | Conflicto (ej: recurso ya existe) |
| 500 | Internal Server Error | Error interno del servidor |

---

### Estructura de Respuesta Exitosa

```json
{
  "status": "success",
  "message": "Operación exitosa",
  "data": { ... },
  "timestamp": "2025-10-30 10:00:00"
}
```

---

### Estructura de Respuesta de Error

```json
{
  "status": "fail",
  "message": "Descripción del error",
  "error": {
    "code": "ERROR_CODE",
    "details": "Detalles adicionales del error"
  },
  "timestamp": "2025-10-30 10:00:00",
  "path": "/api/endpoint"
}
```

---

### Códigos de Error Personalizados

| Código | Descripción |
|--------|-------------|
| `VALIDATION_ERROR` | Error de validación de datos |
| `AUTHENTICATION_ERROR` | Error de autenticación |
| `AUTHORIZATION_ERROR` | Error de autorización |
| `RESOURCE_NOT_FOUND` | Recurso no encontrado |
| `BUSINESS_ERROR` | Error de lógica de negocio |
| `EXCEL_BUSINESS_ERROR` | Error al generar informe Excel |
| `DATABASE_ERROR` | Error de base de datos |
| `EXTERNAL_SERVICE_ERROR` | Error en servicio externo (Firebase, Twilio) |

---

## Autenticación y Seguridad

### Header de Autenticación

Para endpoints que requieren autenticación, incluir el token JWT en el header:

```
Authorization: Bearer {token}
```

### Roles del Sistema

| Rol | Código | Descripción |
|-----|--------|-------------|
| Superadministrador | `SAD` | Acceso completo al sistema |
| Administrador | `ADM` | Gestión de pedidos y productos |
| Cliente (OTP) | - | Acceso temporal mediante OTP |