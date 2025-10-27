/**
 * Funciones de Validación Personalizadas
 * Basadas en las validaciones del backend
 */

import { VALIDACIONES } from './constants';

/**
 * Valida que la cédula tenga exactamente 10 dígitos
 */
export const validarCedula = (cedula: string): boolean => {
  const regex = /^\d{10}$/;
  return regex.test(cedula);
};

/**
 * Valida que el teléfono tenga exactamente 10 dígitos
 */
export const validarTelefono = (telefono: string): boolean => {
  const regex = /^\d{10}$/;
  return regex.test(telefono);
};

/**
 * Valida que el código OTP tenga exactamente 6 dígitos
 */
export const validarOTP = (otp: string): boolean => {
  const regex = /^\d{6}$/;
  return regex.test(otp);
};

/**
 * Valida la contraseña
 */
export const validarContrasenia = (contrasenia: string): boolean => {
  return contrasenia.length >= VALIDACIONES.PASSWORD_MIN_LENGTH;
};

/**
 * Valida que dos contraseñas coincidan
 */
export const validarCoincidenciaContrasenia = (
  contrasenia: string,
  confirmacion: string
): boolean => {
  return contrasenia === confirmacion;
};

/**
 * Valida formato de email (por si se necesita en el futuro)
 */
export const validarEmail = (email: string): boolean => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
};

/**
 * Valida que un número sea positivo
 */
export const validarNumeroPositivo = (numero: number): boolean => {
  return numero > 0;
};

/**
 * Valida formato de fecha ISO (YYYY-MM-DD)
 */
export const validarFechaISO = (fecha: string): boolean => {
  const regex = /^\d{4}-\d{2}-\d{2}$/;
  if (!regex.test(fecha)) return false;
  
  const date = new Date(fecha);
  return !isNaN(date.getTime());
};

/**
 * Valida que una fecha sea futura
 */
export const validarFechaFutura = (fecha: string): boolean => {
  const date = new Date(fecha);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  return date >= today;
};

/**
 * Sanitiza input para prevenir XSS
 */
export const sanitizarInput = (input: string): string => {
  return input
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;');
};

/**
 * Formatea número a moneda colombiana
 */
export const formatearMoneda = (valor: number): string => {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(valor);
};

/**
 * Formatea fecha de ISO a formato legible
 */
export const formatearFecha = (fecha: string): string => {
  const date = new Date(fecha);
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }).format(date);
};

/**
 * Formatea fecha y hora
 */
export const formatearFechaHora = (fecha: string): string => {
  const date = new Date(fecha);
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

/**
 * Trunca texto con elipsis
 */
export const truncarTexto = (texto: string, maxLength: number): string => {
  if (texto.length <= maxLength) return texto;
  return texto.substring(0, maxLength) + '...';
};
