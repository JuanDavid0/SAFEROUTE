/**
 * Servicio de Solicitudes
 * Maneja todas las operaciones de solicitudes con el backend
 */

import apiClient from '@/lib/axios';
import { BackendResponse } from '@/types';

// Interfaces para solicitudes
export interface ProductoSolicitudResponse {
    idProducto: number;
    nombreProducto: string | null;
    cantidadSolicitada: number;
    precio: number;
}

export interface SolicitudResponse {
    idSolicitud: number;
    idCliente: number;
    nombreCliente: string;
    idPedido: number;
    estadoSolicitud: string; // PDP (Pendiente de Pago), PGD (Pagada), CAN (Cancelada)
    direccionEntrega: string;
    fechaSolicitud: string;
    fechaLimitePago: string;
    modificacionesRestantes: number;
    productos: ProductoSolicitudResponse[];
}

/**
 * Obtener solicitudes por pedido y estado opcional
 */
export const obtenerSolicitudesPorPedido = async (
    idPedido: number,
    estado?: string
): Promise<SolicitudResponse[]> => {
    const url = estado 
        ? `/solicitudes/pedido/${idPedido}?estado=${estado}`
        : `/solicitudes/pedido/${idPedido}`;
    
    const response = await apiClient.get<BackendResponse<SolicitudResponse[]>>(url);

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al obtener solicitudes');
};

/**
 * Contar solicitudes pagadas (PGD) de un pedido
 */
export const contarSolicitudesPagadas = async (idPedido: number): Promise<number> => {
    try {
        const solicitudes = await obtenerSolicitudesPorPedido(idPedido, 'PGD');
        return solicitudes.length;
    } catch (error) {
        
        return 0;
    }
};

/**
 * Actualizar estado de una solicitud (PDP -> PGD)
 */
export const actualizarEstadoSolicitud = async (
    idSolicitud: number,
    nuevoEstado: 'PDP' | 'PGD' | 'CAN'
): Promise<SolicitudResponse> => {
    try {
        const response = await apiClient.put<BackendResponse<SolicitudResponse>>(
            `/solicitudes/${idSolicitud}/estado`,
            { nuevoEstado }
        );

        // Verificar si la respuesta es exitosa
        if (response.data.status === 'success') {
            // Si hay data, retornarla; si no, crear objeto básico
            if (response.data.data) {
                return response.data.data;
            }
            
            // Si no hay data pero fue exitoso, retornar objeto mínimo
            return {
                idSolicitud,
                estadoSolicitud: nuevoEstado
            } as SolicitudResponse;
        }

        // Si el status no es success, lanzar error
        throw new Error(response.data.message || 'Error al actualizar estado de solicitud');
    } catch (error: any) {
        // Mejorar el manejo de errores
        if (error.response?.data?.message) {
            throw new Error(error.response.data.message);
        }
        throw new Error(error.message || 'Error al actualizar estado de solicitud');
    }
};
