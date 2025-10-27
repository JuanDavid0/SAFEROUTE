# API Documentation - Solicitud Controller (Parte 1/2)

## Información General

**Base URL:** `/solicitudes`

**Descripción:** Controlador para gestión de solicitudes de clientes. Este es el **componente más importante** del sistema SAFEROUTE, permite a los clientes crear y gestionar sus solicitudes de productos asociadas a pedidos colectivos.

**Versión:** 1.0

**Características Principales:**
- Creación de solicitudes para clientes registrados
- Creación de solicitudes para clientes nuevos (sin registro previo)
- Autenticación dual: JWT estándar + Token OTP temporal
- Gestión completa de productos en solicitudes
- Sistema de modificaciones limitadas (3 cambios máximos)
- Estados de solicitud con flujo controlado

---

## Autenticación Especial - Token OTP

**IMPORTANTE:** Varios endpoints de este controller requieren **Token OTP** (no JWT estándar).

### ¿Qué es el Token OTP?
Es un token JWT temporal generado después de verificar un código OTP enviado por SMS. Permite a clientes **sin credenciales** acceder a sus solicitudes usando solo su cédula.

### ¿Cómo obtenerlo?
1. Cliente solicita código OTP: `POST /auth/otp/solicitar` con su cédula
2. Recibe SMS con código de 6 dígitos
3. Verifica código: `POST /auth/otp/verificar` con cédula + código
4. Obtiene Token OTP válido por tiempo limitado

### Diferencia: Token OTP vs JWT Estándar

| Característica | Token OTP | JWT Estándar |
|----------------|-----------|--------------|
| **Obtención** | Verificación SMS | Login con contraseña |
| **Duración** | Temporal (1-2 horas) | Extendida (24 horas) |
| **Claim especial** | `isOtpToken: true` | Roles (CLI/ADM/SAD) |
| **Uso principal** | Consultar/modificar solicitudes | Operaciones generales |
| **Identificador** | Solo cédula | Usuario completo + roles |

### Endpoints que requieren Token OTP:
- `GET /solicitudes/mis-solicitudes/{hashPedido}`
- `GET /solicitudes/mis-solicitudes`
- `PUT /solicitudes/{idSolicitud}/modificar`
- `POST /solicitudes/{idSolicitud}/productos`
- `DELETE /solicitudes/{idSolicitud}/productos/{idProducto}`
- `PUT /solicitudes/{idSolicitud}/productos/{idProducto}`

**⚠️ Validación:** Estos endpoints verifican que el token OTP pertenezca al dueño de la solicitud (por cédula).

---

## Endpoints (Parte 1 - Primeros 6 endpoints)

### 1. Crear Solicitud (Cliente Registrado)

Permite a un cliente registrado crear una nueva solicitud asociada a un pedido.

**Endpoint:**
```
POST /solicitudes/{idCliente}
```

**Autenticación:** Requerida (JWT estándar)

**Roles Permitidos:** `CLI`

**Path Parameters:**
- `idCliente` (Integer, requerido) - ID del cliente que crea la solicitud

**Request Headers:**
```http
Authorization: Bearer <token_jwt_estandar>
Content-Type: application/json
```

**Request Body:**
```json
{
  "idPedido": 5,
  "direccionEntrega": "Av. 6 de Diciembre N34-120, Edificio Torres del Norte, Depto 502",
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 5
    },
    {
      "idProducto": 12,
      "cantidadSolicitada": 3
    }
  ]
}
```

