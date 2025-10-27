# OtpController API Documentation

## 📋 Información General
- **Base Path:** `/api/auth/otp`
- **Controlador:** `OtpController.java`
- **Seguridad:** 🌐 Endpoints públicos (no requieren autenticación previa)
- **Propósito:** Autenticación de clientes mediante código OTP vía SMS

---

## 🔐 Flujo de Autenticación OTP

```mermaid
sequenceDiagram
    Cliente->>API: POST /auth/otp/solicitar {cedula}
    API->>Twilio: Enviar SMS con código
    Twilio->>Cliente: SMS: "Tu código es 123456"
    Cliente->>API: POST /auth/otp/verificar {cedula, codigo}
    API->>Cliente: {token: "eyJ..."}
    Cliente->>API: Usar token en endpoints de solicitudes
```

---

## 🔐 Endpoints

### 1. Solicitar Código OTP

```http
POST /api/auth/otp/solicitar
Content-Type: application/json
```

**Descripción:** Genera y envía un código OTP de 6 dígitos al teléfono registrado del cliente.

**Seguridad:** 🌐 Público (no requiere token)

**Request Body:**
```json
{
  "cedula": "1234567890"
}
```

**Validaciones:**
- `cedula`: Obligatoria, exactamente 10 dígitos numéricos

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Código OTP enviado exitosamente al teléfono registrado",
  "data": {
    "exito": true,
    "mensaje": "Código OTP enviado al número terminado en **4567. El código expira en 5 minutos.",
    "token": null
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Comportamiento:**
- El código OTP es de **6 dígitos numéricos**
- Se envía vía SMS usando **Twilio**
- El código expira en **5 minutos**
- El usuario tiene **3 intentos** para verificarlo
- Si el cliente no existe en la base de datos, retorna error

**Errores y Respuestas HTTP:**

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Error de validación",
  "data": {
    "exito": false,
    "mensaje": "Formato de cédula inválido",
    "errores": [
      "La cédula debe contener exactamente 10 dígitos numéricos",
      "La cédula debe ser un número válido según algoritmo de verificación"
    ]
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Cliente no encontrado",
  "data": {
    "exito": false,
    "mensaje": "No existe un cliente registrado con la cédula proporcionada",
    "datos": {
      "cedula": "1234567890",
      "sugerencia": "Verifique el número de cédula o contacte a soporte"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Error de validación del servicio",
  "data": {
    "exito": false,
    "mensaje": "No se pudo procesar la solicitud",
    "datos": {
      "telefonoNoVerificado": true,
      "sugerencia": "El cliente debe verificar su número telefónico primero"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "status": "error",
  "message": "Límite de intentos excedido",
  "data": {
    "exito": false,
    "mensaje": "Demasiados intentos de solicitud de código OTP",
    "datos": {
      "tiempoEspera": 300,
      "intentosMaximos": 3,
      "intentosRealizados": 4,
      "proximoIntentoPermitido": "2025-10-26T18:35:00"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Error al enviar SMS",
  "data": {
    "exito": false,
    "mensaje": "No se pudo enviar el código OTP vía SMS",
    "datos": {
      "tipo": "TwilioException",
      "error": "Error de comunicación con el servicio SMS",
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
  "message": "Servicio SMS no disponible",
  "data": {
    "exito": false,
    "mensaje": "El servicio de SMS está temporalmente no disponible",
    "datos": {
      "estado": "Mantenimiento",
      "tiempoEstimadoReinicio": "2025-10-26T19:00:00",
      "metodosAlternativos": ["email", "llamada"]
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

### Advertencias y consideraciones

- **Límites de solicitud:**
  - Máximo 3 solicitudes de OTP por hora
  - Máximo 10 solicitudes de OTP por día
  - Espera de 5 minutos entre solicitudes

- **Seguridad:**
  - Los códigos OTP son de un solo uso
  - Se invalidan automáticamente al ser usados
  - Se registra la IP del solicitante
  - Se bloquean IPs sospechosas

- **Validaciones:**
  - La cédula debe ser válida según algoritmo
  - El teléfono debe estar verificado
  - No se permiten VoIP o números virtuales
  - Se verifica historial de fraude

- **SMS:**
  - Puede haber retrasos en la entrega
  - El número debe ser de Ecuador
  - Mensajes en español únicamente
  - No se reenvían códigos activos

- **Monitoreo:**
  - Se registran todos los intentos
  - Se notifica de patrones sospechosos
  - Alertas por múltiples fallos
  - Registro de tiempos de respuesta

---

### 2. Verificar Código OTP

```http
POST /api/auth/otp/verificar
Content-Type: application/json
```

**Descripción:** Verifica el código OTP ingresado y retorna un token JWT temporal si es correcto.

**Seguridad:** 🌐 Público (no requiere token)

**Request Body:**
```json
{
  "cedula": "1234567890",
  "codigoOtp": "123456"
}
```

**Validaciones:**
- `cedula`: Obligatoria, exactamente 10 dígitos numéricos
- `codigoOtp`: Obligatorio, exactamente 6 dígitos numéricos

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Código OTP verificado correctamente. Token de autorización generado",
  "data": {
    "exito": true,
    "mensaje": "Autenticación exitosa. Puede proceder a crear/modificar su solicitud.",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Token JWT Generado:**
- **Tipo:** Token temporal OTP
- **Claims:** Contiene la cédula del cliente
- **Uso:** Autoriza operaciones en endpoints de solicitudes
- **Duración:** Configurable (típicamente 24 horas)
- **Header:** `Authorization: Bearer {token}`

**Errores y Respuestas HTTP:**

**Response 400 Bad Request:**
```json
{
  "status": "error",
  "message": "Error de validación",
  "data": {
    "exito": false,
    "mensaje": "Formato de código OTP inválido",
    "errores": [
      "El código OTP debe contener exactamente 6 dígitos numéricos",
      "La cédula debe contener exactamente 10 dígitos numéricos"
    ]
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 401 Unauthorized:**
```json
{
  "status": "error",
  "message": "Código OTP incorrecto",
  "data": {
    "exito": false,
    "mensaje": "El código ingresado no es válido",
    "datos": {
      "intentosRestantes": 2,
      "tiempoRestanteExpiracion": 180,
      "sugerencia": "Verifique el código e intente nuevamente"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 403 Forbidden:**
```json
{
  "status": "error",
  "message": "Máximo de intentos alcanzado",
  "data": {
    "exito": false,
    "mensaje": "Ha excedido el número máximo de intentos",
    "datos": {
      "intentosMaximos": 3,
      "intentosRealizados": 3,
      "tiempoEspera": 300,
      "sugerencia": "Solicite un nuevo código OTP"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "Código OTP no encontrado",
  "data": {
    "exito": false,
    "mensaje": "No existe un código OTP activo para esta cédula",
    "datos": {
      "cedula": "1234567890",
      "sugerencia": "Solicite un nuevo código OTP",
      "posiblesCausas": [
        "El código ha expirado",
        "El código ya fue utilizado",
        "No se ha solicitado un código"
      ]
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 422 Unprocessable Entity:**
```json
{
  "status": "error",
  "message": "Código OTP expirado",
  "data": {
    "exito": false,
    "mensaje": "El código OTP ha expirado",
    "datos": {
      "fechaExpiracion": "2025-10-26T18:25:00",
      "tiempoExpirado": 300,
      "sugerencia": "Solicite un nuevo código OTP"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

**Response 429 Too Many Requests:**
```json
{
  "status": "error",
  "message": "Demasiados intentos de verificación",
  "data": {
    "exito": false,
    "mensaje": "Ha realizado demasiados intentos de verificación",
    "datos": {
      "limitePorMinuto": 5,
      "intentosRealizados": 6,
      "tiempoEspera": 60,
      "intentosRestantes": 0
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
    "mensaje": "Error al procesar la verificación del código OTP",
    "datos": {
      "tipo": "DatabaseException",
      "error": "Error al actualizar el estado del código OTP",
      "sugerencia": "Reintente en unos minutos"
    }
  },
  "timestamp": "2025-10-26T18:30:00"
}
```

---

## 📦 Modelos de Datos

### SolicitarOtpRequest
```typescript
{
  cedula: string        // Exactamente 10 dígitos numéricos
}
```

### VerificarOtpRequest
```typescript
{
  cedula: string        // Exactamente 10 dígitos numéricos
  codigoOtp: string     // Exactamente 6 dígitos numéricos
}
```

### OtpResponse
```typescript
{
  exito: boolean        // true si la operación fue exitosa
  mensaje: string       // Mensaje descriptivo para el usuario
  token?: string        // Token JWT (solo en verificación exitosa)
}
```

---

## 🔒 Seguridad

### Configuración OTP
```yaml
otp:
  length: 6                    # Longitud del código (6 dígitos)
  expiration-minutes: 5        # Código válido por 5 minutos
  max-attempts: 3              # Máximo 3 intentos de verificación
```

### Twilio SMS
- **Servicio:** Twilio SMS API
- **Número:** Configurado en variables de entorno
- **Formato SMS:** `"SafeRoute: Tu código de verificación es {codigo}. Válido por 5 minutos."`

### Token OTP
- **Claim especial:** `"tokenType": "OTP"`
- **Claim cédula:** `"cedula": "1234567890"`
- **Uso:** Solo endpoints de solicitudes aceptan este tipo de token
- **Validación:** El sistema verifica que el token contenga la cédula correcta

---

## ⚠️ Códigos de Error

| Código | Descripción |
|--------|-------------|
| 400 | Formato de cédula o código OTP inválido |
| 401 | Código OTP incorrecto |
| 403 | Máximo de intentos alcanzado |
| 404 | Cliente no registrado con esa cédula |
| 429 | Demasiadas solicitudes, rate limit excedido |
| 500 | Error al enviar SMS (Twilio) |

---

## 📝 Notas Importantes

### Límites y Restricciones
1. **Código OTP:**
   - 6 dígitos numéricos aleatorios
   - Expira en 5 minutos
   - Solo válido para la cédula que lo solicitó

2. **Intentos de Verificación:**
   - Máximo 3 intentos por código
   - Después de 3 intentos fallidos, debe solicitar nuevo código

3. **Rate Limiting:**
   - No puede solicitar múltiples códigos en corto tiempo
   - Debe esperar que expire el código anterior

### Casos de Uso
- **Cliente nuevo:** Puede usar OTP para crear solicitud sin registrarse
- **Cliente registrado:** Puede usar OTP como alternativa al login tradicional
- **Modificar solicitud:** Usar token OTP para modificar su propia solicitud

### Diferencia con Login Normal
- **Login normal (`/auth/login`):** Requiere contraseña, retorna token permanente
- **Login OTP:** No requiere contraseña, código temporal vía SMS, token temporal

---

## 🧪 Ejemplos con cURL

### Solicitar OTP
```bash
curl -X POST "http://localhost:8080/api/auth/otp/solicitar" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1234567890"
  }'
```

**Respuesta:**
```json
{
  "status": "success",
  "message": "Código OTP enviado exitosamente al teléfono registrado",
  "data": {
    "exito": true,
    "mensaje": "Código OTP enviado al número terminado en **4567. El código expira en 5 minutos."
  }
}
```

### Verificar OTP
```bash
curl -X POST "http://localhost:8080/api/auth/otp/verificar" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1234567890",
    "codigoOtp": "123456"
  }'
```

**Respuesta:**
```json
{
  "status": "success",
  "message": "Código OTP verificado correctamente. Token de autorización generado",
  "data": {
    "exito": true,
    "mensaje": "Autenticación exitosa. Puede proceder a crear/modificar su solicitud.",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Usar Token OTP en Solicitud
```bash
curl -X GET "http://localhost:8080/api/solicitudes/mis-solicitudes" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🔄 Flujo Completo de Ejemplo

### Escenario: Cliente quiere ver/modificar su solicitud

**Paso 1: Solicitar OTP**
```http
POST /api/auth/otp/solicitar
{
  "cedula": "1234567890"
}
```

**Paso 2: Cliente recibe SMS**
```
SafeRoute: Tu código de verificación es 456789. Válido por 5 minutos.
```

**Paso 3: Verificar OTP**
```http
POST /api/auth/otp/verificar
{
  "cedula": "1234567890",
  "codigoOtp": "456789"
}
```

**Paso 4: Recibe Token**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Paso 5: Usar Token para Ver Solicitudes**
```http
GET /api/solicitudes/mis-solicitudes
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Paso 6: Modificar Solicitud**
```http
PUT /api/solicitudes/789/modificar
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
{
  "direccionEntrega": "Nueva dirección"
}
```

---

## 📱 Integración con Frontend

### Ejemplo JavaScript
```javascript
// 1. Solicitar OTP
async function solicitarOTP(cedula) {
  const response = await fetch('http://localhost:8080/api/auth/otp/solicitar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ cedula })
  });
  
  const data = await response.json();
  console.log(data.data.mensaje); // Mostrar mensaje al usuario
}

// 2. Verificar OTP
async function verificarOTP(cedula, codigoOtp) {
  const response = await fetch('http://localhost:8080/api/auth/otp/verificar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ cedula, codigoOtp })
  });
  
  const data = await response.json();
  
  if (data.data.exito) {
    // Guardar token para usar en siguientes requests
    localStorage.setItem('otpToken', data.data.token);
    return data.data.token;
  } else {
    throw new Error(data.data.mensaje);
  }
}

// 3. Usar token en requests
async function misSolicitudes() {
  const token = localStorage.getItem('otpToken');
  
  const response = await fetch('http://localhost:8080/api/solicitudes/mis-solicitudes', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return await response.json();
}
```
