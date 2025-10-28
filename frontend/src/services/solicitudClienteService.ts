/**
 * Servicio para gestionar las solicitudes de clientes autenticados con OTP
 * Permite listar, modificar, cancelar y gestionar productos de solicitudes
 */

import { api } from '@/lib/axios';

// ============================
// INTERFACES
// ============================

export interface ProductoSolicitud {
  idProducto: number;
  nombreProducto: string | null;
  cantidadSolicitada: number;
  precio: number;
}

export interface SolicitudCliente {
  idSolicitud: number;
  idCliente: number;
  nombreCliente: string;
  idPedido: number;
  estadoSolicitud: string;
  direccionEntrega: string;
  fechaSolicitud: string;
  fechaLimitePago: string;
  modificacionesRestantes: number;
  productos: ProductoSolicitud[];
}

export interface MisSolicitudesResponse {
  status: string;
  message: string;
  data: SolicitudCliente[];
  timestamp: string;
}

export interface AgregarProductoRequest {
  idProducto: number;
  cantidadSolicitada: number;
}

export interface ModificarSolicitudRequest {
  direccionEntrega: string;
}

export interface ActualizarCantidadRequest {
  cantidad: number;
}

export interface OperacionResponse {
  status: string;
  message: string;
  data?: any;
  timestamp: string;
}

// ============================
// FUNCIONES
// ============================

/**
 * Obtiene todas las solicitudes del cliente para un pedido específico
 * @param hash - Hash del pedido
 * @param tokenOtp - Token de autorización OTP
 * @returns Lista de solicitudes del cliente
 */
export const obtenerMisSolicitudes = async (
  hash: string,
  tokenOtp: string
): Promise<MisSolicitudesResponse> => {
  try {
    const response = await api.get<MisSolicitudesResponse>(
      `/solicitudes/mis-solicitudes/${hash}`,
      {
        headers: {
          Authorization: `Bearer ${tokenOtp}`,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al obtener solicitudes',
      data: [],
    };
  }
};

/**
 * Agrega un producto a una solicitud existente
 * @param idSolicitud - ID de la solicitud
 * @param datos - Producto y cantidad a agregar
 * @param tokenOtp - Token de autorización OTP
 */
export const agregarProductoASolicitud = async (
  idSolicitud: number,
  datos: AgregarProductoRequest,
  tokenOtp: string
): Promise<OperacionResponse> => {
  try {
    const response = await api.post<OperacionResponse>(
      `/solicitudes/${idSolicitud}/productos`,
      datos,
      {
        headers: {
          Authorization: `Bearer ${tokenOtp}`,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al agregar producto',
    };
  }
};

/**
 * Modifica la dirección de entrega de una solicitud
 * @param idSolicitud - ID de la solicitud
 * @param datos - Nueva dirección de entrega
 * @param tokenOtp - Token de autorización OTP
 */
export const modificarSolicitud = async (
  idSolicitud: number,
  datos: ModificarSolicitudRequest,
  tokenOtp: string
): Promise<OperacionResponse> => {
  try {
    const response = await api.put<OperacionResponse>(
      `/solicitudes/${idSolicitud}/modificar`,
      datos,
      {
        headers: {
          Authorization: `Bearer ${tokenOtp}`,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al modificar solicitud',
    };
  }
};

/**
 * Actualiza la cantidad de un producto en una solicitud
 * @param idSolicitud - ID de la solicitud
 * @param idProducto - ID del producto
 * @param datos - Nueva cantidad
 * @param tokenOtp - Token de autorización OTP
 */
export const actualizarCantidadProducto = async (
  idSolicitud: number,
  idProducto: number,
  datos: ActualizarCantidadRequest,
  tokenOtp: string
): Promise<OperacionResponse> => {
  try {
    const response = await api.put<OperacionResponse>(
      `/solicitudes/${idSolicitud}/productos/${idProducto}`,
      datos,
      {
        headers: {
          Authorization: `Bearer ${tokenOtp}`,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al actualizar cantidad',
    };
  }
};

/**
 * Elimina un producto de una solicitud
 * @param idSolicitud - ID de la solicitud
 * @param idProducto - ID del producto a eliminar
 * @param tokenOtp - Token de autorización OTP
 */
export const eliminarProductoDeSolicitud = async (
  idSolicitud: number,
  idProducto: number,
  tokenOtp: string
): Promise<OperacionResponse> => {
  try {
    const response = await api.delete<OperacionResponse>(
      `/solicitudes/${idSolicitud}/productos/${idProducto}`,
      {
        headers: {
          Authorization: `Bearer ${tokenOtp}`,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al eliminar producto',
    };
  }
};

/**
 * Cancela una solicitud completa
 * @param idSolicitud - ID de la solicitud a cancelar
 * @param tokenOtp - Token de autorización OTP
 */
export const cancelarSolicitud = async (
  idSolicitud: number,
  tokenOtp: string
): Promise<OperacionResponse> => {
  try {
    const response = await api.delete<OperacionResponse>(
      `/solicitudes/${idSolicitud}`,
      {
        headers: {
          Authorization: `Bearer ${tokenOtp}`,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response) {
      throw error.response.data;
    }
    throw {
      status: 'error',
      message: 'Error al cancelar solicitud',
    };
  }
};
