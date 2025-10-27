# API Documentation - Excel Controller

## Información General

**Base URL:** `/excel`

**Descripción:** Controlador para generar reportes en formato Excel (.xlsx).

**Roles:** `SAD`, `ADM`

---

## Endpoints

### 1. Generar Informe de Cliente

Genera archivo Excel con el historial de compras de un cliente.

**Endpoint:**
```
GET /excel/cliente/{id}
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**
- `id` (Integer) - ID del cliente

**Response (200 OK):**
- **Content-Type:** `application/octet-stream`
- **Content-Disposition:** `attachment; filename="Compras_Cliente_25_20251026.xlsx"`
- **Body:** Archivo Excel binario

**Contenido del Excel:**
- Hoja 1: Datos del cliente
- Hoja 2: Historial de solicitudes
- Hoja 3: Resumen por producto
- Hoja 4: Totales y estadísticas

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/excel/cliente/25" \
  -H "Authorization: Bearer <token>" \
  --output compras_cliente.xlsx
```

---

### 2. Generar Informe de Pedido

Genera archivo Excel con información consolidada del pedido.

**Endpoint:**
```
GET /excel/pedido/{id}
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**

**Response (200 OK):**

**Contenido del Excel:**

---
### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "ID de cliente o pedido inválido.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "exito": false,
  "mensaje": "Token de autenticación inválido o expirado.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 403 Forbidden:**
```json
{
  "exito": false,
  "mensaje": "No tiene permisos para generar el informe solicitado.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Cliente o pedido no encontrado.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "exito": false,
  "mensaje": "Error al generar el archivo Excel.",
  "datos": {
    "errores": [
      "Datos insuficientes para generar el reporte",
      "Error al calcular totales en hoja 3"
    ],
    "sugerencia": "Verifique que el cliente/pedido tenga datos suficientes"
  },
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno al generar archivo Excel.",
  "timestamp": "2025-10-26T16:00:00"
}
```

### Advertencias y consideraciones

- **Tamaño de archivo**: Para historiales extensos, la generación puede tomar más tiempo.
- **Formato de datos**:
  - Las fechas se formatean como dd/MM/yyyy
  - Los montos se formatean con 2 decimales
  - Los números de teléfono mantienen el formato original
- **Validaciones**:
  - Se requiere al menos una transacción para generar el informe
  - Las hojas de cálculo tienen protección básica
- **Cache**: Los informes se cachean por 30 minutos para mejorar rendimiento
- **Límites**:
  - Máximo 50,000 filas por hoja
  - Tamaño máximo de archivo: 10MB
- **Compatibilidad**: 
  - Excel 2010 o superior
  - LibreOffice/OpenOffice
- **Seguridad**:
  - No incluye información sensible (contraseñas, tokens)
  - Las fórmulas están protegidas contra modificación
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor. Intente nuevamente más tarde.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/excel/pedido/5" \
  -H "Authorization: Bearer <token>" \
  --output informe_pedido.xlsx
```

**Notas:**
- Formato: Excel 2007+ (.xlsx)
- Nombre archivo: `{Tipo}_{ID}_{fecha}.xlsx`
- Formato fecha: `yyyyMMdd`
- Compatible con Microsoft Excel, LibreOffice, Google Sheets

---

**Última actualización:** 26 de octubre de 2025
