/**
 * Servicio de Productos
 * Maneja todas las operaciones CRUD de productos con el backend
 */

import apiClient from '@/lib/axios';
import { BackendResponse, Producto } from '@/types';

/**
 * Listar todos los productos activos
 */
export const obtenerProductos = async (): Promise<Producto[]> => {
    const response = await apiClient.get<BackendResponse<Producto[]>>('/productos');
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    return [];
};

/**
 * Obtener un producto por ID
 */
export const obtenerProductoPorId = async (id: number): Promise<Producto | null> => {
    const response = await apiClient.get<BackendResponse<Producto>>(`/productos/${id}`);
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    return null;
};

/**
 * Buscar producto por nombre
 */
export const buscarProductoPorNombre = async (nombre: string): Promise<Producto | null> => {
    const response = await apiClient.get<BackendResponse<Producto>>(
        `/productos/buscar?nombre=${encodeURIComponent(nombre)}`
    );
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    return null;
};

/**
 * Crear producto CON imagen
 */
export const crearProductoConImagen = async (
    productoData: {
        nombreProducto: string;
        tipoProducto: string;
        descripcionProducto: string;
        precioUnitario: number;
        costoUnitario: number;
        estadoProducto?: string;
    },
    imagen: File
): Promise<Producto> => {
    const formData = new FormData();
    
    // Agregar producto como JSON string
    formData.append('producto', JSON.stringify(productoData));
    
    // Agregar imagen
    formData.append('imagen', imagen);
    
    const response = await apiClient.post<BackendResponse<Producto>>(
        '/productos',
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        }
    );
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al crear producto');
};

/**
 * Crear producto SIN imagen
 */
export const crearProductoSinImagen = async (
    productoData: {
        nombreProducto: string;
        tipoProducto: string;
        descripcionProducto: string;
        precioUnitario: number;
        costoUnitario: number;
        estadoProducto?: string;
    }
): Promise<Producto> => {
    const response = await apiClient.post<BackendResponse<Producto>>(
        '/productos/sin-imagen',
        productoData
    );
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al crear producto');
};

/**
 * Actualizar producto CON cambio de imagen
 */
export const actualizarProductoConImagen = async (
    id: number,
    productoData: {
        nombreProducto: string;
        tipoProducto: string;
        descripcionProducto: string;
        precioUnitario: number;
        costoUnitario: number;
        estadoProducto?: string;
    },
    imagen?: File
): Promise<Producto> => {
    const formData = new FormData();
    
    // Agregar producto como JSON string
    formData.append('producto', JSON.stringify(productoData));
    
    // Agregar imagen si existe
    if (imagen) {
        formData.append('imagen', imagen);
    }
    
    const response = await apiClient.put<BackendResponse<Producto>>(
        `/productos/${id}`,
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        }
    );
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al actualizar producto');
};

/**
 * Actualizar producto SIN cambiar imagen
 */
export const actualizarProductoSinImagen = async (
    id: number,
    productoData: {
        nombreProducto: string;
        tipoProducto: string;
        descripcionProducto: string;
        precioUnitario: number;
        costoUnitario: number;
        estadoProducto?: string;
    }
): Promise<Producto> => {
    const response = await apiClient.put<BackendResponse<Producto>>(
        `/productos/${id}/sin-imagen`,
        productoData
    );
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al actualizar producto');
};

/**
 * Eliminar producto (soft delete)
 */
export const eliminarProducto = async (id: number): Promise<void> => {
    const response = await apiClient.delete<BackendResponse<null>>(
        `/productos/${id}`
    );
    
    if (response.data.status !== 'success') {
        throw new Error(response.data.message || 'Error al eliminar producto');
    }
};
