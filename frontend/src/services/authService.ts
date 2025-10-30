/**
 * Servicio de Autenticación
 * Maneja las llamadas al API de autenticación
 * Basado en: API_AuthController.md
 */

import { api } from '@/lib/axios';
import type { 
  LoginRequest,
  LoginResponse,
  RegistroRequest, 
  CambiarContraseniaRequest,
  JwtResponse,
  ApiResponse,
  BackendResponse,
  Usuario 
} from '@/types';

/**
 * POST /auth/login
 * Inicia sesión con cédula y contraseña
 */
export const login = async (credentials: LoginRequest): Promise<BackendResponse<LoginResponse>> => {
  const response = await api.post<BackendResponse<LoginResponse>>('/auth/login', credentials);
  return response.data;
};

/**
 * POST /auth/registro
 * Registra un nuevo usuario (rol CLI)
 */
export const registro = async (data: RegistroRequest): Promise<ApiResponse<Usuario>> => {
  const response = await api.post<ApiResponse<Usuario>>('/auth/registro', data);
  return response.data;
};

/**
 * PUT /auth/cambiar-contrasenia
 * Cambia la contraseña del usuario autenticado
 * Requiere: Token JWT
 */
export const cambiarContrasenia = async (
  data: CambiarContraseniaRequest
): Promise<ApiResponse<string>> => {
  const response = await api.put<ApiResponse<string>>('/auth/cambiar-contrasenia', data);
  return response.data;
};

/**
 * POST /auth/crear-administrador
 * Crea un nuevo administrador (solo SAD)
 * Requiere: Token JWT con rol SAD
 */
export const crearAdministrador = async (data: RegistroRequest): Promise<ApiResponse<Usuario>> => {
  const response = await api.post<ApiResponse<Usuario>>('/auth/crear-administrador', data);
  return response.data;
};

export default {
  login,
  registro,
  cambiarContrasenia,
  crearAdministrador,
};
