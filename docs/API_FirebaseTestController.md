# API Documentation - Firebase Test Controller

## Información General

**Base URL:** `/test/firebase`

**Descripción:** Controlador temporal para pruebas de integración con Firebase Storage. Permite probar subida, eliminación y actualización de imágenes.

**Roles:** `SAD`, `ADM`

**⚠️ IMPORTANTE:** Este es un controlador de **testing/desarrollo**. No debe usarse en producción.

---

## Endpoints

### 1. Subir Imagen de Prueba

Prueba la subida de una imagen a Firebase Storage.

**Endpoint:**
```
POST /test/firebase/upload?idProducto={id}
```

**Autenticación:** JWT (SAD, ADM)

**Query Parameters:**
- `idProducto` (Integer) - ID del producto (usado para nombrar archivo)

**Request Body (Multipart/Form-Data):**
- `imagen` (File) - Archivo de imagen

**Content-Type:** `multipart/form-data`

**Response (201 Created):**
```json
{
  "exito": true,
  "mensaje": "Imagen subida correctamente",
  "datos": {
    "urlImagen": "https://firebasestorage.googleapis.com/v0/b/saferoute-f90f8.appspot.com/o/productos%2Fproducto_10_1730000000000.jpg?alt=media&token=abc123",
    "mensaje": "Imagen subida exitosamente"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/test/firebase/upload?idProducto=10" \
  -H "Authorization: Bearer <token>" \
  -F "imagen=@/ruta/imagen.jpg"
```

**Ejemplo Postman:**
```
POST http://localhost:8080/test/firebase/upload?idProducto=10
Headers:
  Authorization: Bearer <token>
Body:
  Form-Data:
    imagen: [Seleccionar archivo]
```

**Notas:**
- Imagen se guarda en: `productos/producto_{idProducto}_{timestamp}.jpg`
- Formatos aceptados: JPG, JPEG, PNG
- Tamaño máximo: 10MB (configurado en `application.properties`)
- Retorna URL pública de Firebase Storage

---

### 2. Eliminar Imagen de Prueba

Prueba la eliminación de una imagen de Firebase Storage.

**Endpoint:**
```
DELETE /test/firebase/delete?urlImagen={url}
```

**Autenticación:** JWT (SAD, ADM)

