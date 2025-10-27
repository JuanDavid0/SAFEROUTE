# API Documentation - Cancelacion Controller

## Información General

**Base URL:** `/cancelar-solicitudes`

**Descripción:** Controlador para cancelación automática de solicitudes vencidas (que no pagaron antes de la fecha límite).

**Roles:** `SAD`, `ADM`

---

## Endpoints

### 1. Cancelar Solicitudes Vencidas de un Pedido

Cancela automáticamente todas las solicitudes pendientes con fecha límite de pago vencida de un pedido específico.

**Endpoint:**
```
POST /cancelar-solicitudes/cancelar-vencidas/{idPedido}
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**
- `idPedido` (Integer) - ID del pedido

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se cancelaron 5 solicitudes vencidas del pedido",
  "datos": {
    "idPedido": 5,
    "fechaCierre": "2025-10-25",
    "fechaProceso": "2025-10-26",
    "totalSolicitudesCanceladas": 5,
    "totalSolicitudesNoCanceladas": 0,
    "solicitudesCanceladas": [
      {
        "idSolicitud": 48,
        "nombreCliente": "María Fernanda López Martínez",
        "telefono": "0987654321",
        "notificacionEnviada": true
      },
      {
        "idSolicitud": 52,
        "nombreCliente": "Pedro Antonio Silva Vargas",
        "telefono": "0976543210",
        "notificacionEnviada": true
      }
    ],
    "errores": [],
    "exitoso": true
  },
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response (200 OK - Sin solicitudes vencidas):**
```json
{
  "exito": true,
  "mensaje": "No hay solicitudes vencidas para cancelar en este pedido",
  "datos": {
    "idPedido": 5,
    "fechaCierre": "2025-10-30",
  }
}
```

---
### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "ID de pedido inválido o datos de entrada incorrectos.",
  "errores": ["El ID debe ser un número entero positivo."],
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "exito": false,
  "mensaje": "Token de autenticación inválido o expirado.",
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "exito": false,
  "mensaje": "No tiene permisos para cancelar solicitudes en este pedido.",
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Pedido no encontrado o no existen solicitudes vencidas.",
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 409 Conflict:**
```json
{
  "exito": false,
  "mensaje": "El pedido no está en un estado válido para cancelación (solo AGP permitido).",
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "exito": false,
  "mensaje": "Error al procesar algunas cancelaciones.",
  "datos": {
    "idPedido": 5,
    "fechaProceso": "2025-10-26",
    "totalSolicitudesCanceladas": 3,
    "totalSolicitudesNoCanceladas": 2,
    "solicitudesCanceladas": [...],
    "errores": [
      {
        "idSolicitud": 48,
        "error": "Error al enviar notificación SMS"
      },
      {
        "idSolicitud": 52,
        "error": "La solicitud ya fue cancelada"
      }
    ],
    "exitoso": false
  },
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor. Intente nuevamente más tarde.",
  "timestamp": "2025-10-26T16:30:00"
}
```

### Advertencias y consideraciones

- **Cancelación parcial**: Si algunas solicitudes no se pueden cancelar (por ejemplo, error en SMS), el proceso continúa con las demás y retorna código 422 con detalles.
- **Notificaciones SMS**: Los errores de notificación no detienen el proceso de cancelación.
- **Estados de pedido**: Solo se pueden cancelar solicitudes de pedidos en estado `AGP` (Aguardando solicitudes).
- **Fecha límite**: Las solicitudes se consideran vencidas si la fecha actual es posterior a `fechaLimitePago`.
- **Idempotencia**: Ejecutar el endpoint múltiples veces es seguro, las solicitudes ya canceladas se ignoran.
  "mensaje": "No tiene permisos para cancelar solicitudes en este pedido.",
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Pedido no encontrado o no existen solicitudes vencidas.",
  "timestamp": "2025-10-26T16:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor. Intente nuevamente más tarde.",
  "timestamp": "2025-10-26T16:30:00"
}
```
    "fechaProceso": "2025-10-26",
    "totalSolicitudesCanceladas": 0,
    "totalSolicitudesNoCanceladas": 0,
    "solicitudesCanceladas": [],
    "errores": [],
    "exitoso": true
  },
  "timestamp": "2025-10-26T16:30:00"
}
```

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/cancelar-solicitudes/cancelar-vencidas/5" \
  -H "Authorization: Bearer <token>"
```