**Validaciones Request:**
- `idPedido`: Requerido, debe existir y estar en estado "AGP" (Aceptando Solicitudes)
- `direccionEntrega`: Opcional (si no se envía, usa dirección del cliente)
- `productos`: Array requerido, mínimo 1 producto
- `productos[].idProducto`: Debe existir y estar asociado al pedido
- `productos[].cantidadSolicitada`: Mayor a 0, dentro del rango permitido (cantidadMin-cantidadMax)

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Solicitud creada exitosamente",
  "datos": {
    "idSolicitud": 47,
    "idCliente": 8,
    "nombreCliente": "Juan Carlos Pérez González",
    "idPedido": 5,
    "estadoSolicitud": "PND",
    "direccionEntrega": "Av. 6 de Diciembre N34-120, Edificio Torres del Norte, Depto 502",
    "fechaSolicitud": "2025-10-26",
    "fechaLimitePago": "2025-10-29",
    "modificacionesRestantes": 3,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadSolicitada": 5,
        "precio": 1.85
      },
      {
        "idProducto": 12,
        "nombreProducto": "Aceite Girasol 1L",
        "cantidadSolicitada": 3,
        "precio": 3.25
      }
    ]
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Error de validación",
  "data": {
    "exito": false,
    "mensaje": "La solicitud contiene errores de validación",
    "errores": [
      "El pedido no está aceptando solicitudes (estado: CSL)",
      "direccionEntrega: no puede estar vacía",
      "productos[0].cantidadSolicitada: fuera del rango permitido",
      "idPedido: debe ser un pedido activo"
    ],
    "datos": {
      "estadoPedido": "CSL",
      "estadosPermitidos": ["AGP"],
      "rangosProductos": {
        "10": {"min": 1, "max": 50},
        "12": {"min": 1, "max": 30}
      }
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 401 Unauthorized:**
```json
{
  "status": "error",
  "message": "No autenticado",
  "data": {
    "exito": false,
    "mensaje": "Token no válido o expirado",
    "datos": {
      "causa": "Token expirado",
      "tiempoExpiracion": "2025-10-26T09:15:30",
      "sugerencia": "Renueve su sesión"
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 403 Forbidden:**
```json
{
  "status": "error",
  "message": "Acceso denegado",
  "data": {
    "exito": false,
    "mensaje": "No tiene permisos para esta operación",
    "datos": {
      "rolRequerido": "CLI",
      "rolUsuario": "ADM",
      "operacion": "crear_solicitud"
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Recurso no encontrado",
  "data": {
    "exito": false,
    "mensaje": "No se encontró el cliente o pedido",
    "datos": {
      "idCliente": 8,
      "idPedido": 5,
      "entidadNoEncontrada": "pedido",
      "sugerencia": "Verifique los IDs proporcionados"
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 409 Conflict:**
```json
{
  "status": "error",
  "message": "Conflicto de datos",
  "data": {
    "exito": false,
    "mensaje": "Ya existe una solicitud activa para este cliente en el pedido",
    "datos": {
      "idCliente": 8,
      "idPedido": 5,
      "idSolicitudExistente": 46,
      "estadoSolicitud": "PND",
      "sugerencia": "Modifique la solicitud existente o espere a que sea procesada"
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Error de negocio",
  "data": {
    "exito": false,
    "mensaje": "No se puede procesar la solicitud",
    "datos": {
      "validaciones": [
        "Cliente tiene solicitudes pendientes de pago",
        "Pedido excede límite de solicitudes",
        "Productos no disponibles en stock",
        "Fecha límite de pedido alcanzada"
      ]
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 429 Too Many Requests:**
```json
{
  "status": "error",
  "message": "Demasiadas solicitudes",
  "data": {
    "exito": false,
    "mensaje": "Ha excedido el límite de solicitudes",
    "datos": {
      "limitePorHora": 5,
      "solicitudesRealizadas": 6,
      "tiempoEspera": 1800,
      "reintentar": "2025-10-26T11:15:30"
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

**Response 500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Error interno",
  "data": {
    "exito": false,
    "mensaje": "Error al procesar la solicitud",
    "datos": {
      "tipo": "DatabaseException",
      "error": "Error de transacción",
      "referencia": "SOL-2025102610153001",
      "sugerencia": "Contacte al administrador"
    }
  },
  "timestamp": "2025-10-26T10:15:30"
}
```

### Advertencias y consideraciones

- **Límites y validaciones:**
  - 5 solicitudes máximo por hora por cliente
  - Dirección: mínimo 10, máximo 200 caracteres
  - Productos: mínimo 1, máximo 20 por solicitud
  - Cantidades: según límites del pedido

- **Estados y transiciones:**
  - PND: Pendiente de pago (inicial)
  - PAG: Pagado y verificado
  - ENT: Entregado
  - CNL: Cancelado
  - No permitir cambios en ENT/CNL

- **Modificaciones:**
  - 3 modificaciones máximo
  - Solo en estado PND
  - No cambios después del pago
  - Registro de cada cambio

- **Pagos:**
  - 3 días para pagar
  - Notificaciones 24h antes
  - Cancelación automática
  - No reembolsos

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/solicitudes/8" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "idPedido": 5,
    "direccionEntrega": "Av. 6 de Diciembre N34-120, Edificio Torres del Norte, Depto 502",
    "productos": [
      {"idProducto": 10, "cantidadSolicitada": 5},
      {"idProducto": 12, "cantidadSolicitada": 3}
    ]
  }'
```

**Notas Importantes:**
- Estado inicial de solicitud: `PND` (Pendiente de pago)
- Fecha límite de pago: Se calcula automáticamente (3 días por defecto)
- Modificaciones restantes: Inicia en 3
- Si no se envía `direccionEntrega`, se usa la dirección registrada del cliente
- Total de solicitud se calcula automáticamente (cantidad × precio unitario)

---

### 2. Crear Solicitud (Cliente Nuevo - Público)

Permite a un **cliente sin registro** crear una solicitud. El sistema crea automáticamente el usuario y la solicitud en una sola operación.

**Endpoint:**
```
POST /solicitudes/public/nueva
```

**Autenticación:** NO requerida (Endpoint público)

**Request Headers:**
```http
Content-Type: application/json
```

**Request Body:**
```json
{
  "nombres": "María Fernanda",
  "apellidos": "López Martínez",
  "telefono": "0987654321",
  "cedula": "1750234567",
  "direccion": "Calle Mariscal Foch E7-45 y Diego de Almagro",
  "idPedido": 5,
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 2
    }
  ]
}
```

**Validaciones Request:**
- `nombres`: Requerido, solo letras y espacios, máx 100 caracteres
- `apellidos`: Requerido, solo letras y espacios, máx 100 caracteres
- `telefono`: Requerido, exactamente 10 dígitos numéricos, único en el sistema
- `cedula`: Requerido, exactamente 10 dígitos numéricos, único en el sistema
- `direccion`: Requerido, máx 150 caracteres
- `idPedido`: Requerido, debe existir y estar en estado "AGP"
- `productos`: Array requerido, mínimo 1 producto

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Solicitud y cliente creados exitosamente",
  "datos": {
    "idSolicitud": 48,
    "idCliente": 25,
    "nombreCliente": "María Fernanda López Martínez",
    "idPedido": 5,
    "estadoSolicitud": "PND",
    "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
    "fechaSolicitud": "2025-10-26",
    "fechaLimitePago": "2025-10-29",
    "modificacionesRestantes": 3,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadSolicitada": 2,
        "precio": 1.85
      }
    ]
  },
  "timestamp": "2025-10-26T10:20:45"
}
```

**Response (400 Bad Request):**
```json
{
  "exito": false,
  "mensaje": "Ya existe un usuario registrado con la cédula 1750234567",
  "datos": null,
  "timestamp": "2025-10-26T10:20:45"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitud y cliente creados exitosamente
- `400 Bad Request` - Cédula/teléfono duplicado, pedido cerrado, validación fallida
- `404 Not Found` - Pedido o producto no encontrado
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/solicitudes/public/nueva" \
  -H "Content-Type: application/json" \
  -d '{
    "nombres": "María Fernanda",
    "apellidos": "López Martínez",
    "telefono": "0987654321",
    "cedula": "1750234567",
    "direccion": "Calle Mariscal Foch E7-45 y Diego de Almagro",
    "idPedido": 5,
    "productos": [
      {"idProducto": 10, "cantidadSolicitada": 2}
    ]
  }'
```

**Notas Importantes:**
- **Endpoint PÚBLICO** - No requiere autenticación previa
- Crea usuario con rol `CLI` automáticamente
- Genera contraseña temporal basada en cédula (debe cambiarse después)
- Usuario queda activo inmediatamente
- Después de crear solicitud, cliente puede:
  1. Obtener Token OTP con su cédula para modificar solicitud
  2. Hacer login con contraseña temporal para acceso completo
- Validación de cédula/teléfono únicos previene duplicados

**Flujo Típico:**
1. Cliente recibe link del pedido por WhatsApp: `https://app.com/pedido/abc123def456`
2. Cliente ingresa datos personales + productos deseados
3. Sistema crea usuario + solicitud en una transacción
4. Cliente recibe confirmación con ID de solicitud
5. Cliente solicita OTP con su cédula para modificar si necesita

---

### 3. Listar Mis Solicitudes por Pedido

Permite a un cliente consultar sus solicitudes asociadas a un pedido específico usando **Token OTP**.

**Endpoint:**
```
GET /solicitudes/mis-solicitudes/{hashPedido}
```

**Autenticación:** Requerida (Token OTP)

**Path Parameters:**
- `hashPedido` (String, requerido) - Hash público del pedido (ej: "abc123def456xyz789")

**Request Headers:**
```http
Authorization: Bearer <token_otp>
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 2 solicitud(es) para este pedido",
  "datos": [
    {
      "idSolicitud": 48,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PND",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-26",
      "fechaLimitePago": "2025-10-29",
      "modificacionesRestantes": 3,
      "productos": [
        {
          "idProducto": 10,
          "nombreProducto": "Arroz Premium 1kg",
          "cantidadSolicitada": 2,
          "precio": 1.85
        }
      ]
    },
    {
      "idSolicitud": 52,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PGD",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-22",
      "fechaLimitePago": "2025-10-25",
      "modificacionesRestantes": 1,
      "productos": [
        {
          "idProducto": 12,
          "nombreProducto": "Aceite Girasol 1L",
          "cantidadSolicitada": 5,
          "precio": 3.25
        }
      ]
    }
  ],
  "timestamp": "2025-10-26T11:00:00"
}
```

**Response (200 OK - Sin solicitudes):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 0 solicitud(es) para este pedido",
  "datos": [],
  "timestamp": "2025-10-26T11:00:00"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitudes obtenidas exitosamente (puede ser lista vacía)
- `400 Bad Request` - Token no es OTP o hash inválido
- `401 Unauthorized` - Token no válido o expirado
- `404 Not Found` - Pedido con ese hash no encontrado
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/solicitudes/mis-solicitudes/abc123def456xyz789" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Notas Importantes:**
- **Requiere Token OTP** (no JWT estándar)
- Filtra automáticamente por cédula del token OTP
- Devuelve solo solicitudes del cliente autenticado para ese pedido específico
- Útil cuando cliente accede desde link público del pedido
- El hash identifica el pedido de forma segura sin exponer IDs internos

**Caso de Uso:**
Cliente recibe link: `https://app.com/pedido/abc123def456xyz789`
1. Cliente solicita OTP con su cédula
2. Verifica código OTP → obtiene token temporal
3. Usa token para consultar sus solicitudes de ese pedido
4. Puede ver estado, productos, modificaciones restantes

---

### 4. Listar Todas Mis Solicitudes

Permite a un cliente consultar **todas** sus solicitudes (de todos los pedidos) usando **Token OTP**.

**Endpoint:**
```
GET /solicitudes/mis-solicitudes
```

**Autenticación:** Requerida (Token OTP)

**Request Headers:**
```http
Authorization: Bearer <token_otp>
```

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 5 solicitud(es)",
  "datos": [
    {
      "idSolicitud": 48,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PND",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-26",
      "fechaLimitePago": "2025-10-29",
      "modificacionesRestantes": 3,
      "productos": [...]
    },
    {
      "idSolicitud": 52,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 5,
      "estadoSolicitud": "PGD",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-22",
      "fechaLimitePago": "2025-10-25",
      "modificacionesRestantes": 1,
      "productos": [...]
    },
    {
      "idSolicitud": 35,
      "idCliente": 25,
      "nombreCliente": "María Fernanda López Martínez",
      "idPedido": 3,
      "estadoSolicitud": "ENT",
      "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "fechaSolicitud": "2025-10-10",
      "fechaLimitePago": "2025-10-13",
      "modificacionesRestantes": 0,
      "productos": [...]
    }
  ],
  "timestamp": "2025-10-26T11:05:00"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitudes obtenidas exitosamente
- `400 Bad Request` - Token no es OTP
- `401 Unauthorized` - Token no válido o expirado
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/solicitudes/mis-solicitudes" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Notas Importantes:**
- **Requiere Token OTP** (no JWT estándar)
- Extrae cédula del token y busca todas las solicitudes asociadas
- Devuelve solicitudes de múltiples pedidos
- Incluye solicitudes en cualquier estado (PND, PGD, ENT, CAN)
- Útil para que cliente vea su histórico completo

**Diferencia con Endpoint Anterior:**
- **Por pedido** (`/mis-solicitudes/{hashPedido}`): Solicitudes de UN pedido específico
- **Todas** (`/mis-solicitudes`): Solicitudes de TODOS los pedidos del cliente

---

### 5. Modificar Solicitud

Permite modificar dirección de entrega y/o productos de una solicitud existente. **Limitado a 3 modificaciones** por solicitud.

**Endpoint:**
```
PUT /solicitudes/{idSolicitud}/modificar
```

**Autenticación:** Requerida (Token OTP)

**Roles:** Dueño de la solicitud (validado por cédula del token)

**Path Parameters:**
- `idSolicitud` (Integer, requerido) - ID de la solicitud a modificar

**Request Headers:**
```http
Authorization: Bearer <token_otp>
Content-Type: application/json
```

**Request Body (Opción 1 - Solo cambiar dirección):**
```json
{
  "direccionEntrega": "Nueva dirección: Av. Amazonas N24-155, Conjunto Portal del Sol, Casa 8"
}
```

**Request Body (Opción 2 - Solo cambiar productos):**
```json
{
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 8
    },
    {
      "idProducto": 15,
      "cantidadSolicitada": 2
    }
  ]
}
```

**Request Body (Opción 3 - Cambiar dirección Y productos):**
```json
{
  "direccionEntrega": "Nueva dirección: Av. Amazonas N24-155, Conjunto Portal del Sol, Casa 8",
  "productos": [
    {
      "idProducto": 10,
      "cantidadSolicitada": 8
    }
  ]
}
```

**Validaciones Request:**
- `direccionEntrega`: Opcional, si se envía debe tener contenido
- `productos`: Opcional, si se envía debe tener mínimo 1 producto
- `productos[].idProducto`: Debe existir y estar en el pedido
- `productos[].cantidadSolicitada`: Mayor a 0, dentro del rango permitido
- **Solicitud debe tener modificacionesRestantes > 0**
- **Solicitud debe estar en estado "PND" (Pendiente)**

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Solicitud modificada exitosamente",
  "datos": {
    "idSolicitud": 48,
    "idCliente": 25,
    "nombreCliente": "María Fernanda López Martínez",
    "idPedido": 5,
    "estadoSolicitud": "PND",
    "direccionEntrega": "Nueva dirección: Av. Amazonas N24-155, Conjunto Portal del Sol, Casa 8",
    "fechaSolicitud": "2025-10-26",
    "fechaLimitePago": "2025-10-29",
    "modificacionesRestantes": 2,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadSolicitada": 8,
        "precio": 1.85
      },
      {
        "idProducto": 15,
        "nombreProducto": "Azúcar Blanca 2kg",
        "cantidadSolicitada": 2,
        "precio": 2.10
      }
    ]
  },
  "timestamp": "2025-10-26T11:30:00"
}
```

**Response (400 Bad Request - Sin modificaciones):**
```json
{
  "exito": false,
  "mensaje": "No quedan modificaciones disponibles para esta solicitud",
  "datos": null,
  "timestamp": "2025-10-26T11:30:00"
}
```

**Response (403 Forbidden - No es dueño):**
```json
{
  "exito": false,
  "mensaje": "No tienes permiso para modificar esta solicitud",
  "datos": null,
  "timestamp": "2025-10-26T11:30:00"
}
```

**Códigos de Estado:**
- `200 OK` - Solicitud modificada exitosamente
- `400 Bad Request` - Sin modificaciones restantes, estado no permite cambios
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Token OTP no pertenece al dueño de la solicitud
- `404 Not Found` - Solicitud no encontrada
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X PUT "http://localhost:8080/solicitudes/48/modificar" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "direccionEntrega": "Nueva dirección: Av. Amazonas N24-155, Conjunto Portal del Sol, Casa 8",
    "productos": [
      {"idProducto": 10, "cantidadSolicitada": 8},
      {"idProducto": 15, "cantidadSolicitada": 2}
    ]
  }'
```

**Notas Importantes:**
- **Requiere Token OTP**
- **Validación de propiedad**: Sistema verifica que cédula del token coincida con dueño de solicitud
- **Límite de 3 modificaciones** por solicitud
- Cada modificación decrementa `modificacionesRestantes`
- Solo solicitudes en estado `PND` pueden modificarse
- Si se envía `productos`, se **reemplaza completamente** la lista anterior (no es merge)
- La modificación cuenta como 1, sin importar si cambia dirección, productos, o ambos

**Sistema de Modificaciones:**
```
Solicitud nueva → modificacionesRestantes: 3
Modificación 1 → modificacionesRestantes: 2
Modificación 2 → modificacionesRestantes: 1
Modificación 3 → modificacionesRestantes: 0
Modificación 4 → ❌ Error: "No quedan modificaciones disponibles"
```

---

### 6. Agregar Producto a Solicitud

Agrega un producto adicional a una solicitud existente.

**Endpoint:**
```
POST /solicitudes/{idSolicitud}/productos
```

**Autenticación:** Requerida (Token OTP)

**Roles:** Dueño de la solicitud (validado por cédula del token)

**Path Parameters:**
- `idSolicitud` (Integer, requerido) - ID de la solicitud

**Request Headers:**
```http
Authorization: Bearer <token_otp>
Content-Type: application/json
```

**Request Body:**
```json
{
  "idProducto": 18,
  "cantidadSolicitada": 4
}
```

**Validaciones Request:**
- `idProducto`: Requerido, debe existir y estar en el pedido
- `cantidadSolicitada`: Requerido, mayor a 0, dentro del rango permitido
- Producto NO debe estar ya en la solicitud (usar modificar cantidad si existe)

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Producto agregado exitosamente a la solicitud",
  "datos": {
    "idSolicitud": 48,
    "idCliente": 25,
    "nombreCliente": "María Fernanda López Martínez",
    "idPedido": 5,
    "estadoSolicitud": "PND",
    "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
    "fechaSolicitud": "2025-10-26",
    "fechaLimitePago": "2025-10-29",
    "modificacionesRestantes": 3,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadSolicitada": 2,
        "precio": 1.85
      },
      {
        "idProducto": 18,
        "nombreProducto": "Fideo Tallarin 500g",
        "cantidadSolicitada": 4,
        "precio": 1.45
      }
    ]
  },
  "timestamp": "2025-10-26T12:00:00"
}
```

**Response (400 Bad Request - Producto duplicado):**
```json
{
  "exito": false,
  "mensaje": "El producto ya existe en la solicitud. Use el endpoint de modificar cantidad.",
  "datos": null,
  "timestamp": "2025-10-26T12:00:00"
}
```

**Códigos de Estado:**
- `200 OK` - Producto agregado exitosamente
- `400 Bad Request` - Producto duplicado, cantidad inválida, fuera de rango
- `401 Unauthorized` - Token no válido o expirado
- `403 Forbidden` - Token OTP no pertenece al dueño de la solicitud
- `404 Not Found` - Solicitud o producto no encontrado
- `500 Internal Server Error` - Error del servidor

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/solicitudes/48/productos" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "idProducto": 18,
    "cantidadSolicitada": 4
  }'
```

