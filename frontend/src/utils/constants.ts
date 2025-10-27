/**
 * Constantes Globales de SAFE ROUTE
 */

// Estados de Solicitud
export const ESTADOS_SOLICITUD = {
  PND: 'Pendiente de pago',
  PGD: 'Pagado',
  ENT: 'Entregado',
  CAN: 'Cancelado',
} as const;

// Estados de Pedido
export const ESTADOS_PEDIDO = {
  AGP: 'Aguardando pago',
  CSL: 'Consolidado',
  FNL: 'Finalizado',
  CRM: 'Con remisión',
} as const;

// Roles
export const ROLES = {
  CLI: 'Cliente',
  ADM: 'Administrador',
  SAD: 'Super Administrador',
} as const;

// Tipos de Producto
export const TIPOS_PRODUCTO = [
  'Granos',
  'Lácteos',
  'Carnes',
  'Frutas',
  'Verduras',
  'Abarrotes',
  'Bebidas',
  'Aseo',
  'Otros',
] as const;

// Validaciones
export const VALIDACIONES = {
  CEDULA_LENGTH: 10,
  TELEFONO_LENGTH: 10,
  OTP_LENGTH: 6,
  PASSWORD_MIN_LENGTH: 6,
  MODIFICACIONES_MAX: 3,
  OTP_EXPIRACION_MINUTOS: 5,
  OTP_MAX_INTENTOS: 3,
} as const;

// Formatos de Fecha
export const FORMATOS_FECHA = {
  ISO: 'yyyy-MM-dd',
  ISO_DATETIME: "yyyy-MM-dd'T'HH:mm:ss",
  DISPLAY: 'dd/MM/yyyy',
  DISPLAY_DATETIME: 'dd/MM/yyyy HH:mm',
} as const;

// Mensajes de Error Comunes
export const MENSAJES_ERROR = {
  REQUIRED: 'Este campo es requerido',
  INVALID_CEDULA: 'La cédula debe tener exactamente 10 dígitos',
  INVALID_TELEFONO: 'El teléfono debe tener exactamente 10 dígitos',
  INVALID_OTP: 'El código OTP debe tener 6 dígitos',
  PASSWORD_MISMATCH: 'Las contraseñas no coinciden',
  PASSWORD_MIN: `La contraseña debe tener al menos ${VALIDACIONES.PASSWORD_MIN_LENGTH} caracteres`,
  NETWORK_ERROR: 'Error de conexión. Verifique su conexión a internet.',
  UNAUTHORIZED: 'No tiene permisos para realizar esta acción',
  SESSION_EXPIRED: 'Su sesión ha expirado. Por favor, inicie sesión nuevamente.',
} as const;

// Colores (para uso programático)
export const COLORES = {
  CORAL_ROJO: '#E46B6B',
  AZUL_PETROLEO: '#1F4E5F',
  VERDE_SUAVE: '#6BBF8E',
  BLANCO: '#FFFFFF',
  ROJO_INTENSO: '#F21D1D',
  GRIS_CLARO: '#F2F2F2',
  NEGRO: '#404040',
  VERDE_CLARO: '#CEF2D0',
} as const;