**Notas:**
- Solo cancela solicitudes con estado "PND" (Pendiente)
- Verifica que `fechaLimitePago < fechaActual`
- Cambia estado de solicitud a "CAN" (Cancelado)
- Intenta enviar notificación SMS al cliente
- Si SMS falla, aún cancela la solicitud pero marca `notificacionEnviada: false`

---

### 2. Cancelar Todas las Solicitudes Vencidas

Cancela automáticamente todas las solicitudes vencidas del sistema (todos los pedidos).

**Endpoint:**
```
POST /cancelar-solicitudes/cancelar-todas-vencidas
```

**Autenticación:** JWT (SAD, ADM)

**Request Body:** No requiere

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Proceso de cancelación masiva completado. 15 solicitudes canceladas en 3 pedidos.",
  "datos": {
    "idPedido": null,
    "fechaCierre": null,
    "fechaProceso": "2025-10-26",
    "totalSolicitudesCanceladas": 15,
    "totalSolicitudesNoCanceladas": 2,
    "solicitudesCanceladas": [
      {
        "idSolicitud": 48,
        "nombreCliente": "María Fernanda López Martínez",
        "telefono": "0987654321",
        "notificacionEnviada": true
      },
      {
        "idSolicitud": 52,
        "nombreCliente": "Pedro Antonio Silva Vargas",
        "telefono": "0976543210",
        "notificacionEnviada": false
      }
    ],
    "errores": [
      "Error al enviar SMS a 0976543210: Número inválido"
    ],
    "exitoso": true
  },
  "timestamp": "2025-10-26T16:30:00"
}
```

**Ejemplo cURL:**
```bash
curl -X POST "http://localhost:8080/cancelar-solicitudes/cancelar-todas-vencidas" \
  -H "Authorization: Bearer <token>"
```

**Notas:**
- Procesa TODOS los pedidos del sistema
- Busca solicitudes pendientes con fecha límite vencida
- Útil para ejecutar como tarea programada (cron job)
- Lista de errores incluye problemas al enviar notificaciones
- `totalSolicitudesNoCanceladas`: solicitudes que no se pudieron cancelar por errores

---

## Flujo de Cancelación

### Proceso Automático

1. **Identificación:**
   - Busca solicitudes con estado "PND"
   - Filtra por `fechaLimitePago < fechaActual`

2. **Cancelación:**
   - Cambia estado solicitud: PND → CAN
   - Registra fecha de cancelación

3. **Notificación:**
   - Intenta enviar SMS al cliente vía Twilio
   - Mensaje: "Su solicitud #{id} fue cancelada por vencimiento del plazo de pago"
   - Si falla SMS, continúa pero marca error

4. **Reporte:**
   - Devuelve lista de solicitudes canceladas
   - Incluye estado de notificaciones
   - Lista errores encontrados

### Diferencia entre Endpoints

| Característica | Cancelar por Pedido | Cancelar Todas |
|----------------|---------------------|----------------|
| **Alcance** | Un pedido específico | Todos los pedidos |
| **Uso** | Manual por pedido | Tarea programada |
| **idPedido en Response** | ID del pedido | `null` |
| **Rendimiento** | Rápido | Puede tardar minutos |

---

## Modelos

### CancelacionSolicitudesDTO
```typescript
interface CancelacionSolicitudesDTO {
  idPedido: number | null;                        // ID del pedido o null si es masivo
  fechaCierre: string | null;                     // Fecha cierre del pedido (YYYY-MM-DD)
  fechaProceso: string;                           // Fecha de ejecución (YYYY-MM-DD)
  totalSolicitudesCanceladas: number;             // Solicitudes canceladas exitosamente
  totalSolicitudesNoCanceladas: number;           // Solicitudes que no se pudieron cancelar
  solicitudesCanceladas: SolicitudCanceladaDTO[]; // Detalle de cancelaciones
  errores: string[];                              // Lista de errores durante el proceso
  mensaje: string;                                // Mensaje descriptivo del resultado
  exitoso: boolean;                               // true si proceso completó sin errores críticos
}
```

### SolicitudCanceladaDTO
```typescript
interface SolicitudCanceladaDTO {
  idSolicitud: number;
  nombreCliente: string;
  telefono: string;
  notificacionEnviada: boolean;                   // true si SMS enviado exitosamente
}
```

---

## Casos de Uso

### Caso 1: Cancelación Manual por Pedido

**Escenario:** ADM cerró un pedido y quiere cancelar solicitudes no pagadas.

```bash
# Pedido 5 cerró el 2025-10-25
# Hoy es 2025-10-26
POST /cancelar-solicitudes/cancelar-vencidas/5

