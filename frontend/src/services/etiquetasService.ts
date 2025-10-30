/**
 * Servicio de Etiquetas
 * Maneja la obtención de datos de etiquetas y descarga de PDFs
 */

import apiClient from '@/lib/axios';

// ===========================
// INTERFACES
// ===========================

/**
 * Etiqueta individual de una solicitud
 */
export interface Etiqueta {
	idSolicitud: number;
	idPedido: number;
	nombreCliente: string;
	direccion: string;
	telefono: string;
	numeroEtiqueta: number;
	totalEtiquetas: number;
}

/**
 * Respuesta del endpoint de etiquetas con todas las etiquetas de un pedido
 */
export interface EtiquetasPedidoResponse {
	idPedido: number;
	fechaEntrega: string;
	etiquetas: Etiqueta[];
	totalEtiquetas: number;
}

/**
 * Pedido para selección en la tabla
 */
export interface PedidoEtiqueta {
	idPedido: number;
	idAdmin: number;
	estadoPedido: string;
	fechaCreado: string;
	fechaCierre: string;
	urlHash?: string;
	productos?: any[];
}

// ===========================
// FUNCIONES DEL SERVICIO
// ===========================

/**
 * Obtener lista de pedidos filtrados por estado ENT (Entregado)
 * Solo los pedidos entregados pueden tener etiquetas
 */
export const obtenerPedidos = async (): Promise<PedidoEtiqueta[]> => {
	try {
		const response = await apiClient.get('/pedidos', {
			params: {
				estado: 'ENT'
			}
		});

		// Asegurar que siempre retornemos un array
		if (Array.isArray(response.data)) {
			return response.data;
		}

		// Si la respuesta tiene una propiedad que contiene el array
		if (response.data && Array.isArray(response.data.pedidos)) {
			return response.data.pedidos;
		}

		// Si la respuesta tiene data dentro de data
		if (response.data && Array.isArray(response.data.data)) {
			return response.data.data;
		}

		console.warn('⚠️ Respuesta inesperada del servidor:', response.data);
		return [];
	} catch (error) {
		
		return [];
	}
};

/**
 * Obtener vista previa de etiquetas de un pedido (JSON)
 */
export const obtenerEtiquetasPedido = async (
	idPedido: number
): Promise<EtiquetasPedidoResponse> => {
	const response = await apiClient.get(
		`/etiquetas/pedido/${idPedido}/json`
	);

	

	return response.data;
};

/**
 * Descargar PDF de etiquetas de un pedido
 */
export const descargarEtiquetasPDF = async (
	idPedido: number,
	nombreArchivo?: string
): Promise<void> => {
	try {
		const response = await apiClient.get(
			`/etiquetas/pedido/${idPedido}/pdf`,
			{
				responseType: 'blob',
			}
		);

		// Verificar si la respuesta es un error (JSON) en lugar de un PDF
		if (response.data.type === 'application/json') {
			const text = await response.data.text();
			const errorData = JSON.parse(text);

			// Lanzar error con el mensaje del backend
			throw new Error(
				errorData.message ||
					errorData.error ||
					errorData.details ||
					'Error al generar el PDF de etiquetas'
			);
		}

		// Crear blob y descargar
		const blob = new Blob([response.data], { type: 'application/pdf' });
		const url = window.URL.createObjectURL(blob);
		const link = document.createElement('a');
		link.href = url;

		// Nombre del archivo: usar el proporcionado o generar uno por defecto
		const fileName = nombreArchivo
			? `${nombreArchivo}.pdf`
			: `Etiquetas_Pedido_${idPedido}.pdf`;

		link.setAttribute('download', fileName);
		document.body.appendChild(link);
		link.click();

		// Limpiar
		link.remove();
		window.URL.revokeObjectURL(url);

		
	} catch (error: any) {
		

		// Propagar el error con mensaje claro
		if (error.response?.data) {
			// Si hay respuesta del servidor con datos
			if (error.response.data.type === 'application/json') {
				const text = await error.response.data.text();
				const errorData = JSON.parse(text);
				throw new Error(
					errorData.message ||
						errorData.error ||
						errorData.details ||
						'Error al generar el PDF de etiquetas'
				);
			}
		}

		throw error;
	}
};

// ===========================
// EXPORT DEFAULT
// ===========================

const etiquetasService = {
	obtenerPedidos,
	obtenerEtiquetasPedido,
	descargarEtiquetasPDF,
};

export default etiquetasService;