**Query Parameters:**
- `urlImagen` (String) - URL completa de la imagen a eliminar

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Imagen eliminada correctamente",
  "datos": {
    "mensaje": "Imagen eliminada exitosamente"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Ejemplo cURL:**
```bash
curl -X DELETE "http://localhost:8080/test/firebase/delete?urlImagen=https://firebasestorage.googleapis.com/..." \
  -H "Authorization: Bearer <token>"
```

**Notas:**
- URL debe ser completa (incluir dominio de Firebase)
- Si la imagen no existe, no genera error
- Útil para limpiar imágenes de prueba

---

### 3. Actualizar Imagen de Prueba

Prueba la actualización de una imagen (elimina anterior y sube nueva).

**Endpoint:**
```
PUT /test/firebase/update?idProducto={id}&urlAnterior={url}
```

**Autenticación:** JWT (SAD, ADM)

**Query Parameters:**
- `idProducto` (Integer) - ID del producto
- `urlAnterior` (String, opcional) - URL de la imagen anterior a eliminar

**Request Body (Multipart/Form-Data):**
- `imagen` (File) - Nueva imagen

**Content-Type:** `multipart/form-data`

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Imagen actualizada correctamente",
  "datos": {
    "urlNueva": "https://firebasestorage.googleapis.com/v0/b/saferoute-f90f8.appspot.com/o/productos%2Fproducto_10_1730000500000.jpg?alt=media&token=xyz789",
    "urlAnterior": "https://firebasestorage.googleapis.com/v0/b/saferoute-f90f8.appspot.com/o/productos%2Fproducto_10_1730000000000.jpg?alt=media&token=abc123",
    "mensaje": "Imagen actualizada exitosamente"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Ejemplo cURL:**
```bash
curl -X PUT "http://localhost:8080/test/firebase/update?idProducto=10&urlAnterior=https://firebasestorage.googleapis.com/..." \
  -H "Authorization: Bearer <token>" \
  -F "imagen=@/ruta/nueva_imagen.jpg"
```

**Notas:**
- Si `urlAnterior` se proporciona, elimina la imagen vieja automáticamente
- Luego sube la nueva imagen
- Retorna ambas URLs (nueva y anterior)
- Si falla eliminación, aún sube la nueva imagen

---

### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "Parámetros inválidos o archivo faltante.",
  "errores": ["idProducto debe ser un entero","imagen es requerida para la subida"],
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "exito": false,
  "mensaje": "Token inválido o expirado.",
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 403 Forbidden:**
```json
{
  "exito": false,
  "mensaje": "No tiene permisos para realizar esta operación.",
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Recurso no encontrado (producto o archivo de imagen).",
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 409 Conflict:**
```json
{
  "exito": false,
  "mensaje": "Conflicto al actualizar imagen existente.",
  "datos": {
    "idProducto": 10,
    "error": "La URL anterior no coincide con la imagen actual del producto"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 413 Payload Too Large:**
```json
{
  "exito": false,
  "mensaje": "La imagen excede el tamaño máximo permitido (10MB).",
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 415 Unsupported Media Type:**
```json
{
  "exito": false,
  "mensaje": "Formato de imagen no soportado. Use: JPG, JPEG, PNG",
  "datos": {
    "formatosPermitidos": ["image/jpeg", "image/png"],
    "formatoRecibido": "image/gif"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "exito": false,
  "mensaje": "Demasiadas solicitudes. Límite excedido.",
  "datos": {
    "limitePorMinuto": 10,
    "esperarSegundos": 30
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error en Firebase Storage.",
  "datos": {
    "tipo": "StorageException",
    "detalles": "Error al conectar con Firebase Storage"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 503 Service Unavailable:**
```json
{
  "exito": false,
  "mensaje": "Servicio de Firebase Storage no disponible.",
  "datos": {
    "sugerencia": "Reintente en unos minutos"
  },
  "timestamp": "2025-10-26T18:00:00"
}
```

### Advertencias y consideraciones

- **Límites de Firebase Storage:**
  - Tamaño máximo por archivo: 10MB
  - Formatos permitidos: JPG, JPEG, PNG
  - Rate limit: 10 operaciones por minuto
  - Conexión: Timeout después de 30 segundos

- **Nombrado de archivos:**
  - Formato: `producto_{idProducto}_{timestamp}.{extension}`
  - No usar caracteres especiales en nombres
  - Se preserva la extensión original del archivo

- **Operaciones atómicas:**
  - La actualización (eliminar + subir) no es atómica
  - Si falla la subida, la imagen anterior podría perderse
  - Usar `urlAnterior` con precaución

- **Seguridad:**
  - URLs públicas tienen token de acceso limitado
  - Los tokens expiran después de 1 hora
  - No compartir URLs sin procesar

- **Reintentos automáticos:**
  - Subida: 3 intentos con backoff exponencial
  - Eliminación: 2 intentos
  - Actualización: Sin reintentos (operación manual)

**Response 413 Payload Too Large:**
```json
{
  "exito": false,
  "mensaje": "Archivo demasiado grande. Tamaño máximo permitido: 10MB.",
  "timestamp": "2025-10-26T18:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor o problema con Firebase/Twilio. Intente más tarde.",
  "timestamp": "2025-10-26T18:00:00"
}
```

## Configuración de Firebase

### Archivo de Configuración

El proyecto usa credenciales de Firebase en:
```
backend/src/main/resources/firebase-service-account.json
```

### Bucket de Storage
```
saferoute-f90f8.appspot.com
```

### Estructura de Carpetas
```
productos/
  ├── producto_10_1730000000000.jpg
  ├── producto_10_1730000500000.jpg
  ├── producto_12_1730001000000.png
  └── ...
```

### Nombre de Archivos
Formato: `producto_{idProducto}_{timestamp}.{extension}`

Ejemplo: `producto_10_1730000000000.jpg`

---

## Casos de Uso

### Caso 1: Probar Subida de Imagen

**Escenario:** Desarrollador quiere verificar integración con Firebase.

```bash
# 1. Subir imagen de prueba
POST /test/firebase/upload?idProducto=999
Body: imagen.jpg

# 2. Verificar URL funciona
# Abrir URL en navegador
https://firebasestorage.googleapis.com/.../producto_999_...jpg

# 3. Limpiar
DELETE /test/firebase/delete?urlImagen=https://...
```

### Caso 2: Probar Actualización de Imagen

**Escenario:** Verificar que actualización elimina imagen vieja.

```bash
# 1. Subir primera imagen
POST /test/firebase/upload?idProducto=999
# Retorna: urlImagen1

# 2. Actualizar con nueva imagen
PUT /test/firebase/update?idProducto=999&urlAnterior=urlImagen1
Body: nueva_imagen.jpg
# Retorna: urlNueva

# 3. Verificar que urlImagen1 ya no existe
# Abrir urlImagen1 → Error 404
```

### Caso 3: Probar Manejo de Errores

**Escenario:** Verificar validaciones de formato/tamaño.

```bash
# Archivo muy grande (>10MB)
POST /test/firebase/upload?idProducto=999
Body: imagen_20mb.jpg
# Retorna: Error 413 Payload Too Large

# Formato no soportado
POST /test/firebase/upload?idProducto=999
Body: documento.pdf
# Retorna: Error 400 Bad Request
```

---

## Validaciones

### Formatos Aceptados
- `image/jpeg` (.jpg, .jpeg)
- `image/png` (.png)

### Tamaño Máximo
- **10 MB** por archivo

### Configuración en `application.properties`
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
```

---

## Errores Comunes

### Error 413 - Payload Too Large
```json
{
  "exito": false,
  "mensaje": "El archivo excede el tamaño máximo permitido (10MB)",
  "datos": null
}
```

**Solución:** Reducir tamaño de imagen o comprimir.

### Error 400 - Formato No Soportado
```json
{
  "exito": false,
  "mensaje": "Formato de archivo no soportado. Use JPG o PNG",
  "datos": null
}
```

**Solución:** Convertir imagen a JPG o PNG.

### Error 500 - Firebase No Configurado
```json
{
  "exito": false,
  "mensaje": "Error al conectar con Firebase Storage",
  "datos": null
}
```

**Solución:** Verificar `firebase-service-account.json` esté presente.

---

## Diferencia con ProductoController

| Característica | FirebaseTestController | ProductoController |
|----------------|------------------------|-------------------|
| **Propósito** | Testing y desarrollo | Producción |
| **URL Base** | `/test/firebase` | `/productos` |
| **Validaciones** | Básicas | Completas (producto debe existir) |
| **Base de datos** | No actualiza BD | Actualiza campo `urlImagen` |
| **Uso recomendado** | Solo desarrollo | Producción |

---

## Endpoints de Producción

Para uso en producción, usar **ProductoController**:

```bash
# Crear producto con imagen
POST /productos
Body: MultipartForm (producto=JSON, imagen=File)

# Actualizar imagen de producto existente
PUT /productos/{id}/imagen
Body: MultipartForm (imagen=File)
```

Ver: [API_ProductoController.md](./API_ProductoController.md)

---

## Integración con Postman

### Colección de Pruebas Firebase

**1. Subir Imagen:**
```
POST http://localhost:8080/test/firebase/upload?idProducto=999
Headers:
  Authorization: Bearer {{token}}
Body:
  form-data:
    imagen: [File] arroz.jpg
```

**2. Eliminar Imagen:**
```
DELETE http://localhost:8080/test/firebase/delete
Headers:
  Authorization: Bearer {{token}}
Params:
  urlImagen: {{urlImagenPrueba}}
```

**3. Actualizar Imagen:**
```
PUT http://localhost:8080/test/firebase/update?idProducto=999
Headers:
  Authorization: Bearer {{token}}
Params:
  urlAnterior: {{urlImagenAnterior}}
Body:
  form-data:
    imagen: [File] arroz_nuevo.jpg
```

---

## Códigos de Estado

- `200 OK` - Operación exitosa (eliminar, actualizar)
- `201 Created` - Imagen subida exitosamente
- `400 Bad Request` - Formato inválido o parámetros faltantes
- `401 Unauthorized` - Token inválido
- `403 Forbidden` - Usuario sin rol SAD/ADM
- `413 Payload Too Large` - Archivo excede 10MB
- `500 Internal Server Error` - Error de Firebase Storage

---

## Desactivar en Producción

### Recomendación de Seguridad

Este controller debe **desactivarse en producción**:

**Opción 1: Perfil de Spring**
```java
@Profile("dev")
@RestController
@RequestMapping("/test/firebase")
public class FirebaseTestController {
    // ...
}
```

**Opción 2: Eliminar Completamente**
```bash
# Borrar archivo en producción
rm src/main/java/.../FirebaseTestController.java
```

**Opción 3: Comentar @RequestMapping**
```java
// @RestController
// @RequestMapping("/test/firebase")
public class FirebaseTestController {
    // ...
}
```

---

**Última actualización:** 26 de octubre de 2025

**⚠️ RECORDATORIO:** Este controller es solo para testing. Usar ProductoController en producción.
