# ProductoController API Documentation

## 📋 Información General
- **Base Path:** `/api/productos`
- **Controlador:** `ProductoController.java`
- **Seguridad:** 🔒 Todos los endpoints requieren rol `SAD` o `ADM`
- **Firebase Storage:** Integrado para manejo de imágenes

---

### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Datos de entrada inválidos",
  "data": {
    "exito": false,
    "errores": [
      "nombreProducto: es requerido y no puede estar vacío",
      "precioUnitario: debe ser >= 0.01",
      "descripcion: máximo 500 caracteres",
      "codigoProducto: debe seguir el formato PRD-XXXXX",
      "cantidadStock: debe ser un número entero positivo"
    ],
    "datos": {
      "formatoCodigoProducto": "PRD-XXXXX",
      "ejemploValido": {
        "nombreProducto": "Leche Entera 1L",
        "precioUnitario": 1.25,
        "codigoProducto": "PRD-00123"
      }
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "status": "error",
  "message": "Token inválido o expirado",
  "data": {
    "exito": false,
    "mensaje": "Sesión expirada o token inválido",
    "datos": {
      "causa": "Token expirado",
      "tiempoExpiracion": "2025-10-26T18:00:00",
      "sugerencia": "Inicie sesión nuevamente"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "status": "error",
  "message": "No tiene permisos suficientes",
  "data": {
    "exito": false,
    "mensaje": "No tiene permisos para crear/modificar productos",
    "datos": {
      "rolesPermitidos": ["SAD", "ADM"],
      "rolUsuario": "CLI",
      "operacion": "crear_producto",
      "sugerencia": "Contacte a un administrador"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Producto no encontrado",
  "data": {
    "exito": false,
    "mensaje": "El producto solicitado no existe",
    "datos": {
      "idProducto": 123,
      "codigoProducto": "PRD-00123",
      "sugerencia": "Verifique el ID o código del producto",
      "posiblesCausas": [
        "Producto eliminado",
        "ID/código incorrecto",
        "Producto descontinuado"
      ]
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 409 Conflict:**
```json
{
  "status": "error",
  "message": "Conflicto con el producto",
  "data": {
    "exito": false,
    "mensaje": "No se puede modificar el producto",
    "datos": {
      "idProducto": 123,
      "causa": "Producto en uso en pedido activo",
      "pedidosActivos": [456, 789],
      "sugerencia": "Espere a que finalicen los pedidos activos"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 413 Payload Too Large:**
```json
{
  "status": "error",
  "message": "Archivo demasiado grande",
  "data": {
    "exito": false,
    "mensaje": "La imagen excede el tamaño máximo permitido",
    "datos": {
      "tamañoMaximo": "10MB",
      "tamañoArchivo": "15.2MB",
      "sugerencias": [
        "Comprima la imagen",
        "Reduzca las dimensiones",
        "Use formato WebP"
      ]
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 415 Unsupported Media Type:**
```json
{
  "status": "error",
  "message": "Formato de archivo no soportado",
  "data": {
    "exito": false,
    "mensaje": "El formato de imagen no está permitido",
    "datos": {
      "formatosPermitidos": ["PNG", "JPG", "JPEG", "WebP"],
      "formatoRecibido": "GIF",
      "sugerencia": "Convierta la imagen a un formato permitido"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Error de validación de negocio",
  "data": {
    "exito": false,
    "mensaje": "No se puede procesar el producto",
    "datos": {
      "validaciones": [
        "El precio no puede ser menor al costo",
        "El stock no puede ser negativo",
        "El código de producto ya existe",
        "La categoría no existe"
      ]
    }
  },
  "timestamp": "2025-10-26T18:30:00"
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
      "limiteSubidasPorHora": 50,
      "subidasRealizadas": 51,
      "tiempoEspera": 360,
      "reintentar": "2025-10-26T19:30:00"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Error interno del servidor",
  "data": {
    "exito": false,
    "mensaje": "Error al procesar la solicitud",
    "datos": {
      "tipo": "FirebaseStorageException",
      "error": "Error al subir imagen a Firebase",
      "referencia": "ERR-2025102618300001",
      "sugerencia": "Reintente en unos minutos"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 503 Service Unavailable:**
```json
{
  "status": "error",
  "message": "Servicio no disponible",
  "data": {
    "exito": false,
    "mensaje": "Firebase Storage no disponible",
    "datos": {
      "servicio": "Firebase Storage",
      "estado": "Mantenimiento",
      "tiempoEstimadoReinicio": "2025-10-26T19:00:00",
      "alternativa": "Guarde el producto sin imagen"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

### Advertencias y consideraciones

- **Límites de imágenes:**
  - Tamaño máximo: 10MB
  - Formatos: PNG, JPG, JPEG, WebP
  - Dimensiones máximas: 2000x2000px
  - Ratio aspecto: 1:1 recomendado

- **Validaciones de producto:**
  - Nombres únicos por categoría
  - Precios con 2 decimales máximo
  - Stock mínimo configurable
  - Códigos de producto únicos

- **Limitaciones Firebase:**
  - 50 subidas por hora por usuario
  - Timeouts después de 30 segundos
  - Reintentos automáticos: 3 veces
  - Caché de imágenes: 1 hora

- **Seguridad:**
  - Escaneo de malware en imágenes
  - Validación de tipos MIME
  - Sanitización de nombres de archivo
  - Permisos por rol y categoría

- **Performance:**
  - Compresión automática de imágenes
  - Generación de thumbnails
  - Caché de productos frecuentes
  - Optimización de consultas
```

## 🔐 Endpoints

### 1. Crear Producto CON Imagen

```http
POST /api/productos
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**Descripción:** Crea un producto con imagen opcional subida a Firebase Storage.

**Seguridad:** 🔒 Requiere rol `SAD` o `ADM`

**Form Data:**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `producto` | Text (JSON) | Datos del producto en formato JSON |
| `imagen` | File | Archivo de imagen (opcional, máx 10MB) |

**JSON del campo `producto`:**
```json
{
  "nombreProducto": "Leche Entera Alpina 1L",
  "tipoProducto": "PERECEDERO",
  "descripcionProducto": "Leche entera pasteurizada",
  "precioUnitario": 3500.00,
  "costoUnitario": 2000.00
}
```

**Validaciones:**
- `nombreProducto`: Obligatorio, no vacío
- `tipoProducto`: Obligatorio, no vacío (ej: "PERECEDERO", "NO_PERECEDERO")
- `descripcionProducto`: Obligatorio, no vacío
- `precioUnitario`: Mínimo 0.01
- `costoUnitario`: Mínimo 0.00
- `imagen`: Opcional, máx 10MB, formatos: PNG, JPG, JPEG, WebP

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto creado exitosamente",
  "data": {
    "idProducto": 45,
    "nombreProducto": "Leche Entera Alpina 1L",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche entera pasteurizada",
    "precioUnitario": 3500.00,
    "costoUnitario": 2000.00,
    "urlImagen": "https://firebasestorage.googleapis.com/v0/b/saferoute-f90f8.appspot.com/o/productos%2F1730000000_leche-entera-alpina-1l.png?alt=media&token=abc123..."
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Configuración en Postman:**
```
POST http://localhost:8080/api/productos
Headers:
  Authorization: Bearer YOUR_TOKEN

Body (form-data):
  producto (Text): {"nombreProducto":"Leche Entera Alpina 1L","tipoProducto":"PERECEDERO","descripcionProducto":"Leche entera pasteurizada","precioUnitario":3500.00,"costoUnitario":2000.00}
  imagen (File): [Seleccionar archivo PNG/JPG]
```

**Errores:**
- `400`: Validación fallida
- `403`: Sin permisos (no es SAD/ADM)
- `413`: Imagen excede 10MB
- `415`: Formato de imagen no soportado

---

### 2. Crear Producto SIN Imagen

```http
POST /api/productos/sin-imagen
Content-Type: application/json
Authorization: Bearer {token}
```

**Descripción:** Crea un producto sin imagen (para compatibilidad).

**Request Body:**
```json
{
  "nombreProducto": "Arroz Diana 500g",
  "tipoProducto": "NO_PERECEDERO",
  "descripcionProducto": "Arroz blanco de primera calidad",
  "precioUnitario": 2500.00,
  "costoUnitario": 1500.00
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto creado exitosamente (sin imagen)",
  "data": {
    "idProducto": 46,
    "nombreProducto": "Arroz Diana 500g",
    "tipoProducto": "NO_PERECEDERO",
    "descripcionProducto": "Arroz blanco de primera calidad",
    "precioUnitario": 2500.00,
    "costoUnitario": 1500.00,
    "urlImagen": null
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

---

### 3. Listar Todos los Productos

```http
GET /api/productos
Authorization: Bearer {token}
```

**Descripción:** Obtiene la lista completa de productos activos.

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Se encontraron 25 producto(s)",
  "data": [
    {
      "idProducto": 45,
      "nombreProducto": "Leche Entera Alpina 1L",
      "tipoProducto": "PERECEDERO",
      "descripcionProducto": "Leche entera pasteurizada",
      "precioUnitario": 3500.00,
      "costoUnitario": 2000.00,
      "urlImagen": "https://firebasestorage.googleapis.com/..."
    },
    {
      "idProducto": 46,
      "nombreProducto": "Arroz Diana 500g",
      "tipoProducto": "NO_PERECEDERO",
      "descripcionProducto": "Arroz blanco de primera calidad",
      "precioUnitario": 2500.00,
      "costoUnitario": 1500.00,
      "urlImagen": null
    }
  ],
  "timestamp": "2025-10-26T18:30:00"
}
```

---

### 4. Obtener Producto por ID

```http
GET /api/productos/{id}
Authorization: Bearer {token}
```

**Path Parameters:**
- `id` (integer): ID del producto

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto encontrado",
  "data": {
    "idProducto": 45,
    "nombreProducto": "Leche Entera Alpina 1L",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche entera pasteurizada",
    "precioUnitario": 3500.00,
    "costoUnitario": 2000.00,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Errores:**
- `404`: Producto no encontrado

---

### 5. Buscar Producto por Nombre

```http
GET /api/productos/buscar?nombre={nombre}
Authorization: Bearer {token}
```

**Query Parameters:**
- `nombre` (string): Nombre exacto del producto

**Ejemplo:**
```
GET /api/productos/buscar?nombre=Leche%20Entera%20Alpina%201L
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto encontrado por nombre",
  "data": {
    "idProducto": 45,
    "nombreProducto": "Leche Entera Alpina 1L",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche entera pasteurizada",
    "precioUnitario": 3500.00,
    "costoUnitario": 2000.00,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Errores:**
- `404`: Producto no encontrado con ese nombre

---

### 6. Actualizar Producto CON Imagen

```http
PUT /api/productos/{id}
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**Path Parameters:**
- `id` (integer): ID del producto a actualizar

**Form Data:**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `producto` | Text (JSON) | Datos actualizados del producto |
| `imagen` | File | Nueva imagen (opcional) |

**JSON del campo `producto`:**
```json
{
  "nombreProducto": "Leche Entera Alpina Premium 1L",
  "tipoProducto": "PERECEDERO",
  "descripcionProducto": "Leche entera pasteurizada premium",
  "precioUnitario": 4000.00,
  "costoUnitario": 2500.00
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto actualizado exitosamente",
  "data": {
    "idProducto": 45,
    "nombreProducto": "Leche Entera Alpina Premium 1L",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche entera pasteurizada premium",
    "precioUnitario": 4000.00,
    "costoUnitario": 2500.00,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

**Comportamiento:**
- Si envías `imagen`: Se elimina la imagen anterior de Firebase y se sube la nueva
- Si NO envías `imagen`: Se mantiene la imagen actual

---

### 7. Actualizar Producto SIN Cambiar Imagen

```http
PUT /api/productos/{id}/sin-imagen
Content-Type: application/json
Authorization: Bearer {token}
```

**Path Parameters:**
- `id` (integer): ID del producto a actualizar

**Request Body:**
```json
{
  "nombreProducto": "Leche Entera Alpina Premium 1L",
  "tipoProducto": "PERECEDERO",
  "descripcionProducto": "Leche entera pasteurizada premium",
  "precioUnitario": 4000.00,
  "costoUnitario": 2500.00
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto actualizado exitosamente (sin cambiar imagen)",
  "data": {
    "idProducto": 45,
    "nombreProducto": "Leche Entera Alpina Premium 1L",
    "tipoProducto": "PERECEDERO",
    "descripcionProducto": "Leche entera pasteurizada premium",
    "precioUnitario": 4000.00,
    "costoUnitario": 2500.00,
    "urlImagen": "https://firebasestorage.googleapis.com/..."
  },
  "timestamp": "2025-10-26T19:00:00"
}
```

---

### 8. Eliminar Producto

```http
DELETE /api/productos/{id}
Authorization: Bearer {token}
```

**Path Parameters:**
- `id` (integer): ID del producto a eliminar

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Producto eliminado exitosamente",
  "data": null,
  "timestamp": "2025-10-26T19:00:00"
}
```

**Comportamiento:**
- Elimina el producto de la base de datos
- Elimina la imagen de Firebase Storage si existe

**Errores:**
- `404`: Producto no encontrado

---

## 📦 Modelos de Datos

### ProductoDTO
```typescript
{
  idProducto?: number               // Auto-generado, read-only
  nombreProducto: string            // Obligatorio, no vacío
  tipoProducto: string              // Obligatorio (ej: "PERECEDERO", "NO_PERECEDERO")
  descripcionProducto: string       // Obligatorio, no vacío
  precioUnitario: number            // Decimal, mínimo 0.01
  costoUnitario: number             // Decimal, mínimo 0.00
  urlImagen?: string | null         // URL de Firebase Storage, opcional
}
```

---

## 🖼️ Gestión de Imágenes con Firebase

### Configuración
- **Bucket:** `saferoute-f90f8.firebasestorage.app`
- **Ruta:** `/productos/{timestamp}_{nombre-normalizado}.{ext}`
- **Formatos:** PNG, JPG, JPEG, WebP
- **Tamaño máximo:** 10MB por archivo

### Nomenclatura de Archivos
```
1730000000_leche-entera-alpina-1l.png
└─────┬─────┘ └─────────┬──────────┘
   Timestamp      Nombre normalizado
```

### URL Generada
```
https://firebasestorage.googleapis.com/v0/b/saferoute-f90f8.appspot.com/o/productos%2F1730000000_leche-entera-alpina-1l.png?alt=media&token=abc123...
```

---

## 🔒 Seguridad

### Autorización
- **Roles permitidos:** `SAD`, `ADM`
- **Validación:** `@PreAuthorize("hasAnyRole('SAD', 'ADM')")`
- **Token:** Header `Authorization: Bearer {token}`

### Multipart Configuration
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 15MB
```

---

## ⚠️ Códigos de Error

| Código | Descripción |
|--------|-------------|
| 400 | Validación fallida (campos obligatorios, formatos incorrectos) |
| 403 | Sin permisos (no es SAD/ADM) |
| 404 | Producto no encontrado |
| 413 | Imagen excede 10MB |
| 415 | Formato de imagen no soportado |
| 500 | Error en Firebase Storage |

---

## 📝 Notas Importantes

### Configuración Postman para Multipart
1. **Campo `producto`:**
   - Type: **Text** (NO File)
   - Value: JSON válido como string sin escapar

2. **Campo `imagen`:**
   - Type: **File**
   - Seleccionar archivo desde explorador

### Tipos de Producto Comunes
- `PERECEDERO` - Productos con fecha de vencimiento (lácteos, carnes, etc.)
- `NO_PERECEDERO` - Productos de larga duración (arroz, pasta, enlatados, etc.)

### Validaciones
- `precioUnitario` debe ser mayor a `costoUnitario` (lógica de negocio recomendada)
- `nombreProducto` debe ser único (controlado en la lógica de negocio)

---

## 🧪 Ejemplos con cURL

### Crear Producto CON Imagen
```bash
curl -X POST "http://localhost:8080/api/productos" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F 'producto={"nombreProducto":"Leche Entera Alpina 1L","tipoProducto":"PERECEDERO","descripcionProducto":"Leche entera pasteurizada","precioUnitario":3500.00,"costoUnitario":2000.00}' \
  -F "imagen=@/path/to/image.png"
```

### Crear Producto SIN Imagen
```bash
curl -X POST "http://localhost:8080/api/productos/sin-imagen" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "nombreProducto": "Arroz Diana 500g",
    "tipoProducto": "NO_PERECEDERO",
    "descripcionProducto": "Arroz blanco de primera calidad",
    "precioUnitario": 2500.00,
    "costoUnitario": 1500.00
  }'
```

### Listar Productos
```bash
curl -X GET "http://localhost:8080/api/productos" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Buscar por Nombre
```bash
curl -X GET "http://localhost:8080/api/productos/buscar?nombre=Leche%20Entera%20Alpina%201L" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Actualizar Producto
```bash
curl -X PUT "http://localhost:8080/api/productos/45" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F 'producto={"nombreProducto":"Leche Entera Alpina Premium 1L","tipoProducto":"PERECEDERO","descripcionProducto":"Leche entera pasteurizada premium","precioUnitario":4000.00,"costoUnitario":2500.00}' \
  -F "imagen=@/path/to/new-image.png"
```

### Eliminar Producto
```bash
curl -X DELETE "http://localhost:8080/api/productos/45" \
  -H "Authorization: Bearer YOUR_TOKEN"
```
