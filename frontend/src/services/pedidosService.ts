/**
 * Servicio de Pedidos
 * Maneja todas las operaciones de pedidos con el backend
 */

import apiClient from '@/lib/axios';
import { BackendResponse } from '@/types';

// Interfaces para pedidos
export interface ProductoPedidoRequest {
    idProducto: number;
    cantidadMin: number;
    cantidadMax?: number;
}

export interface CrearPedidoRequest {
    productos: ProductoPedidoRequest[];
    fechaCierre: string; // formato: YYYY-MM-DD
}

export interface ProductoPedidoResponse {
    idProducto: number;
    cantidadMin: number;
    cantidadMax: number | null;
    nombreProducto?: string;
    precioUnitario?: number;
}

export interface PedidoResponse {
    idPedido: number;
    idAdmin: number;
    estadoPedido: string;
    fechaCreado: string;
    fechaCierre: string;
    urlHash: string | null;
    productos: ProductoPedidoResponse[];
}

/**
 * Crear un nuevo pedido
 */
export const crearPedido = async (
    idAdmin: number,
    datos: CrearPedidoRequest
): Promise<PedidoResponse> => {
    const response = await apiClient.post<BackendResponse<PedidoResponse>>(
        `/pedidos/${idAdmin}`,
        datos
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al crear pedido');
};

/**
 * Listar todos los pedidos
 */
export const obtenerPedidos = async (): Promise<PedidoResponse[]> => {
    const response = await apiClient.get<BackendResponse<PedidoResponse[]>>('/pedidos');

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    return [];
};

/**
 * Obtener un pedido por ID
 */
export const obtenerPedidoPorId = async (id: number): Promise<PedidoResponse | null> => {
    const response = await apiClient.get<BackendResponse<PedidoResponse>>(`/pedidos/${id}`);

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    return null;
};

/**
 * Actualizar pedido
 */
export const actualizarPedido = async (
    idPedido: number,
    datos: CrearPedidoRequest
): Promise<PedidoResponse> => {
    const response = await apiClient.put<BackendResponse<PedidoResponse>>(
        `/pedidos/${idPedido}`,
        datos
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al actualizar pedido');
};

/**
 * Actualizar estado del pedido
 */
export const actualizarEstadoPedido = async (
    idPedido: number,
    estado: 'CRT' | 'ACT' | 'CRM' | 'CRA' | 'PRD' | 'RCP' | 'RTA' | 'ADU' | 'ENT'
): Promise<PedidoResponse> => {
    const response = await apiClient.put<BackendResponse<PedidoResponse>>(
        `/pedidos/${idPedido}/estado/${estado}`
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al actualizar estado');
};

/**
 * Actualizar fecha de cierre del pedido (solo para pedidos activos)
 */
export const actualizarFechaCierre = async (
    idPedido: number,
    fechaCierre: string // formato: YYYY-MM-DD
): Promise<PedidoResponse> => {
    const response = await apiClient.put<BackendResponse<PedidoResponse>>(
        `/pedidos/${idPedido}`,
        { fechaCierre }
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al actualizar fecha de cierre');
};

/**
 * Cancelar pedido
 */
export const cancelarPedido = async (idPedido: number): Promise<void> => {
    const response = await apiClient.delete<BackendResponse<null>>(
        `/pedidos/${idPedido}`
    );

    if (response.data.status !== 'success') {
        throw new Error(response.data.message || 'Error al cancelar pedido');
    }
};

/**
 * Agregar producto al pedido
 */
export const agregarProductoAlPedido = async (
    idPedido: number,
    producto: ProductoPedidoRequest
): Promise<PedidoResponse> => {
    const response = await apiClient.post<BackendResponse<PedidoResponse>>(
        `/pedidos/${idPedido}/productos`,
        producto
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al agregar producto');
};

/**
 * Eliminar producto del pedido
 */
export const eliminarProductoDelPedido = async (
    idPedido: number,
    idProducto: number
): Promise<PedidoResponse> => {
    const response = await apiClient.delete<BackendResponse<PedidoResponse>>(
        `/pedidos/${idPedido}/productos/${idProducto}`
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al eliminar producto');
};

/**
 * Modificar cantidades de un producto del pedido
 */
export const modificarProductoDelPedido = async (
    idPedido: number,
    idProducto: number,
    producto: Partial<ProductoPedidoRequest>
): Promise<PedidoResponse> => {
    const response = await apiClient.put<BackendResponse<PedidoResponse>>(
        `/pedidos/${idPedido}/productos/${idProducto}`,
        producto
    );

    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    throw new Error(response.data.message || 'Error al modificar producto');
};

/**
 * Cancelar solicitudes vencidas de un pedido
 */
export const cancelarSolicitudesVencidas = async (idPedido: number): Promise<void> => {
    const response = await apiClient.post<BackendResponse<null>>(
        `/cancelar-solicitudes/cancelar-vencidas/${idPedido}`
    );

    if (response.data.status !== 'success') {
        throw new Error(response.data.message || 'Error al cancelar solicitudes vencidas');
    }
};

/**
 * Consolidar pedido (cambiar a estado RTA las solicitudes pagadas)
 */
export const consolidarPedido = async (idPedido: number): Promise<void> => {
    const response = await apiClient.post<BackendResponse<null>>(
        `/consolidacion/pedido/${idPedido}`
    );

    if (response.data.status !== 'success') {
        throw new Error(response.data.message || 'Error al consolidar pedido');
    }
};
