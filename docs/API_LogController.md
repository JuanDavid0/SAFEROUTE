# API Documentation - Log Controller

## Información General

**Base URL:** `/logs`

**Descripción:** Controlador para consulta de logs de auditoría del sistema. Registra acciones de usuarios.

**Roles:** `SAD` (Solo Super Administrador)

---

## Endpoints

### 1. Obtener Todos los Logs

Obtiene todos los logs del sistema sin filtros.

**Endpoint:**
```
GET /logs
```

**Autenticación:** JWT (Solo SAD)

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 1250 log(s) en el sistema",
  "datos": [
    {
      "idLog": 1250,
      "idUsuario": 2,
      "nombreUsuario": "Admin Principal",
      "accion": "Creó pedido #5",
      "fechaLog": "2025-10-26T10:30:15"
    },
    {
      "idLog": 1249,
      "idUsuario": 25,
      "nombreUsuario": "María Fernanda López Martínez",
      "accion": "Modificó solicitud #48",
      "fechaLog": "2025-10-26T09:15:42"
    },
    {
      "idLog": 1248,
      "idUsuario": 2,
      "nombreUsuario": "Admin Principal",
      "accion": "Confirmó pago de solicitud #52",
      "fechaLog": "2025-10-26T08:45:30"
    }
  ],
  "timestamp": "2025-10-26T17:30:00"
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/logs" \
  -H "Authorization: Bearer <token_sad>"
```

**Notas:**
- Solo accesible por usuarios con rol SAD
- Devuelve logs ordenados por fecha descendente (más recientes primero)
- Sin paginación (considerar agregar para grandes volúmenes)

---

### 2. Obtener Logs por Usuario

Obtiene todos los logs de acciones realizadas por un usuario específico.

**Endpoint:**
```
GET /logs/usuario/{idUsuario}
```

**Autenticación:** JWT (Solo SAD)

**Path Parameters:**
- `idUsuario` (Integer) - ID del usuario

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 45 log(s) para el usuario con ID 25",
  "datos": [
    {
      "idLog": 1249,
      "idUsuario": 25,
      "nombreUsuario": "María Fernanda López Martínez",
      "accion": "Modificó solicitud #48",
      "fechaLog": "2025-10-26T09:15:42"
    },
    {
      "idLog": 1200,
      "idUsuario": 25,
      "nombreUsuario": "María Fernanda López Martínez",
      "accion": "Creó solicitud #48",
      "fechaLog": "2025-10-25T14:30:20"
    },
    {
      "idLog": 1150,
      "idUsuario": 25,
      "nombreUsuario": "María Fernanda López Martínez",
      "accion": "Agregó producto a solicitud #48",
      "fechaLog": "2025-10-25T15:00:10"
    }
  ],
  "timestamp": "2025-10-26T17:30:00"
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/logs/usuario/25" \
  -H "Authorization: Bearer <token_sad>"
```

**Notas:**
- Útil para auditoría de acciones de un usuario específico
- Muestra historial completo de actividades
- Incluye nombre del usuario para mejor legibilidad

---

### 3. Obtener Logs por Rango de Fechas

Obtiene logs registrados en un rango de fechas específico.

**Endpoint:**
```
GET /logs/fecha?fechaInicio={datetime}&fechaFin={datetime}
```

**Autenticación:** JWT (Solo SAD)

**Query Parameters:**
- `fechaInicio` (DateTime, requerido) - Formato: `YYYY-MM-DDTHH:mm:ss`
- `fechaFin` (DateTime, requerido) - Formato: `YYYY-MM-DDTHH:mm:ss`

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Se encontraron 85 log(s) en el rango de fechas especificado",
  "datos": [
    {
      "idLog": 1250,
      "idUsuario": 2,
      "nombreUsuario": "Admin Principal",
      "accion": "Creó pedido #5",
      "fechaLog": "2025-10-26T10:30:15"
    },
    {
      "idLog": 1249,
      "idUsuario": 25,
      "nombreUsuario": "María Fernanda López Martínez",
      "accion": "Modificó solicitud #48",
      "fechaLog": "2025-10-26T09:15:42"
    }
  ],
  "timestamp": "2025-10-26T17:30:00"
}
```

**Ejemplo cURL:**
```bash
curl -X GET "http://localhost:8080/logs/fecha?fechaInicio=2025-10-26T00:00:00&fechaFin=2025-10-26T23:59:59" \
  -H "Authorization: Bearer <token_sad>"
```

**Ejemplos de Rangos:**

**Logs del día actual:**
```bash
curl -X GET "http://localhost:8080/logs/fecha?fechaInicio=2025-10-26T00:00:00&fechaFin=2025-10-26T23:59:59" \
  -H "Authorization: Bearer <token_sad>"
```

**Logs de la última semana:**
```bash
curl -X GET "http://localhost:8080/logs/fecha?fechaInicio=2025-10-20T00:00:00&fechaFin=2025-10-26T23:59:59" \
  -H "Authorization: Bearer <token_sad>"
