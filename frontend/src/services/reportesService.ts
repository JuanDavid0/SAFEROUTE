/**
 * Servicio de Reportes
 * Maneja todas las operaciones de reportes con el backend
 */

import apiClient from '@/lib/axios';
import { BackendResponse } from '@/types';

// ===========================
// INTERFACES DE REPORTES
// ===========================

/**
 * Productos más vendidos
 */
export interface ProductoMasVendido {
    idProducto: number;
    nombreProducto: string;
    descripcion: string;
    categoria: string;
    cantidadTotalVendida: number;
    ingresosGenerados: number;
    costosAsociados: number;
    gananciaNeta: number;
    numeroPedidos: number;
}

/**
 * Ingresos por periodo
 */
export interface DatoPeriodo {
    periodo: string;
    ingresos: number;
    costos: number;
    ganancia: number;
    cantidadSolicitudes: number;
}

export interface ReporteIngresos {
    fechaInicio: string;
    fechaFin: string;
    agrupacion: string;
    totalIngresos: number;
    totalCostos: number;
    gananciaNeta: number;
    totalSolicitudesPagadas: number;
    datosPorPeriodo: DatoPeriodo[];
}

/**
 * Productos con mayor ganancia
 */
export interface ProductoMayorGanancia {
    idProducto: number;
    nombreProducto: string;
    descripcion: string;
    categoria: string;
    cantidadTotalVendida: number;
    ingresosGenerados: number;
    costosAsociados: number;
    gananciaNeta: number;
    numeroPedidos: number;
}

/**
 * Clientes frecuentes
 */
export interface ClienteFrecuente {
    cedula: string;
    nombreCompleto: string;
    telefono: string;
    totalSolicitudes: number;
    solicitudesPagadas: number;
    montoTotalGastado: number;
    primeraCompra: string;
    ultimaCompra: string;
    promedioGastoPorSolicitud: number;
}

/**
 * Resumen general
 */
export interface ReporteResumen {
    totalPedidos: number;
    pedidosActivos: number;
    pedidosEnCurso: number;
    pedidosEntregados: number;
    pedidosCancelados: number;
    totalSolicitudes: number;
    solicitudesPendientes: number;
    solicitudesConfirmadas: number;
    solicitudesPagadas: number;
    solicitudesRechazadas: number;
    ingresosTotales: number;
    costosTotales: number;
    gananciaNeta: number;
    totalClientes: number;
    clientesActivos: number;
    totalProductos: number;
    productosActivos: number;
}

// ===========================
// FUNCIONES DEL SERVICIO
// ===========================

/**
 * Obtener productos más vendidos
 */
export const obtenerProductosMasVendidos = async (
    limite: number = 10
): Promise<ProductoMasVendido[]> => {
    const response = await apiClient.get(
        `/reportes/productos-mas-vendidos?limite=${limite}`
    );

    // Si la respuesta tiene el formato BackendResponse
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    // Si la respuesta es directamente un array
    if (Array.isArray(response.data)) {
        return response.data;
    }

    throw new Error('Error al obtener productos más vendidos');
};

/**
 * Obtener reporte de ingresos
 */
export const obtenerReporteIngresos = async (
    fechaInicio: string,
    fechaFin: string,
    agrupacion: 'DIARIA' | 'SEMANAL' | 'MENSUAL' | 'TRIMESTRAL' | 'ANUAL' = 'MENSUAL'
): Promise<ReporteIngresos> => {
    const response = await apiClient.get(
        `/reportes/ingresos?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}&agrupacion=${agrupacion}`
    );

    // Si la respuesta tiene el formato BackendResponse
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    // Si la respuesta es directamente el objeto
    if (response.data.fechaInicio || response.data.totalIngresos !== undefined) {
        return response.data;
    }

    throw new Error('Error al obtener reporte de ingresos');
};

/**
 * Obtener productos con mayor ganancia
 */
export const obtenerProductosMayorGanancia = async (): Promise<ProductoMayorGanancia[]> => {
    const response = await apiClient.get(
        `/reportes/productos-mayor-ganancia`
    );

    // Si la respuesta tiene el formato BackendResponse
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    // Si la respuesta es directamente un array
    if (Array.isArray(response.data)) {
        return response.data;
    }

    throw new Error('Error al obtener productos con mayor ganancia');
};

/**
 * Obtener clientes frecuentes
 */
export const obtenerClientesFrecuentes = async (): Promise<ClienteFrecuente[]> => {
    const response = await apiClient.get(
        `/reportes/clientes-frecuentes`
    );


    // Si la respuesta tiene el formato BackendResponse
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    // Si la respuesta es directamente un array
    if (Array.isArray(response.data)) {
        return response.data;
    }

    throw new Error('Error al obtener clientes frecuentes');
};

/**
 * Obtener resumen general
 */
export const obtenerResumenGeneral = async (): Promise<ReporteResumen> => {
    const response = await apiClient.get(
        `/reportes/resumen`
    );

    // Si la respuesta tiene el formato BackendResponse
    if (response.data.status === 'success' && response.data.data) {
        return response.data.data;
    }

    // Si la respuesta es directamente el objeto
    if (response.data.totalPedidos !== undefined || response.data.ingresosTotales !== undefined) {
        return response.data;
    }

    throw new Error('Error al obtener resumen general');
};