# Resultado:
# - 5 solicitudes pendientes encontradas
# - Todas con fechaLimitePago < 2025-10-26
# - 5 solicitudes canceladas
# - 5 SMS enviados
```

### Caso 2: Tarea Programada Nocturna

**Escenario:** Cron job ejecuta cancelación masiva cada día a las 2:00 AM.

```javascript
// Cron job configuration
0 2 * * * curl -X POST "http://localhost:8080/cancelar-solicitudes/cancelar-todas-vencidas" \
  -H "Authorization: Bearer <service_token>"

// Resultado típico:
// - 15 solicitudes vencidas encontradas en 3 pedidos diferentes
// - 13 SMS enviados exitosamente
// - 2 SMS fallidos (números inválidos)
// - 15 solicitudes canceladas de todas formas
```

### Caso 3: Manejo de Errores de Notificación

**Escenario:** Algunos clientes tienen números telefónicos inválidos.

```json
{
  "totalSolicitudesCanceladas": 5,
  "solicitudesCanceladas": [
    {
      "idSolicitud": 48,
      "telefono": "0987654321",
      "notificacionEnviada": true      // ✅ SMS exitoso
    },
    {
      "idSolicitud": 52,
      "telefono": "0976543210",
      "notificacionEnviada": false     // ❌ SMS falló
    }
  ],
  "errores": [
    "Error al enviar SMS a 0976543210: Número inválido"
  ],
  "exitoso": true                       // Proceso completó (solicitud se canceló igual)
}
```

---

## Integración con Twilio SMS

### Mensaje de Notificación

**Plantilla del SMS:**
```
SAFEROUTE: Su solicitud #{idSolicitud} fue cancelada por vencimiento del plazo de pago. 
Fecha límite: {fechaLimitePago}. 
Para más información contacte al administrador.
```

**Ejemplo:**
```
SAFEROUTE: Su solicitud #48 fue cancelada por vencimiento del plazo de pago. 
Fecha límite: 2025-10-25. 
Para más información contacte al administrador.
```

### Manejo de Errores SMS

El sistema es **resiliente** ante fallos de SMS:
- Si Twilio falla → Solicitud se cancela de todas formas
- Error se registra en array `errores`
- `notificacionEnviada: false` para esa solicitud
- Proceso continúa con siguientes solicitudes

---

## Configuración de Tarea Programada

### Ejemplo con Spring Scheduler

```java
@Configuration
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    private ICancelacionSolicitudService cancelacionService;

    // Ejecutar todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cancelarSolicitudesVencidas() {
        log.info("Iniciando cancelación automática de solicitudes vencidas");
        CancelacionSolicitudesDTO resultado = 
            cancelacionService.cancelarTodasSolicitudesVencidas();
        log.info("Canceladas {} solicitudes", 
            resultado.getTotalSolicitudesCanceladas());
    }
}
```

### Ejemplo con Cron Job (Linux)

```bash
# Ejecutar todos los días a las 2:00 AM
0 2 * * * curl -X POST "http://localhost:8080/cancelar-solicitudes/cancelar-todas-vencidas" \
  -H "Authorization: Bearer ${SERVICE_TOKEN}" >> /var/log/saferoute/cancelaciones.log 2>&1
```

---

## Códigos de Estado

- `200 OK` - Proceso completado (con o sin cancelaciones)
- `400 Bad Request` - Error crítico que impidió el proceso
- `401 Unauthorized` - Token inválido
- `403 Forbidden` - Usuario sin rol SAD/ADM
- `404 Not Found` - Pedido no encontrado (endpoint específico)
- `500 Internal Server Error` - Error del servidor

---

**Última actualización:** 26 de octubre de 2025
