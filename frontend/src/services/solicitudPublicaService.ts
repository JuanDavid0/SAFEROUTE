/**
 * Servicio de Solicitudes Públicas
 * Maneja las solicitudes de clientes a través del hash del pedido
 */

import axios from 'axios';
import apiClient from '@/lib/axios';
import type { SolicitudProductoDTO } from '@/types';

// ===========================
// INTERFACES
// ===========================

/**
 * Información del pedido disponible público
 */
export interface PedidoDisponibleResponse {
	status: string;
	message: string;
	data: {
		idPedido: number;
		idAdmin: number;
		estadoPedido: string;
		fechaCreado: string;
		fechaCierre: string;
		urlHash: string;
		productos: Array<{
			idProducto: number;
			cantidadMin: number;
			cantidadMax: number;
		}>;
	};
	timestamp: string;
}

/**
 * Request para crear solicitud pública (cliente nuevo)
 */
export interface CrearSolicitudPublicaRequest {
	nombres: string;
	apellidos: string;
	telefono: string;
	cedula: string;
	direccion: string;
	idPedido: number;
	productos: SolicitudProductoDTO[];
}

/**
 * Response al crear solicitud pública
 */
export interface CrearSolicitudPublicaResponse {
	exito: boolean;
	mensaje: string;
	datos: {
		idSolicitud: number;
		idCliente: number;
		nombreCliente: string;
		idPedido: number;
		estadoSolicitud: string;
		direccionEntrega: string;
		fechaSolicitud: string;
		fechaLimitePago: string;
		modificacionesRestantes: number;
		productos: Array<{
			idProducto: number;
			nombreProducto: string;
			cantidadSolicitada: number;
			precio: number;
		}>;
	};
	timestamp: string;
}

// ===========================
// FUNCIONES DEL SERVICIO
// ===========================

/**
 * Obtener información del pedido disponible por hash
 */
const obtenerPedidoPorHash = async (
	hash: string
): Promise<PedidoDisponibleResponse> => {
	try {
		

		const response = await apiClient.get(
			`/pedidos/pedido-disponible/${hash}`
		);

		

		return response.data;
	} catch (error: unknown) {
		

		if (axios.isAxiosError(error)) {
			const response = error.response;
			if (response?.data?.message) {
				throw new Error(response.data.message);
			} else if (response?.data?.error) {
				throw new Error(response.data.error);
			}
		}

		throw new Error('Error al obtener información del pedido');
	}
};

/**
 * Crear solicitud pública (cliente nuevo sin registro)
 */
const crearSolicitudPublica = async (
	datos: CrearSolicitudPublicaRequest
): Promise<CrearSolicitudPublicaResponse> => {
	try {
		

		const response = await apiClient.post('/solicitudes/public/nueva', datos);

		

		return response.data;
	} catch (error: unknown) {
		

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

		throw new Error('Error al crear la solicitud. Por favor, intenta nuevamente.');
	}
};

// ===========================
// EXPORT DEFAULT
// ===========================

const solicitudPublicaService = {
	obtenerPedidoPorHash,
	crearSolicitudPublica,
};

export default solicitudPublicaService;
