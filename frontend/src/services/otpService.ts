/**
 * Servicio para la autenticación OTP de clientes
 * Permite solicitar y verificar códigos OTP para acceder a solicitudes
 */

import { api } from '@/lib/axios';

// ============================
// INTERFACES
// ============================

export interface SolicitarOtpRequest {
  cedula: string;
}

export interface SolicitarOtpResponse {
  status: string;
  message: string;
  data: {
    exito: boolean;
    mensaje: string;
    token: string | null;
  };
  timestamp: string;
}

export interface VerificarOtpRequest {
  cedula: string;
  codigoOtp: string;
}

export interface VerificarOtpResponse {
  status: string;
  message: string;
  data: {
    exito: boolean;
    mensaje: string;
    token: string;
  };
  timestamp: string;
}

export interface OtpErrorResponse {
  status: string;
  message: string;
  error: {
    code: string;
    details: string;
  };
  timestamp: string;
  path: string;
}

// ============================
// FUNCIONES
// ============================

/**
 * Solicita un código OTP para la cédula especificada
 * El código es enviado al teléfono registrado del cliente
 * @param cedula - Cédula del cliente (8-10 dígitos)
 * @returns Respuesta con confirmación del envío
 */
export const solicitarOtp = async (
  cedula: string
): Promise<SolicitarOtpResponse> => {
  try {
    const response = await api.post<SolicitarOtpResponse>(
      '/auth/otp/solicitar',
      { cedula }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al solicitar código OTP',
      data: { exito: false, mensaje: 'Error de conexión', token: null },
    };
  }
};

/**
 * Verifica el código OTP ingresado por el cliente
 * Si es correcto, devuelve un token de autorización temporal
 * @param cedula - Cédula del cliente
 * @param codigoOtp - Código de 6 dígitos enviado al teléfono
 * @returns Token OTP para autenticar solicitudes
 */
export const verificarOtp = async (
  cedula: string,
  codigoOtp: string
): Promise<VerificarOtpResponse> => {
  try {
    const response = await api.post<VerificarOtpResponse>(
      '/auth/otp/verificar',
      { cedula, codigoOtp }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al verificar código OTP',
      data: { exito: false, mensaje: 'Error de conexión', token: '' },
    };
  }
};
