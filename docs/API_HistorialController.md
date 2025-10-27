# API Documentation - Historial Controller

## Información General

**Base URL:** `/historico`

**Descripción:** Controlador para consulta de histórico de pedidos finalizados (RF004). Permite filtrar por múltiples criterios.

**Roles:** `SAD`, `ADM`

---

## Endpoints

### 1. Consultar Histórico de Pedidos

Consulta pedidos históricos con filtros opcionales.

**Endpoint:**
```
GET /historico/pedidos?idCliente={id}&idProducto={id}&fechaInicio={fecha}&fechaFin={fecha}&estado={estado}
```

**Autenticación:** JWT (SAD, ADM)

**Query Parameters (Todos opcionales):**
- `idCliente` (Integer) - Filtrar por cliente específico
- `idProducto` (Integer) - Filtrar pedidos que contengan este producto
- `fechaInicio` (Date) - Fecha inicio del rango (formato: `YYYY-MM-DD`)
- `fechaFin` (Date) - Fecha fin del rango (formato: `YYYY-MM-DD`)
- `estado` (String) - Estado del pedido: `CSL`, `FNL`, `CRM`

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 12 pedido(s) en el histórico",
  "datos": [
    {
      "idPedido": 3,
      "idAdmin": 2,
      "estadoPedido": "FNL",
      "fechaCreado": "2025-09-15",
      "fechaCierre": "2025-09-25",
      "totalSolicitudes": 18,
      "precioTotal": 3456.75,
      "solicitudes": [
        {
          "idSolicitud": 35,
          "idCliente": 25,
          "nombreCliente": "María Fernanda López Martínez",
          "estadoSolicitud": "ENT",
          "direccionEntrega": "Calle Mariscal Foch E7-45",
          "fechaSolicitud": "2025-09-16",
          "productos": [...]
        }
      ]
    },
    {
      "idPedido": 2,
      "idAdmin": 2,
      "estadoPedido": "CRM",
      "fechaCreado": "2025-08-20",
      "fechaCierre": "2025-08-30",
      "totalSolicitudes": 8,
      "precioTotal": 1250.50,
      "solicitudes": []
    }
  ],
  "timestamp": "2025-10-26T17:00:00"
}
```

**Ejemplos de Uso:**

**Sin filtros (todos los pedidos históricos):**
```bash
curl -X GET "http://localhost:8080/historico/pedidos" \
  -H "Authorization: Bearer <token>"
```

**Filtrar por cliente:**
```bash
curl -X GET "http://localhost:8080/historico/pedidos?idCliente=25" \
  -H "Authorization: Bearer <token>"
```

**Filtrar por rango de fechas:**
```bash
curl -X GET "http://localhost:8080/historico/pedidos?fechaInicio=2025-09-01&fechaFin=2025-09-30" \
  -H "Authorization: Bearer <token>"
```

**Filtrar por producto:**
```bash
curl -X GET "http://localhost:8080/historico/pedidos?idProducto=10" \
  -H "Authorization: Bearer <token>"
```

**Filtrar por estado:**
```bash
curl -X GET "http://localhost:8080/historico/pedidos?estado=FNL" \
  -H "Authorization: Bearer <token>"
```

**Filtros combinados:**
```bash
curl -X GET "http://localhost:8080/historico/pedidos?idCliente=25&fechaInicio=2025-09-01&estado=FNL" \
  -H "Authorization: Bearer <token>"
```

**Notas:**
- Sin filtros devuelve todos los pedidos históricos
- Los filtros se combinan con operador AND (todos deben cumplirse)
- Estados históricos: `CSL` (Consolidado), `FNL` (Finalizado), `CRM` (Cerrado sin consolidar)
- Pedidos en estado `AGP` (Activos) no aparecen en histórico

---

### 2. Obtener Detalle de Pedido Histórico

Obtiene información completa de un pedido histórico específico.

**Endpoint:**
```
GET /historico/pedidos/{idPedido}
```

**Autenticación:** JWT (SAD, ADM)

**Path Parameters:**
- `idPedido` (Integer) - ID del pedido

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Detalle del pedido histórico obtenido exitosamente",
  "datos": {
    "idPedido": 3,
    "idAdmin": 2,
    "estadoPedido": "FNL",
    "fechaCreado": "2025-09-15",
    "fechaCierre": "2025-09-25",
    "totalSolicitudes": 18,
    "precioTotal": 3456.75,
    "solicitudes": [
      {
        "idSolicitud": 35,
        "idCliente": 25,
        "nombreCliente": "María Fernanda López Martínez",
        "idPedido": 3,
        "estadoSolicitud": "ENT",
        "direccionEntrega": "Calle Mariscal Foch E7-45 y Diego de Almagro",
        "fechaSolicitud": "2025-09-16",
        "fechaLimitePago": "2025-09-19",
        "modificacionesRestantes": 0,
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
      {
        "idSolicitud": 36,
        "idCliente": 18,
        "nombreCliente": "Pedro Antonio Silva Vargas",
        "estadoSolicitud": "ENT",
        "productos": [...]
      }
    ]
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/historico/pedidos/3" \
  -H "Authorization: Bearer <token>"
```

