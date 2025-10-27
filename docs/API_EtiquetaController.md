# API Documentation - Etiqueta Controller

## Información General

**Base URL:** `/etiquetas`

**Descripción:** Controlador para generar etiquetas de entrega de solicitudes en formato JSON o PDF.

**Roles:** `SAD`, `ADM`

---

## Endpoints

### 1. Generar Etiquetas JSON

Genera información de etiquetas en formato JSON para el pedido.

**Endpoint:**
```
GET /etiquetas/pedido/{id}/json
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**
- `id` (Integer) - ID del pedido

**Response (200 OK):**
```json
{
  "idPedido": 5,
  "fechaEntrega": "2025-10-30",
  "totalEtiquetas": 8,
  "etiquetas": [
    {
      "idSolicitud": 48,
      "nombreCliente": "María Fernanda López Martínez",
      "direccion": "Calle Mariscal Foch E7-45 y Diego de Almagro",
      "telefono": "0987654321",
      "productos": [
        {"nombre": "Arroz Premium 1kg", "cantidad": 5},
        {"nombre": "Aceite Girasol 1L", "cantidad": 3}
      ]
    }
  ]
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/etiquetas/pedido/5/json" \
  -H "Authorization: Bearer <token>"
```

---
### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "ID de pedido inválido o datos de entrada incorrectos.",
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
  "mensaje": "No tiene permisos para generar etiquetas de este pedido.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Pedido no encontrado o no existen solicitudes asociadas.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "exito": false,
  "mensaje": "Error al procesar las etiquetas.",
  "datos": {
    "idPedido": 5,
    "errores": [
      "Dirección faltante en solicitud #48",
      "Teléfono inválido en solicitud #52"
    ]
  },
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor al generar etiquetas. Intente nuevamente más tarde.",
  "timestamp": "2025-10-26T16:00:00"
}
```

### Advertencias y consideraciones

- **Estados de pedido**: Solo se pueden generar etiquetas para pedidos en estado `CSL` o `FNL`.
- **Validación de datos**: Se verifica que todas las solicitudes tengan dirección y teléfono válidos.
- **Formato PDF**: La generación de PDF puede fallar si hay caracteres especiales en los datos.
- **Tamaño del pedido**: Para pedidos grandes, la generación puede tomar más tiempo.
- **Campos requeridos**: 
  - Nombre del cliente
  - Dirección de entrega completa
  - Teléfono de contacto
  - Lista de productos y cantidades
- **Reintento automático**: En caso de fallo en PDF, el sistema reintenta hasta 3 veces.
- **Cache**: Las etiquetas generadas se cachean por 1 hora para mejorar el rendimiento."
}
```

---

### 2. Generar Etiquetas PDF

Genera archivo PDF con etiquetas de entrega listas para imprimir.

**Endpoint:**
```
GET /etiquetas/pedido/{id}/pdf
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**
- `id` (Integer) - ID del pedido

**Response (200 OK):**
- **Content-Type:** `application/pdf`
- **Content-Disposition:** `attachment; filename="Etiquetas_Pedido_5_20251026.pdf"`
- **Body:** Archivo PDF binario

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/etiquetas/pedido/5/pdf" \
  -H "Authorization: Bearer <token>" \
  --output etiquetas.pdf
```

**Notas:**
- Nombre del archivo: `Etiquetas_Pedido_{id}_{fecha}.pdf`
- Formato fecha: `yyyyMMdd` (ej: 20251026)
- PDF optimizado para impresión en hojas tamaño carta
- Cada etiqueta contiene: nombre cliente, dirección, teléfono, lista de productos

---

## Modelos

### EtiquetasResponseDTO
```typescript
interface EtiquetasResponseDTO {
  idPedido: number;
  fechaEntrega: string;             // Formato: "YYYY-MM-DD"
  totalEtiquetas: number;
  etiquetas: EtiquetaDTO[];
}
```

### EtiquetaDTO
```typescript
interface EtiquetaDTO {
  idSolicitud: number;
  nombreCliente: string;
  direccion: string;
  telefono: string;
  productos: {
    nombre: string;
    cantidad: number;
  }[];
}
```

---

**Última actualización:** 26 de octubre de 2025
