/**
 * Tipos y Interfaces Globales para SAFE ROUTE
 * Basados en la documentación del backend
 */

// ============================================
// API Response Genérico
// ============================================

export interface ApiResponse<T = any> {
  exito: boolean;
  mensaje: string;
  datos: T | null;
  timestamp?: string;
}

// Respuesta alternativa del backend (formato status/message/data)
export interface BackendResponse<T = any> {
  status: 'success' | 'fail' | 'error';
  message: string;
  data?: T | null;
  error?: {
    code: string;
    details: string;
    field?: string;
    rejectedValue?: any;
  };
  timestamp?: string;
  path?: string;
}

// ============================================
// Autenticación y Usuarios
// ============================================

export interface Usuario {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  telefono: string;
  cedula: string;
  direccion: string;
  estadoUsuario: string;
  rol: {
    idRol: number;
    nombreRol: 'CLI' | 'ADM' | 'SAD';
  };
}

export interface LoginRequest {
  cedula: string;
  contrasenia: string;
}

export interface LoginResponse {
  token: string;
  rol: 'CLI' | 'ADM' | 'SAD';
  idUsuario: number;
}

export interface RegistroRequest {
  nombres: string;
  apellidos: string;
  telefono: string;
  cedula: string;
  direccion: string;
  contrasenia: string;
}

export interface JwtResponse {
  token: string;
  tipo: string;
  idUsuario: number;
  cedula: string;
  nombres: string;
  apellidos: string;
  rol: string;
}

export interface CambiarContraseniaRequest {
  contraseniaActual: string;
  contraseniaNueva: string;
  confirmarContrasenia: string;
}

// ============================================
// OTP (Código SMS)
// ============================================

export interface OtpSolicitudRequest {
  telefono: string;
}

export interface OtpVerificacionRequest {
  telefono: string;
  codigoOtp: string;
}

export interface OtpResponse {
  tokenOtp: string;
  mensaje: string;
  expiraEn: string;
}

// ============================================
// Productos
// ============================================

export interface Producto {
  idProducto: number;
  nombreProducto: string;
  tipoProducto: string;
  descripcionProducto: string;
  precioUnitario: number;
  costoUnitario: number;
  urlImagen: string | null;
  estadoProducto: string;
}

export interface ProductoDTO {
  idProducto?: number;
  nombreProducto: string;
  tipoProducto: string;
  descripcionProducto: string;
  precioUnitario: number;
  costoUnitario: number;
  urlImagen?: string | null;
  estadoProducto?: string;
}

// ============================================
// Solicitudes
// ============================================

export type EstadoSolicitud = 'PND' | 'PGD' | 'ENT' | 'CAN';

export interface SolicitudProductoDTO {
  idProducto: number;
  nombreProducto?: string;
  cantidadSolicitada: number;
  precioUnitario?: number;
  subtotal?: number;
}

export interface SolicitudDTO {
  idSolicitud?: number;
  idUsuario?: number;
  cedula?: string;
  nombres?: string;
  apellidos?: string;
  telefono?: string;
  direccionEntrega: string;
  estadoSolicitud?: EstadoSolicitud;
  fechaLimitePago: string; // ISO format: YYYY-MM-DD
  modificacionesRestantes?: number;
  productos: SolicitudProductoDTO[];
  totalSolicitud?: number;
  fechaCreacion?: string;
}

export interface SolicitudClienteDTO {
  idSolicitud: number;
  direccionEntrega: string;
  estadoSolicitud: EstadoSolicitud;
  fechaLimitePago: string;
  modificacionesRestantes: number;
  productos: SolicitudProductoDTO[];
  totalSolicitud: number;
  fechaCreacion: string;
}

export interface SolicitudModificacionDTO {
  direccionEntrega?: string;
  fechaLimitePago?: string;
  productos?: SolicitudProductoDTO[];
}

// ============================================
// Pedidos
// ============================================

