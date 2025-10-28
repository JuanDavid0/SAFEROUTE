/**
 * Servicio de Informes Contables
 * Maneja la generación y descarga de informes en Excel
 */

import apiClient from '@/lib/axios';

// ===========================
// INTERFACES
// ===========================

/**
 * Usuario/Cliente para el informe
 */
export interface UsuarioInforme {
	idUsuario: number;
	cedula: string;
	nombres: string; // Cambié de 'nombre' a 'nombres'
	apellidos: string; // Cambié de 'apellido' a 'apellidos'
	nombreCompleto?: string;
	telefono?: string;
	direccion?: string;
	roles?: string[];
}

/**
 * Pedido para el informe
 */
export interface PedidoInforme {
	idPedido: number;
	idAdmin: number;
	estadoPedido: string; // Cambié de 'estado' a 'estadoPedido'
	fechaCreado: string; // Cambié de 'fechaPedido' a 'fechaCreado'
	fechaCierre: string;
	urlHash?: string;
	productos?: any[];
}

// ===========================
// FUNCIONES DEL SERVICIO
// ===========================

/**
 * Obtener lista de todos los usuarios/clientes
 */
export const obtenerUsuarios = async (): Promise<UsuarioInforme[]> => {
	const response = await apiClient.get('/usuarios');

	console.log('👥 Respuesta usuarios:', response.data);

	// Si la respuesta tiene el formato BackendResponse
	if (response.data.status === 'success' && response.data.data) {
		return response.data.data;
	}

	// Si la respuesta es directamente un array
	if (Array.isArray(response.data)) {
		return response.data;
	}

	throw new Error('Error al obtener usuarios');
};

/**
 * Obtener lista de pedidos entregados (estado ENT)
 */
export const obtenerPedidosEntregados = async (): Promise<PedidoInforme[]> => {
	const response = await apiClient.get('/pedidos');

	console.log('📦 Respuesta pedidos:', response.data);

	// Si la respuesta tiene el formato BackendResponse
	if (response.data.status === 'success' && response.data.data) {
		return response.data.data;
	}

	// Si la respuesta es directamente un array
	if (Array.isArray(response.data)) {
		return response.data;
	}

	throw new Error('Error al obtener pedidos');
};

/**
 * Descargar informe de cliente en Excel
 */
export const descargarInformeCliente = async (
	idCliente: number,
	nombreCliente: string
) => {
	try {
		const response = await apiClient.get(`/excel/cliente/${idCliente}`, {
			responseType: 'blob', // Importante para archivos binarios
		});

		console.log('📥 Descargando informe de cliente:', idCliente);

		// Crear un blob con el contenido
		const blob = new Blob([response.data], {
			type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
		});

		// Crear URL temporal
		const url = window.URL.createObjectURL(blob);

		// Crear elemento <a> temporal para descargar
		const link = document.createElement('a');
		link.href = url;
		link.download = `Informe_Cliente_${nombreCliente.replace(/\s+/g, '_')}_${idCliente}.xlsx`;
		document.body.appendChild(link);
		link.click();

		// Limpiar
		document.body.removeChild(link);
		window.URL.revokeObjectURL(url);

		console.log('✅ Informe descargado exitosamente');
	} catch (error: any) {
		console.error('❌ Error al descargar informe de cliente:', error);

		// Intentar extraer el mensaje de error del backend si viene en formato JSON
		if (error.response?.data instanceof Blob) {
			try {
				// Convertir el Blob a texto
				const errorText = await error.response.data.text();
				const errorJson = JSON.parse(errorText);

				console.log('📋 Error parseado del backend:', errorJson);

				// Extraer el mensaje específico del backend
				if (errorJson.error?.details) {
					throw new Error(errorJson.error.details);
				} else if (errorJson.message) {
					throw new Error(errorJson.message);
				}
			} catch (parseError) {
				// Si no se puede parsear, usar el mensaje original
				console.warn('⚠️ No se pudo parsear el error del backend:', parseError);
			}
		}

		// Mensaje de error por defecto
		throw new Error(
			error.message || 'Error al descargar informe de cliente'
		);
	}
};

/**
 * Descargar informe de pedido en Excel
 */
export const descargarInformePedido = async (idPedido: number) => {
	try {
		const response = await apiClient.get(`/excel/pedido/${idPedido}`, {
			responseType: 'blob', // Importante para archivos binarios
		});

		console.log('📥 Descargando informe de pedido:', idPedido);

		// Crear un blob con el contenido
		const blob = new Blob([response.data], {
			type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
		});

		// Crear URL temporal
		const url = window.URL.createObjectURL(blob);

		// Crear elemento <a> temporal para descargar
		const link = document.createElement('a');
		link.href = url;
		link.download = `Informe_Pedido_${idPedido}.xlsx`;
		document.body.appendChild(link);
		link.click();

		// Limpiar
		document.body.removeChild(link);
		window.URL.revokeObjectURL(url);

		console.log('✅ Informe descargado exitosamente');
	} catch (error: any) {
		console.error('❌ Error al descargar informe de pedido:', error);

		// Intentar extraer el mensaje de error del backend si viene en formato JSON
		if (error.response?.data instanceof Blob) {
			try {
				// Convertir el Blob a texto
				const errorText = await error.response.data.text();
				const errorJson = JSON.parse(errorText);

				console.log('📋 Error parseado del backend:', errorJson);

				// Extraer el mensaje específico del backend
				if (errorJson.error?.details) {
					throw new Error(errorJson.error.details);
				} else if (errorJson.message) {
					throw new Error(errorJson.message);
				}
			} catch (parseError) {
				// Si no se puede parsear, usar el mensaje original
				console.warn('⚠️ No se pudo parsear el error del backend:', parseError);
			}
		}

		// Mensaje de error por defecto
		throw new Error(
			error.message || 'Error al descargar informe de pedido'
		);
	}
};
