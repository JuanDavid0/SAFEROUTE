/**
 * Utilidades para manejo de errores del backend
 * Extrae mensajes y detalles de las respuestas de error
 */

import { BackendResponse } from '@/types';
import { AxiosError } from 'axios';

interface ErrorInfo {
    message: string;
    details?: string;
    errorCode?: string;
    validationErrors?: Record<string, string>;
}

/**
 * Extrae información detallada de errores del backend
 */
export const extractErrorInfo = (error: any): ErrorInfo => {
    // Si es un error de Axios
    if (error.response) {
        const data: BackendResponse = error.response.data;

        // Caso 1: Error con formato BackendResponse
        if (data && typeof data === 'object') {
            const errorInfo: ErrorInfo = {
                message: data.message || 'Error en la operación',
                details: data.error?.details,
                errorCode: data.error?.code
            };

            // Caso 2: Error de validación con errores por campo
            if (data.status === 'fail' && data.data && typeof data.data === 'object') {
                errorInfo.validationErrors = data.data as Record<string, string>;
            }

            return errorInfo;
        }
    }

    // Caso 3: Error de red o timeout
    if (error.code === 'ERR_NETWORK') {
        return {
            message: 'Error de conexión',
            details: 'No se pudo conectar con el servidor. Verifica tu conexión a internet.',
            errorCode: 'NETWORK_ERROR'
        };
    }

    if (error.code === 'ECONNABORTED') {
        return {
            message: 'Tiempo de espera agotado',
            details: 'La operación tardó demasiado. Por favor, intenta nuevamente.',
            errorCode: 'TIMEOUT_ERROR'
        };
    }

    // Caso 4: Error genérico
    return {
        message: error.message || 'Error desconocido',
        details: 'Ha ocurrido un error inesperado. Por favor, intenta nuevamente.',
        errorCode: 'UNKNOWN_ERROR'
    };
};

/**
 * Extrae el mensaje de éxito del backend
 */
export const extractSuccessMessage = (response: BackendResponse): string => {
    return response.message || 'Operación exitosa';
};

/**
 * Formatea errores de validación para mostrar en lista
 */
export const formatValidationErrors = (errors: Record<string, string>): string => {
    return Object.entries(errors)
        .map(([field, message]) => `• ${field}: ${message}`)
        .join('\n');
};

/**
 * Determina si un error es de validación
 */
export const isValidationError = (error: any): boolean => {
    if (!error.response?.data) return false;
    
    const data: BackendResponse = error.response.data;
    return data.status === 'fail' && 
           data.error?.code === 'VALIDATION_ERROR' &&
           data.data !== null &&
           typeof data.data === 'object';
};

/**
 * Determina si un error es de negocio
 */
export const isBusinessError = (error: any): boolean => {
    if (!error.response?.data) return false;
    
    const data: BackendResponse = error.response.data;
    return data.status === 'fail' && 
           (data.error?.code?.includes('BUSINESS_ERROR') ?? false);
};

/**
 * Determina si un error es del sistema
 */
export const isSystemError = (error: any): boolean => {
    if (!error.response?.data) return false;
    
    const data: BackendResponse = error.response.data;
    return data.status === 'error';
};