export type EstadoPedido = 'AGP' | 'CSL' | 'FNL' | 'CRM';

export interface ProductoPedidoDTO {
  idProducto: number;
  nombreProducto: string;
  cantidadMin: number;
  cantidadMax: number;
  cantidadTotal?: number;
  precioUnitario: number;
  costoUnitario: number;
  subtotal?: number;
}

export interface PedidoDTO {
  idPedido?: number;
  idAdmin?: number;
  estadoPedido?: EstadoPedido;
  fechaCreado?: string;
  fechaCierre?: string;
  productos: ProductoPedidoDTO[];
  precioTotal?: number;
  costoTotal?: number;
  gananciaTotal?: number;
  hashPublico?: string;
  urlPublica?: string;
}

// ============================================
// Consolidación
// ============================================

export interface ConsolidacionProductoDTO {
  idProducto: number;
  nombreProducto: string;
  cantidadTotal: number;
  cantidadMin: number;
  cantidadMax: number;
  cumpleMinimo: boolean;
  solicitudesPorProducto: number;
}

export interface ConsolidacionDTO {
  idPedido: number;
  totalSolicitudes: number;
  solicitudesConsolidadas: number;
  productos: ConsolidacionProductoDTO[];
  mensaje: string;
}

// ============================================
// Etiquetas
// ============================================

export interface EtiquetaDTO {
  idSolicitud: number;
  nombreCliente: string;
  direccionEntrega: string;
  telefono: string;
  productos: string[];
  totalSolicitud: number;
}

export interface EtiquetasResponseDTO {
  idPedido: number;
  totalEtiquetas: number;
  etiquetas: EtiquetaDTO[];
}

// ============================================
// Reportes
// ============================================

export interface ReporteIngresosDTO {
  periodo: string;
  totalIngresos: number;
  totalCostos: number;
  ganancia: number;
  porcentajeGanancia: number;
}

export interface ProductoVendidoDTO {
  idProducto: number;
  nombreProducto: string;
  cantidadVendida: number;
  ingresoTotal: number;
}

export interface ClienteFrecuenteDTO {
  idUsuario: number;
  nombreCompleto: string;
  telefono: string;
  totalSolicitudes: number;
  totalGastado: number;
}

export interface EstadisticasPedidoDTO {
  totalPedidos: number;
  pedidosActivos: number;
  pedidosConsolidados: number;
  pedidosFinalizados: number;
  pedidosConRemision: number;
}

// ============================================
// Histórico
// ============================================

export interface HistorialPedidoDTO {
  idPedido: number;
  idAdmin: number;
  estadoPedido: EstadoPedido;
  fechaCreado: string;
  fechaCierre: string | null;
  solicitudes: SolicitudDTO[];
  totalSolicitudes: number;
  precioTotal: number;
}

// ============================================
// Logs de Auditoría
// ============================================

export interface LogDTO {
  idLog: number;
  idUsuario: number;
  nombreUsuario: string;
  accion: string;
  fechaLog: string; // ISO DateTime
}

// ============================================
// Cancelación
// ============================================

export interface SolicitudCanceladaDTO {
  idSolicitud: number;
  cedula: string;
  nombreCliente: string;
  telefono: string;
  totalSolicitud: number;
  notificacionEnviada: boolean;
}

export interface CancelacionSolicitudesDTO {
  idPedido: number;
  fechaCierre: string;
  totalSolicitudesCanceladas: number;
  solicitudesCanceladas: SolicitudCanceladaDTO[];
  errores: string[];
  exitoso: boolean;
}

// ============================================
// Logs del Sistema
// ============================================

export interface Log {
  idLog: number;
  idUsuario: number;
  nombreUsuario: string;
  accion: string;
  fechaLog: string;
}

export interface LogsResponse {
  status: 'success' | 'fail' | 'error';
  message: string;
  data: Log[];
  timestamp: string;
}

// ============================================
// Utilidades
// ============================================

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface SelectOption {
  value: string | number;
  label: string;
}