```

**Logs de un mes específico:**
```bash
curl -X GET "http://localhost:8080/logs/fecha?fechaInicio=2025-09-01T00:00:00&fechaFin=2025-09-30T23:59:59" \
  -H "Authorization: Bearer <token_sad>"
```

**Notas:**
- Formato ISO 8601: `YYYY-MM-DDTHH:mm:ss`
- Incluye fecha Y hora para precisión
- Útil para análisis de actividad por períodos

---

### Respuestas de error y fallos comunes

**Response 400 Bad Request:**
```json
{
  "exito": false,
  "mensaje": "Parámetros inválidos (formato de fecha incorrecto o parámetros faltantes).",
  "errores": [
    "fechaInicio debe tener formato YYYY-MM-DDTHH:mm:ss",
    "fechaFin debe tener formato YYYY-MM-DDTHH:mm:ss",
    "fechaFin no puede ser anterior a fechaInicio",
    "fechaInicio no puede ser futura"
  ],
  "datos": {
    "formatoEsperado": "YYYY-MM-DDTHH:mm:ss",
    "ejemploValido": "2025-10-26T17:30:00"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "exito": false,
  "mensaje": "Token inválido o expirado.",
  "datos": {
    "causa": "Token expirado",
    "tiempoExpiracion": "2025-10-26T17:00:00",
    "sugerencia": "Iniciar sesión nuevamente"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "exito": false,
  "mensaje": "Acceso denegado. Solo usuarios con rol SAD pueden usar este endpoint.",
  "datos": {
    "rolRequerido": "SAD",
    "rolUsuario": "ADM",
    "recurso": "/logs",
    "sugerencia": "Contacte al super administrador"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "exito": false,
  "mensaje": "No se encontraron logs para los criterios especificados.",
  "datos": {
    "idUsuario": 999,
    "fechaInicio": "2025-10-26T00:00:00",
    "fechaFin": "2025-10-26T23:59:59",
    "sugerencia": "Amplíe el rango de búsqueda o verifique el ID de usuario"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "exito": false,
  "mensaje": "Error en validación de datos.",
  "datos": {
    "rangoMaximo": "31 días",
    "fechaInicio": "2025-09-01T00:00:00",
    "fechaFin": "2025-10-31T23:59:59",
    "error": "El rango excede el límite permitido"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "exito": false,
  "mensaje": "Demasiadas solicitudes al sistema de logs.",
  "datos": {
    "limiteConsultas": 50,
    "tiempoVentana": "1 minuto",
    "tiempoEspera": 30,
    "consultasRestantes": 0
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "exito": false,
  "mensaje": "Error interno del servidor al recuperar logs.",
  "datos": {
    "tipo": "DatabaseException",
    "error": "Error de conexión a la base de datos de logs",
    "sugerencia": "Reintente en unos minutos"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

**Response 503 Service Unavailable:**
```json
{
  "exito": false,
  "mensaje": "Servicio de logs no disponible temporalmente.",
  "datos": {
    "estado": "Mantenimiento programado",
    "tiempoEstimadoReinicio": "2025-10-26T18:00:00",
    "sugerencia": "Reintente después del mantenimiento"
  },
  "timestamp": "2025-10-26T17:30:00"
}
```

### Advertencias y consideraciones

- **Límites de consulta:**
  - Máximo 50 consultas por minuto por usuario
  - Rango máximo de fechas: 31 días
  - Máximo 10,000 registros por consulta

- **Validaciones:**
  - fechaInicio ≤ fechaFin
  - fechaInicio no puede ser futura
  - fechaFin no puede ser futura
  - idUsuario debe existir en el sistema

- **Performance:**
  - Datos de más de 6 meses en almacenamiento frío
  - Consultas largas pueden ser lentas
  - Usar filtros para optimizar búsquedas

- **Seguridad:**
  - Solo accesible por SAD
  - Logs sensibles están enmascarados
  - No se pueden eliminar logs
  - Retención mínima: 2 años

- **Formato de fechas:**
  - Usar ISO 8601: YYYY-MM-DDTHH:mm:ss
  - Zona horaria: UTC-5 (Ecuador)
  - No se aceptan milisegundos

## Tipos de Acciones Registradas

### Ejemplos de Acciones en Logs

**Gestión de Pedidos:**
- `"Creó pedido #5"`
- `"Consolidó pedido #5"`
- `"Cambió estado de pedido #5 a FNL"`

**Gestión de Solicitudes:**
- `"Creó solicitud #48"`
- `"Modificó solicitud #48"`
- `"Agregó producto a solicitud #48"`
- `"Eliminó producto de solicitud #48"`
- `"Confirmó pago de solicitud #52"`
- `"Canceló solicitud #48"`

**Gestión de Productos:**
- `"Creó producto 'Arroz Premium 1kg'"`
- `"Actualizó producto #10"`
- `"Eliminó producto #15"`
- `"Actualizó imagen del producto #10"`

**Gestión de Usuarios:**
- `"Creó usuario 'María López'"`
- `"Eliminó usuario #25"`
- `"Cambió contraseña"`

**Autenticación:**
- `"Inicio de sesión exitoso"`
- `"Cerró sesión"`
- `"Intentó iniciar sesión (falló)"`

---

## Casos de Uso

### Caso 1: Auditoría de Actividad Diaria

**Escenario:** SAD revisa actividad del día.

```bash
GET /logs/fecha?fechaInicio=2025-10-26T00:00:00&fechaFin=2025-10-26T23:59:59

# Resultado:
# - 85 acciones registradas hoy
# - 15 solicitudes creadas
# - 8 pagos confirmados
# - 2 pedidos consolidados
```

### Caso 2: Investigar Acciones de Usuario Específico

**Escenario:** SAD investiga comportamiento sospechoso de un usuario.

```bash
GET /logs/usuario/25

# Resultado:
# - 45 acciones del usuario #25
# - Última acción: hace 2 horas
# - Modificó solicitud 3 veces seguidas
# - Actividad normal detectada
```

### Caso 3: Reporte de Actividad Mensual

**Escenario:** Generar reporte de octubre 2025.

```bash
GET /logs/fecha?fechaInicio=2025-10-01T00:00:00&fechaFin=2025-10-31T23:59:59

# Resultado:
# - 2,450 acciones en octubre
# - 450 solicitudes creadas
# - 380 pagos confirmados
# - 25 pedidos consolidados
```

### Caso 4: Revisar Todos los Logs del Sistema

**Escenario:** SAD necesita ver histórico completo para auditoría.

```bash
GET /logs

# Resultado:
# - 12,500 logs desde inicio del sistema
# - Primero: enero 2024
# - Último: hoy
```

---

## Modelo de Datos

### LogDTO
```typescript
interface LogDTO {
  idLog: number;                    // ID único del log
  idUsuario: number;                // ID del usuario que realizó la acción
  nombreUsuario: string;            // Nombre completo del usuario
  accion: string;                   // Descripción de la acción realizada
  fechaLog: string;                 // Fecha y hora (formato: "YYYY-MM-DDTHH:mm:ss")
}
```

### Ejemplo Completo
```json
{
  "idLog": 1250,
  "idUsuario": 2,
  "nombreUsuario": "Admin Principal",
  "accion": "Creó pedido #5",
  "fechaLog": "2025-10-26T10:30:15"
}
```

---

## Integración Frontend

### Ejemplo TypeScript - Dashboard de Logs

```typescript
interface LogDTO {
  idLog: number;
  idUsuario: number;
  nombreUsuario: string;
  accion: string;
  fechaLog: string;
}

async function obtenerLogsHoy(token: string): Promise<LogDTO[]> {
  const hoy = new Date();
  const inicio = `${hoy.toISOString().split('T')[0]}T00:00:00`;
  const fin = `${hoy.toISOString().split('T')[0]}T23:59:59`;

  const response = await axios.get<ApiResponse<LogDTO[]>>(
    `http://localhost:8080/logs/fecha?fechaInicio=${inicio}&fechaFin=${fin}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );

  return response.data.datos;
}

async function obtenerLogsUsuario(
  idUsuario: number,
  token: string
): Promise<LogDTO[]> {
  const response = await axios.get<ApiResponse<LogDTO[]>>(
    `http://localhost:8080/logs/usuario/${idUsuario}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );

  return response.data.datos;
}

// Componente React para Dashboard de Logs
function LogsPanel() {
  const [logs, setLogs] = React.useState<LogDTO[]>([]);
  const tokenSAD = localStorage.getItem('jwt_token_sad') || '';

  React.useEffect(() => {
    obtenerLogsHoy(tokenSAD)
      .then(setLogs)
      .catch(error => console.error('Error cargando logs:', error));
  }, []);

  return (
    <div>
      <h2>Logs del Sistema - Hoy</h2>
      <table>
        <thead>
          <tr>
            <th>Hora</th>
            <th>Usuario</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          {logs.map(log => (
            <tr key={log.idLog}>
              <td>{new Date(log.fechaLog).toLocaleTimeString()}</td>
              <td>{log.nombreUsuario}</td>
              <td>{log.accion}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## Mejoras Recomendadas

### 1. Paginación
```
GET /logs?page=0&size=50&sort=fechaLog,desc
```

### 2. Filtro por Tipo de Acción
```
GET /logs?tipoAccion=SOLICITUD
GET /logs?tipoAccion=PEDIDO
```

### 3. Búsqueda por Texto
```
GET /logs?buscar=solicitud #48
```

### 4. Exportar a Excel
```
GET /logs/exportar?fechaInicio=...&fechaFin=...
```

### 5. Nivel de Log
```typescript
enum NivelLog {
  INFO = "INFO",       // Acciones normales
  WARNING = "WARNING", // Acciones sospechosas
  ERROR = "ERROR"      // Errores del sistema
}
```

---

## Códigos de Estado

- `200 OK` - Logs obtenidos exitosamente
- `400 Bad Request` - Parámetros de fecha inválidos
- `401 Unauthorized` - Token inválido
- `403 Forbidden` - Usuario no tiene rol SAD
- `404 Not Found` - Usuario no encontrado
- `500 Internal Server Error` - Error del servidor

---

**Última actualización:** 26 de octubre de 2025