**Notas Importantes:**
- **Requiere Token OTP**
- **NO consume modificaciones restantes** (agregar producto es operación separada)
- Producto debe estar disponible en el pedido
- Cantidad debe estar dentro del rango (cantidadMin - cantidadMax) del producto en el pedido
- Si producto ya existe en solicitud → Error (usar endpoint modificar cantidad)
- Solo solicitudes en estado `PND` permiten agregar productos

---

## Próxima Parte

Esta es la **Parte 1/2** de la documentación de SolicitudController con los primeros 6 endpoints.

**Endpoints documentados hasta ahora:**
1. ✅ `POST /solicitudes/{idCliente}` - Crear solicitud (cliente registrado)
2. ✅ `POST /solicitudes/public/nueva` - Crear solicitud (cliente nuevo)
3. ✅ `GET /solicitudes/mis-solicitudes/{hashPedido}` - Listar por pedido (OTP)
4. ✅ `GET /solicitudes/mis-solicitudes` - Listar todas (OTP)
5. ✅ `PUT /solicitudes/{idSolicitud}/modificar` - Modificar solicitud (OTP)
6. ✅ `POST /solicitudes/{idSolicitud}/productos` - Agregar producto (OTP)

**Pendientes para Parte 2:**
7. `DELETE /solicitudes/{idSolicitud}/productos/{idProducto}` - Eliminar producto
8. `PUT /solicitudes/{idSolicitud}/productos/{idProducto}` - Modificar cantidad producto
9. `DELETE /solicitudes/{idSolicitud}` - Cancelar solicitud
10. `GET /solicitudes/{idCliente}` - Listar solicitudes de cliente
11. `PUT /solicitudes/{idSolicitud}/estado` - Cambiar estado (SAD/ADM)
12. `GET /solicitudes/pedido/{idPedido}` - Listar por pedido con filtros (SAD/ADM)

---

**Última actualización:** 26 de octubre de 2025 - Parte 1/2
