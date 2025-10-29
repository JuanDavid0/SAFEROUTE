/**
 * Servicio de Productos
 * Maneja todas las operaciones CRUD de productos con el backend
 */

import apiClient from '@/lib/axios';
import axios from 'axios';
import { BackendResponse, Producto } from '@/types';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

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
 * Obtener un producto por ID (requiere autenticación)
 */
export const obtenerProductoPorId = async (id: number): Promise<Producto | null> => {
    const response = await apiClient.get<BackendResponse<Producto>>(`/productos/${id}`);
    
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }
    
    return null;
};

/**
 * Obtener un producto por ID (versión pública - SIN autenticación)
 * Usar desde rutas públicas como /pedido/[hash]
 */
export const obtenerProductoPorIdPublico = async (id: number): Promise<Producto | null> => {
    try {
        // Crear instancia de axios SIN interceptores que añadan token
        const response = await axios.get<BackendResponse<Producto>>(
            `${BASE_URL}/productos/${id}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                },
                timeout: 30000,
            }
        );
        
        if (response.data.status === 'success' && response.data.data) {
            return response.data.data;
        }
        
        return null;
    } catch (error) {
        return null;
    }
};

/**
 * Obtener múltiples productos por IDs (versión pública - SIN autenticación)
 * Usar desde rutas públicas como /pedido/[hash]/editar-solicitud
 */
export const obtenerProductosPorIdsPublico = async (ids: number[]): Promise<Producto[]> => {
    try {
        const productos = await Promise.all(
            ids.map(id => obtenerProductoPorIdPublico(id))
        );
        
        // Filtrar nulls
        return productos.filter((p): p is Producto => p !== null);
    } catch (error) {
        return [];
    }
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
