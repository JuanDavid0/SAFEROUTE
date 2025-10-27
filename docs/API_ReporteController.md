# API Documentation - Reporte Controller

## Información General

**Base URL:** `/reportes`

**Descripción:** Controlador para generar reportes y estadísticas del sistema.

**Roles:** `SAD`, `ADM`

---

### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Parámetros de reporte inválidos",
  "data": {
    "exito": false,
    "errores": [
      "fechaInicio: debe tener formato YYYY-MM-DD",
      "fechaFin: debe tener formato YYYY-MM-DD",
      "fechaFin: no puede ser anterior a fechaInicio",
      "agrupacion: debe ser TRIMESTRAL o ANUAL"
    ],
    "datos": {
      "formatoFecha": "YYYY-MM-DD",
      "formatosAgrupacion": ["TRIMESTRAL", "ANUAL"],
      "ejemploValido": {
        "fechaInicio": "2025-01-01",
        "fechaFin": "2025-12-31",
        "agrupacion": "TRIMESTRAL"
      }
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "status": "error",
  "message": "No autenticado",
  "data": {
    "exito": false,
    "mensaje": "Token inválido o expirado",
    "datos": {
      "causa": "Token expirado",
      "tiempoExpiracion": "2025-10-26T16:30:00",
      "sugerencia": "Renueve su sesión"
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 403 Forbidden:**
```json
{
  "status": "error",
  "message": "Acceso denegado",
  "data": {
    "exito": false,
    "mensaje": "No tiene permisos para generar reportes",
    "datos": {
      "rolesPermitidos": ["SAD", "ADM"],
      "rolUsuario": "CLI",
      "tipoReporte": "ingresos",
      "sugerencia": "Contacte a un administrador"
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Datos no encontrados",
  "data": {
    "exito": false,
    "mensaje": "No hay datos para los criterios especificados",
    "datos": {
      "criterios": {
        "fechaInicio": "2025-01-01",
        "fechaFin": "2025-01-31",
        "tipoReporte": "ingresos"
      },
      "sugerencia": "Amplíe el rango de fechas o modifique los filtros"
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Error de validación de reporte",
  "data": {
    "exito": false,
    "mensaje": "No se puede generar el reporte",
    "datos": {
      "validaciones": [
        "El rango de fechas excede 1 año",
        "Se requieren al menos 3 meses de datos",
        "La agrupación ANUAL requiere datos completos del año"
      ]
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "status": "error",
  "message": "Demasiadas solicitudes",
  "data": {
    "exito": false,
    "mensaje": "Ha excedido el límite de generación de reportes",
    "datos": {
      "limiteHora": 10,
      "reportesGenerados": 11,
      "tiempoEspera": 1800,
      "sugerencia": "Descargue reportes existentes del caché"
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Error interno",
  "data": {
    "exito": false,
    "mensaje": "Error al generar el reporte",
    "datos": {
      "tipo": "ReportGenerationException",
      "error": "Error al procesar datos históricos",
      "referencia": "RPT-2025102617000001",
      "sugerencia": "Reintente más tarde"
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

**Response 503 Service Unavailable:**
```json
{
  "status": "error",
  "message": "Servicio no disponible",
  "data": {
    "exito": false,
    "mensaje": "Generador de reportes no disponible",
    "datos": {
      "estado": "Mantenimiento programado",
      "inicio": "2025-10-26T17:00:00",
      "fin": "2025-10-26T18:00:00",
      "alternativa": "Use reportes en caché"
    }
  },
  "timestamp": "2025-10-26T17:00:00"
}
```

### Advertencias y consideraciones

- **Límites de reportes:**
  - Máximo 10 reportes por hora
  - Rango máximo: 1 año
  - Tamaño máximo: 50MB
  - Caché: 24 horas

- **Validaciones temporales:**
  - Fechas en formato YYYY-MM-DD
  - Rango mínimo: 3 meses
  - No fechas futuras
  - Agrupación según rango

- **Performance:**
  - Reportes grandes son asíncronos
  - Usar caché cuando sea posible
  - Compresión de respuestas
  - Paginación en listas largas

- **Datos históricos:**
  - Disponibles hasta 2 años atrás
  - Agregados por períodos
  - Precisión hasta centavos
  - Archivado automático

- **Exportación:**
  - Formatos: JSON, CSV, PDF
  - Excel con macros bloqueadas
  - Codificación UTF-8
  - Firma digital opcional

## Endpoints

### 1. Reporte de Ingresos

Genera reporte de ingresos en un rango de fechas con agrupación trimestral o anual.

**Endpoint:**
```
GET /reportes/ingresos?fechaInicio={fecha}&fechaFin={fecha}&agrupacion={tipo}
```

**Query Parameters:**
- `fechaInicio` (Date, requerido) - Formato: `YYYY-MM-DD`
- `fechaFin` (Date, requerido) - Formato: `YYYY-MM-DD`
- `agrupacion` (String, opcional) - `TRIMESTRAL` o `ANUAL` (default: `TRIMESTRAL`)

**Response (200 OK):**
```json
{
  "fechaInicio": "2025-01-01",
  "fechaFin": "2025-10-26",
  "totalIngresos": 45678.90,
  "totalPedidos": 35,
  "periodos": [
    {"periodo": "Q1 2025", "ingresos": 12345.50},
    {"periodo": "Q2 2025", "ingresos": 15678.20},
    {"periodo": "Q3 2025", "ingresos": 17655.20}
  ]
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/reportes/ingresos?fechaInicio=2025-01-01&fechaFin=2025-10-26&agrupacion=TRIMESTRAL" \
  -H "Authorization: Bearer <token>"
```

---

### 2. Productos Más Vendidos

Obtiene los productos más vendidos (por cantidad).

**Endpoint:**
```
GET /reportes/productos-mas-vendidos?limite={numero}
```

**Query Parameters:**
- `limite` (Integer, opcional) - Número de productos (1-100, default: 10)

**Response (200 OK):**
```json
[
  {
    "idProducto": 10,
    "nombreProducto": "Arroz Premium 1kg",
    "cantidadVendida": 450,
    "totalIngresos": 832.50
  },
  {
    "idProducto": 12,
    "nombreProducto": "Aceite Girasol 1L",
    "cantidadVendida": 320,
    "totalIngresos": 1040.00
  }
]
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/reportes/productos-mas-vendidos?limite=5" \
  -H "Authorization: Bearer <token>"
```

---

### 3. Productos con Mayor Ganancia

Obtiene los productos con mayor ganancia (precio - costo).

**Endpoint:**
```
GET /reportes/productos-mayor-ganancia?limite={numero}
```

**Query Parameters:**
- `limite` (Integer, opcional) - Número de productos (1-100, default: 10)

**Response (200 OK):**
```json
[
  {
    "idProducto": 15,
    "nombreProducto": "Chocolate Premium 200g",
    "cantidadVendida": 180,
    "totalIngresos": 2700.00,
    "gananciaTotal": 1440.00
  }
]
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/reportes/productos-mayor-ganancia?limite=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4. Clientes Frecuentes

Obtiene los clientes con más solicitudes realizadas.

**Endpoint:**
```
GET /reportes/clientes-frecuentes?limite={numero}
```

**Query Parameters:**
- `limite` (Integer, opcional) - Número de clientes (1-100, default: 20)

**Response (200 OK):**
```json
[
  {
    "idCliente": 25,
    "nombreCliente": "María Fernanda López Martínez",
    "totalSolicitudes": 15,
    "totalGastado": 1250.75
  },
  {
    "idCliente": 18,
    "nombreCliente": "Pedro Antonio Silva Vargas",
    "totalSolicitudes": 12,
    "totalGastado": 980.50
  }
]
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/reportes/clientes-frecuentes?limite=20" \
  -H "Authorization: Bearer <token>"
```

---

### 5. Pedidos en Curso

Obtiene lista de pedidos activos (estado AGP o CSL).

**Endpoint:**
```
GET /reportes/pedidos-en-curso
```

**Response (200 OK):**
```json
[
  {
    "idPedido": 5,
    "estadoPedido": "AGP",
    "fechaCreado": "2025-10-20",
    "fechaCierre": "2025-10-30",
    "totalSolicitudes": 15,
    "solicitudesPagadas": 8
  },
  {
    "idPedido": 6,
    "estadoPedido": "CSL",
    "fechaCreado": "2025-10-15",
    "fechaCierre": "2025-10-25",
    "totalSolicitudes": 22,
    "solicitudesPagadas": 22
  }
]
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/reportes/pedidos-en-curso" \
  -H "Authorization: Bearer <token>"
```

---

### 6. Resumen de Estadísticas

Obtiene resumen general del sistema (dashboard principal).

**Endpoint:**
```
GET /reportes/resumen
```

**Response (200 OK):**
```json
{
  "totalClientes": 250,
  "totalProductos": 85,
  "totalPedidos": 45,
  "pedidosActivos": 3,
  "solicitudesPendientes": 12,
  "solicitudesPagadas": 180,
  "ingresosMesActual": 15678.90,
  "ingresosTotales": 125456.80
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/reportes/resumen" \
  -H "Authorization: Bearer <token>"
```

**Notas:**
- Útil para dashboard administrativo
- Muestra métricas clave del negocio
- Actualizado en tiempo real

---

## Modelos

### ReporteIngresosDTO
```typescript
interface ReporteIngresosDTO {
  fechaInicio: string;
  fechaFin: string;
  totalIngresos: number;
  totalPedidos: number;
  periodos: {
    periodo: string;
    ingresos: number;
  }[];
}
```

### ProductoVendidoDTO
```typescript
interface ProductoVendidoDTO {
  idProducto: number;
  nombreProducto: string;
  cantidadVendida: number;
  totalIngresos: number;
  gananciaTotal?: number;       // Solo en productos mayor ganancia
}
```

### ClienteFrecuenteDTO
```typescript
interface ClienteFrecuenteDTO {
  idCliente: number;
  nombreCliente: string;
  totalSolicitudes: number;
  totalGastado: number;
}
```

### PedidoEnCursoDTO
```typescript
interface PedidoEnCursoDTO {
  idPedido: number;
  estadoPedido: string;         // "AGP" | "CSL"
  fechaCreado: string;
  fechaCierre: string;
  totalSolicitudes: number;
  solicitudesPagadas: number;
}
```

### ResumenEstadisticasDTO
```typescript
interface ResumenEstadisticasDTO {
  totalClientes: number;
  totalProductos: number;
  totalPedidos: number;
  pedidosActivos: number;
  solicitudesPendientes: number;
  solicitudesPagadas: number;
  ingresosMesActual: number;
  ingresosTotales: number;
}
```

---

**Última actualización:** 26 de octubre de 2025