**Notas:**
- Incluye lista completa de solicitudes del pedido
- Cada solicitud incluye sus productos
- Útil para auditoría y análisis detallado
- Calcula totales automáticamente

---

### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "Parámetros de filtro inválidos (formato de fecha incorrecto o id no numérico).",
  "errores": [
    "fechaInicio debe tener formato YYYY-MM-DD",
    "idProducto debe ser un entero",
    "fechaFin no puede ser anterior a fechaInicio",
    "estado debe ser uno de: CSL, FNL, CRM"
  ],
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "exito": false,
  "mensaje": "Token inválido o expirado.",
  "datos": {
    "causa": "Token expirado",
    "tiempoExpiracion": "2025-10-26T16:00:00"
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 403 Forbidden:**
```json
{
  "exito": false,
  "mensaje": "No tiene permisos para acceder al histórico.",
  "datos": {
    "rolRequerido": ["SAD", "ADM"],
    "rolUsuario": "CLI"
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "Pedido no encontrado en el histórico.",
  "datos": {
    "idPedido": 999,
    "sugerencia": "Verifique que el pedido exista y esté en estado histórico (CSL, FNL, CRM)"
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "exito": false,
  "mensaje": "Error en validación de datos.",
  "datos": {
    "fechaInicio": "2025-12-31",
    "fechaFin": "2025-01-01",
    "error": "El rango de fechas es inválido"
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "exito": false,
  "mensaje": "Demasiadas solicitudes al histórico.",
  "datos": {
    "limiteConsultas": 100,
    "tiempoEspera": 60,
    "consultasRestantes": 0
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor al consultar el histórico.",
  "datos": {
    "tipo": "DatabaseException",
    "error": "Error de conexión a la base de datos"
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

### Advertencias y consideraciones

- **Límites de consulta:**
  - Máximo 100 consultas por minuto por usuario
  - Máximo 1000 registros por consulta
  - Rango máximo de fechas: 1 año

- **Validaciones:**
  - fechaInicio ≤ fechaFin
  - fechaFin no puede ser futura
  - estado debe ser válido (CSL, FNL, CRM)
  - IDs deben ser números positivos

- **Performance:**
  - Consultas sin filtros pueden ser lentas
  - Usar índices para optimizar búsquedas
  - Cacheo de resultados frecuentes

- **Consistencia:**
  - Los datos son de solo lectura
  - Actualización asíncrona del histórico
  - Posible latencia en nuevos registros

- **Paginación:**
  - Default: 50 registros por página
  - Máximo: 200 registros por página
  - Ordenado por fecha descendente

## Estados de Pedidos Históricos

| Estado | Código | Descripción |
|--------|--------|-------------|
| **Consolidado** | `CSL` | Pedido consolidado, esperando entregas |
| **Finalizado** | `FNL` | Todas las entregas completadas |
| **Cerrado sin consolidar** | `CRM` | Pedido cerrado sin alcanzar mínimos |

**Flujo de Estados:**
```
AGP (Activo) → CSL (Consolidado) → FNL (Finalizado)
AGP (Activo) → CRM (Cerrado sin consolidar)
```

---

## Casos de Uso

### Caso 1: Historial de Compras de un Cliente

**Escenario:** ADM quiere ver todos los pedidos en los que participó un cliente.

```bash
GET /historico/pedidos?idCliente=25

# Resultado:
# - 8 pedidos encontrados
# - Desde septiembre 2024 hasta octubre 2025
# - Total gastado calculado
# - Productos más comprados
```

### Caso 2: Análisis de Producto Popular

**Escenario:** SAD quiere ver en cuántos pedidos históricos se incluyó un producto.

```bash
GET /historico/pedidos?idProducto=10

# Resultado:
# - 15 pedidos encontrados con Arroz Premium
# - Desde enero 2025
# - Total de unidades vendidas
# - Ingresos generados
```

### Caso 3: Reporte Mensual

**Escenario:** Generar reporte de pedidos finalizados en septiembre 2025.

```bash
GET /historico/pedidos?fechaInicio=2025-09-01&fechaFin=2025-09-30&estado=FNL

# Resultado:
# - 12 pedidos finalizados en septiembre
# - 245 solicitudes procesadas
# - Ingresos totales del mes
```

### Caso 4: Auditoría de Pedido Específico

**Escenario:** Revisar detalle completo de un pedido cerrado.

```bash
GET /historico/pedidos/3

# Resultado:
# - Detalle completo del pedido #3
# - 18 solicitudes con estado de cada una
# - Productos entregados por cliente
# - Total recaudado: $3,456.75
```

---

## Modelos

### HistorialPedidoDTO
```typescript
interface HistorialPedidoDTO {
  idPedido: number;
  idAdmin: number;                  // Admin que gestionó el pedido
  estadoPedido: string;             // "CSL" | "FNL" | "CRM"
  fechaCreado: string;              // Formato: "YYYY-MM-DD"
  fechaCierre: string;              // Formato: "YYYY-MM-DD"
  totalSolicitudes: number;         // Cantidad de solicitudes del pedido
  precioTotal: number;              // Suma total del pedido
  solicitudes: SolicitudDTO[];      // Lista de solicitudes (puede estar vacía)
}
```

### Ejemplo con Solicitudes
```json
{
  "idPedido": 3,
  "idAdmin": 2,
  "estadoPedido": "FNL",
  "fechaCreado": "2025-09-15",
  "fechaCierre": "2025-09-25",
  "totalSolicitudes": 18,
  "precioTotal": 3456.75,
  "solicitudes": [
    {
      "idSolicitud": 35,
      "nombreCliente": "María Fernanda López Martínez",
      "estadoSolicitud": "ENT",
      "productos": [...]
    }
  ]
}
```

---

## Combinación de Filtros

### Filtros Disponibles

| Filtro | Tipo | Operador | Ejemplo |
|--------|------|----------|---------|
| `idCliente` | Integer | = | `idCliente=25` |
| `idProducto` | Integer | IN | `idProducto=10` |
| `fechaInicio` | Date | >= | `fechaInicio=2025-09-01` |
| `fechaFin` | Date | <= | `fechaFin=2025-09-30` |
| `estado` | String | = | `estado=FNL` |

### Ejemplos de Combinaciones

**Cliente en rango de fechas:**
```
/historico/pedidos?idCliente=25&fechaInicio=2025-09-01&fechaFin=2025-09-30
```

**Producto en pedidos finalizados:**
```
/historico/pedidos?idProducto=10&estado=FNL
```

**Todos los filtros:**
```
/historico/pedidos?idCliente=25&idProducto=10&fechaInicio=2025-09-01&fechaFin=2025-09-30&estado=FNL
```

---

## Integración Frontend

### Ejemplo TypeScript - Consulta con Filtros

```typescript
interface FiltrosHistorico {
  idCliente?: number;
  idProducto?: number;
  fechaInicio?: string;
  fechaFin?: string;
  estado?: 'CSL' | 'FNL' | 'CRM';
}

async function consultarHistorico(
  filtros: FiltrosHistorico,
  token: string
): Promise<HistorialPedidoDTO[]> {
  const params = new URLSearchParams();
  
  if (filtros.idCliente) params.append('idCliente', filtros.idCliente.toString());
  if (filtros.idProducto) params.append('idProducto', filtros.idProducto.toString());
  if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio);
  if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin);
  if (filtros.estado) params.append('estado', filtros.estado);

  const url = `http://localhost:8080/historico/pedidos?${params.toString()}`;

  const response = await axios.get<ApiResponse<HistorialPedidoDTO[]>>(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  return response.data.datos;
}

// Uso
const pedidosCliente = await consultarHistorico({ idCliente: 25 }, token);
const pedidosSeptiembre = await consultarHistorico({ 
  fechaInicio: '2025-09-01', 
  fechaFin: '2025-09-30' 
}, token);
```

---

## Códigos de Estado

- `200 OK` - Histórico obtenido exitosamente
- `401 Unauthorized` - Token inválido
- `403 Forbidden` - Usuario sin rol SAD/ADM
- `404 Not Found` - Pedido no encontrado (detalle específico)
- `500 Internal Server Error` - Error del servidor

---

**Última actualización:** 26 de octubre de 2025
