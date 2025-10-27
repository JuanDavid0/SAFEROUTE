# API Documentation - Consolidacion Controller

## Información General

**Base URL:** `/consolidacion`

**Descripción:** Controlador para consolidar pedidos. Suma todas las solicitudes pagadas de un pedido y cierra el pedido.

**Roles:** `SAD`, `ADM`

---

## Endpoint

### Consolidar Pedido

Consolida un pedido sumando todas las solicitudes pagadas y cambiando el estado del pedido a "CSL" (Consolidado).

**Endpoint:**
```
POST /consolidacion/pedido/{idPedido}
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**
- `idPedido` (Integer) - ID del pedido a consolidar

**Request Body (Opcional):**
```json
{
  "incluirPendientes": false
}
```

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Pedido consolidado exitosamente",
  "datos": {
    "idPedido": 5,
    "nuevoEstado": "CSL",
    "totalSolicitudes": 8,
    "montoTotal": 1547.80,
    "productos": [
      {
        "idProducto": 10,
        "nombreProducto": "Arroz Premium 1kg",
        "cantidadTotal": 45
      },
      {
        "idProducto": 12,
        "nombreProducto": "Aceite Girasol 1L",
        "cantidadTotal": 28
      }
    ],
    "mensaje": "Consolidación completada. 8 solicitudes procesadas."
  },
  "timestamp": "2025-10-26T16:00:00"
}
```

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/consolidacion/pedido/5" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
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
  "mensaje": "No tiene permisos para consolidar este pedido.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Pedido no encontrado.",
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 409 Conflict:**
```json
{
  "exito": false,
  "mensaje": "El pedido no puede ser consolidado en su estado actual.",
  "datos": {
    "idPedido": 5,
    "estadoActual": "CSL",
    "razon": "El pedido ya está consolidado"
  },
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "exito": false,
  "mensaje": "No hay suficientes solicitudes pagadas para consolidar el pedido.",
  "datos": {
    "idPedido": 5,
    "solicitudesPagadas": 3,
    "minimoRequerido": 5,
    "sugerencia": "Espere más pagos o use incluirPendientes=true"
  },
  "timestamp": "2025-10-26T16:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor al consolidar el pedido.",
  "timestamp": "2025-10-26T16:00:00"
}
```

### Advertencias y consideraciones

- **Estados válidos**: Solo se pueden consolidar pedidos en estado `AGP` (Aguardando solicitudes).
- **Mínimo de solicitudes**: Por defecto se requiere un mínimo de solicitudes pagadas para consolidar.
- **incluirPendientes**: Si es `true`, incluye solicitudes pendientes en la consolidación.
- **Validación de productos**: Se verifica que cada producto alcance su cantidad mínima.
- **Notificaciones**: Al consolidar, se notifica a los clientes con solicitudes pendientes.
- **Irreversible**: Una vez consolidado (estado `CSL`), no se puede revertir el proceso.
- **Idempotencia**: Intentar consolidar un pedido ya consolidado retorna error 409.

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Pedido no encontrado o no existen solicitudes pagadas.",
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

**Notas:**
- Solo consolida solicitudes con estado "PGD" (Pagado)
- Cambia estado del pedido de "AGP" → "CSL"
- Suma cantidades de productos por tipo
- Calcula monto total de todas las solicitudes

---

## Modelos

### ConsolidacionDTO
```typescript
interface ConsolidacionDTO {
  idPedido: number;
  nuevoEstado: string;              // "CSL"
  totalSolicitudes: number;          // Cantidad de solicitudes consolidadas
  montoTotal: number;                // Suma total en dinero
  productos: ConsolidacionProductoDTO[];
  mensaje: string;
}
```

### ConsolidacionProductoDTO
```typescript
interface ConsolidacionProductoDTO {
  idProducto: number;
  nombreProducto: string;
  cantidadTotal: number;             // Suma de todas las cantidades solicitadas
}
```

---

**Última actualización:** 26 de octubre de 2025
