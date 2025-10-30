/**
 * Servicio de Configuración
 * Maneja el cambio de contraseña y otras configuraciones del usuario
 */

import axios from 'axios';
import apiClient from '@/lib/axios';

// ===========================
// INTERFACES
// ===========================

/**
 * Request para cambio de contraseña
 */
export interface CambiarContraseniaRequest {
	cedula: string;
	contraseniaActual: string;
	contraseniaNueva: string;
}

/**
 * Respuesta del cambio de contraseña
 */
export interface CambiarContraseniaResponse {
	status: string;
	message: string;
	timestamp: string;
}

// ===========================
// FUNCIONES DEL SERVICIO
// ===========================

/**
 * Cambiar contraseña del usuario
 */
export const cambiarContrasenia = async (
	datos: CambiarContraseniaRequest
): Promise<CambiarContraseniaResponse> => {
	try {
		

		const response = await apiClient.post(
			'/auth/cambiar-contrasenia',
			datos
		);

		

		return response.data;
	} catch (error: unknown) {
		

		// Propagar el error con mensaje claro
		if (axios.isAxiosError(error)) {
			const response = error.response;
			if (response?.data?.message) {
				throw new Error(response.data.message);
			} else if (response?.data?.error) {
				throw new Error(response.data.error);
			} else if (response?.data?.details) {
				throw new Error(response.data.details);
			}
		}

		throw new Error('Error al cambiar la contraseña. Por favor, intenta nuevamente.');
	}
};

// ===========================
// EXPORT DEFAULT
// ===========================

const configuracionService = {
	cambiarContrasenia,
};

export default configuracionService;
