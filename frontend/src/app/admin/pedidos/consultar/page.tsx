/**
 * Consultar Solicitudes - Administrador
 * Listado de pedidos activos con sus solicitudes
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { Button, useToast } from '@/components/ui';
import * as pedidosService from '@/services/pedidosService';
import * as solicitudesService from '@/services/solicitudesService';
import { obtenerProductosPorIdsPublico } from '@/services/productosService';
import { Producto } from '@/types';
import { extractErrorInfo } from '@/utils/errorHandler';

// Estados de solicitud
type EstadoSolicitud = 'PDP' | 'PGD' | 'CAN';

const NOMBRES_ESTADOS_SOLICITUD: Record<EstadoSolicitud, string> = {
    'PDP': 'Pendiente de Pago',
    'PGD': 'Pagada',
    'CAN': 'Cancelada'
};

interface PedidoConContadores {
    pedido: pedidosService.PedidoResponse;
    totalSolicitudes: number;
    solicitudesPagadas: number;
    solicitudesPendientes: number;
    solicitudesCanceladas: number;
}

export default function ConsultarSolicitudesPage() {
    const { showSuccess, showError } = useToast();
    const [pedidos, setPedidos] = useState<PedidoConContadores[]>([]);
    const [cargando, setCargando] = useState(false);

    // Estado para ver solicitudes de un pedido
    const [pedidoSeleccionado, setPedidoSeleccionado] = useState<number | null>(null);
    const [solicitudes, setSolicitudes] = useState<solicitudesService.SolicitudResponse[]>([]);
    const [filtroEstado, setFiltroEstado] = useState<EstadoSolicitud | 'TODOS'>('TODOS');

    // Estado para ver detalles de una solicitud
    const [solicitudDetalle, setSolicitudDetalle] = useState<solicitudesService.SolicitudResponse | null>(null);

    useEffect(() => {
        cargarPedidos();
    }, []);

    const cargarPedidos = async () => {
        try {
            setCargando(true);
            const data = await pedidosService.obtenerPedidos();

            // Filtrar pedidos activos (ACT y estados posteriores, excepto cerrados)
            const pedidosActivos = data.filter(p =>
                p.estadoPedido !== 'CRT' &&
                p.estadoPedido !== 'CRM' &&
                p.estadoPedido !== 'CRA'
            );

            // Obtener contadores de solicitudes para cada pedido
            const pedidosConInfo = await Promise.all(
                pedidosActivos.map(async (pedido) => {
                    const todasLasSolicitudes = await solicitudesService.obtenerSolicitudesPorPedido(pedido.idPedido);
                    const solicitudesPagadas = todasLasSolicitudes.filter(s => s.estadoSolicitud === 'PGD').length;
                    const solicitudesPendientes = todasLasSolicitudes.filter(s => s.estadoSolicitud === 'PDP').length;
                    const solicitudesCanceladas = todasLasSolicitudes.filter(s => s.estadoSolicitud === 'CAN').length;

                    return {
                        pedido,
                        totalSolicitudes: todasLasSolicitudes.length,
                        solicitudesPagadas,
                        solicitudesPendientes,
                        solicitudesCanceladas
                    };
                })
            );

            setPedidos(pedidosConInfo);
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const cargarSolicitudes = async (idPedido: number) => {
        try {
            setCargando(true);
            const data = await solicitudesService.obtenerSolicitudesPorPedido(idPedido);
            setSolicitudes(data);
            setPedidoSeleccionado(idPedido);
            setFiltroEstado('TODOS');
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const handleVolverAPedidos = () => {
        setPedidoSeleccionado(null);
        setSolicitudes([]);
        setSolicitudDetalle(null);
        setFiltroEstado('TODOS');
    };

    const handleVerDetalles = (solicitud: solicitudesService.SolicitudResponse) => {
        setSolicitudDetalle(solicitud);
    };

    const handleCerrarDetalles = () => {
        setSolicitudDetalle(null);
    };

    const handleMarcarComoPagada = async (idSolicitud: number) => {
        if (!confirm('¿Confirmas que esta solicitud ha sido pagada?')) {
            return;
        }

        try {
            setCargando(true);

            // Actualizar estado de la solicitud
            await solicitudesService.actualizarEstadoSolicitud(idSolicitud, 'PGD');

            // Mostrar mensaje de éxito
            showSuccess(
                'Solicitud marcada como pagada',
                'La solicitud se ha actualizado exitosamente'
            );

            // Recargar las solicitudes del pedido actual primero
            if (pedidoSeleccionado) {
                await cargarSolicitudes(pedidoSeleccionado);
            }

            // Luego recargar los pedidos para actualizar contadores
            await cargarPedidos();

        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    // Filtrar solicitudes según el filtro seleccionado
    const solicitudesFiltradas = filtroEstado === 'TODOS'
        ? solicitudes
        : solicitudes.filter(s => s.estadoSolicitud === filtroEstado);

    // Calcular total de productos de una solicitud
    const calcularTotalSolicitud = (solicitud: solicitudesService.SolicitudResponse): number => {
        return solicitud.productos.reduce((total, prod) => total + prod.precio, 0);
    };

    return (
        <DashboardLayout role="ADM">
            {/* Los mensajes ahora se muestran con el sistema de Toast */}

            {/* Encabezado */}
            <div className="dashboard-page-header">
                <div className="flex items-center gap-4">
                    {pedidoSeleccionado && (
                        <button
                            onClick={handleVolverAPedidos}
                            className="btn-volver"
                            disabled={cargando}
                        >
                            ← Volver
                        </button>
                    )}
                    <h1 className="dashboard-page-title">
                        {pedidoSeleccionado
                            ? `Solicitudes del Pedido #${pedidoSeleccionado}`
                            : 'Consultar Solicitudes'}
                    </h1>
                </div>
            </div>

            <div className="dashboard-content">
                {/* Vista de Pedidos */}
                {!pedidoSeleccionado && (
                    <div className="consultar-solicitudes-container">
                        {cargando ? (
                            <div className="pedidos-empty-state">
                                <p>⏳ Cargando pedidos...</p>
                            </div>
                        ) : pedidos.length === 0 ? (
                            <div className="pedidos-empty-state">
                                <p>No hay pedidos activos con solicitudes</p>
                            </div>
                        ) : (
                            <div className="pedidos-grid">
                                {pedidos.map(({ pedido, totalSolicitudes, solicitudesPagadas, solicitudesPendientes, solicitudesCanceladas }) => (
                                    <div key={pedido.idPedido} className="pedido-card-solicitudes">
                                        <div className="pedido-card-header">
                                            <h3>Pedido #{pedido.idPedido}</h3>
                                            <span className={`badge-estado estado-${pedido.estadoPedido.toLowerCase()}`}>
                                                {pedido.estadoPedido}
                                            </span>
                                        </div>

                                        <div className="pedido-card-body">
                                            <div className="pedido-info-row">
                                                <span className="label">Fecha Cierre:</span>
                                                <span className="value">{pedido.fechaCierre}</span>
                                            </div>

                                            <div className="solicitudes-resumen">
                                                <h4>Solicitudes</h4>
                                                <div className="solicitudes-stats">
                                                    <div className="stat-item">
                                                        <span className="stat-label">Total:</span>
                                                        <span className="stat-value">{totalSolicitudes}</span>
                                                    </div>
                                                    <div className="stat-item stat-pagadas">
                                                        <span className="stat-label">Pagadas:</span>
                                                        <span className="stat-value">{solicitudesPagadas}</span>
                                                    </div>
                                                    <div className="stat-item stat-pendientes">
                                                        <span className="stat-label">Pendientes:</span>
                                                        <span className="stat-value">{solicitudesPendientes}</span>
                                                    </div>
                                                    <div className="stat-item stat-canceladas">
                                                        <span className="stat-label">Canceladas:</span>
                                                        <span className="stat-value">{solicitudesCanceladas}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="pedido-card-footer">
                                            <button
                                                onClick={() => cargarSolicitudes(pedido.idPedido)}
                                                className="btn-ver-solicitudes"
                                                disabled={cargando}
                                            >
                                                Ver Solicitudes
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {/* Vista de Solicitudes */}
                {pedidoSeleccionado && !solicitudDetalle && (
                    <div className="solicitudes-container">
                        {/* Filtros */}
                        <div className="filtros-solicitudes">
                            <label>Filtrar por estado:</label>
                            <select
                                value={filtroEstado}
                                onChange={(e) => setFiltroEstado(e.target.value as EstadoSolicitud | 'TODOS')}
                                className="form-select"
                            >
                                <option value="TODOS">Todas</option>
                                <option value="PDP">Pendientes de Pago</option>
                                <option value="PGD">Pagadas</option>
                                <option value="CAN">Canceladas</option>
                            </select>
                            <span className="filtro-count">
                                {solicitudesFiltradas.length} solicitud(es)
                            </span>
                        </div>

                        {/* Lista de Solicitudes */}
                        {solicitudesFiltradas.length === 0 ? (
                            <div className="pedidos-empty-state">
                                <p>No hay solicitudes con el filtro seleccionado</p>
                            </div>
                        ) : (
                            <div className="solicitudes-lista">
                                {solicitudesFiltradas.map((solicitud) => (
                                    <div key={solicitud.idSolicitud} className="solicitud-card">
                                        <div className="solicitud-card-header">
                                            <div className="solicitud-id-cliente">
                                                <h3>Solicitud #{solicitud.idSolicitud}</h3>
                                                <p className="cliente-nombre">👤 {solicitud.nombreCliente}</p>
                                            </div>
                                            <span className={`badge-estado-solicitud estado-sol-${solicitud.estadoSolicitud.toLowerCase()}`}>
                                                {NOMBRES_ESTADOS_SOLICITUD[solicitud.estadoSolicitud as EstadoSolicitud]}
                                            </span>
                                        </div>

                                        <div className="solicitud-card-body">
                                            <div className="solicitud-info-grid">
                                                <div className="info-item">
                                                    <span className="label">📅 Fecha Solicitud:</span>
                                                    <span className="value">{solicitud.fechaSolicitud}</span>
                                                </div>
                                                <div className="info-item">
                                                    <span className="label">⏰ Límite de Pago:</span>
                                                    <span className="value">{solicitud.fechaLimitePago}</span>
                                                </div>
                                                <div className="info-item">
                                                    <span className="label">📦 Productos:</span>
                                                    <span className="value">{solicitud.productos.length}</span>
                                                </div>
                                                <div className="info-item">
                                                    <span className="label">💰 Total:</span>
                                                    <span className="value total-precio">
                                                        ${calcularTotalSolicitud(solicitud).toLocaleString()}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="solicitud-card-footer">
                                            <button
                                                onClick={() => handleVerDetalles(solicitud)}
                                                className="btn-sm btn-secondary"
                                            >
                                                📋 Ver Detalles
                                            </button>

                                            {solicitud.estadoSolicitud === 'PDP' && (
                                                <button
                                                    onClick={() => handleMarcarComoPagada(solicitud.idSolicitud)}
                                                    className="btn-sm btn-success"
                                                    disabled={cargando}
                                                >
                                                    ✓ Marcar como Pagada
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {/* Modal de Detalles de Solicitud */}
                {solicitudDetalle && (
                    <ModalDetallesSolicitud
                        solicitud={solicitudDetalle}
                        onCerrar={handleCerrarDetalles}
                    />
                )}
            </div>
        </DashboardLayout>
    );
}

// Modal para ver detalles completos de una solicitud
interface ModalDetallesSolicitudProps {
    solicitud: solicitudesService.SolicitudResponse;
    onCerrar: () => void;
}

function ModalDetallesSolicitud({ solicitud, onCerrar }: ModalDetallesSolicitudProps) {
    const [productosEnriquecidos, setProductosEnriquecidos] = React.useState<solicitudesService.ProductoSolicitudResponse[]>([]);
    const [cargandoProductos, setCargandoProductos] = React.useState(true);

    React.useEffect(() => {
        const cargarNombresProductos = async () => {
            try {
                setCargandoProductos(true);

                // Obtener IDs únicos de productos
                const idsProductos = solicitud.productos.map(p => p.idProducto);

                // Obtener información completa de productos
                const productosCompletos = await obtenerProductosPorIdsPublico(idsProductos);

                // Enriquecer productos de la solicitud con nombres
                const productosConNombres = solicitud.productos.map(prodSolicitud => {
                    const prodCompleto = productosCompletos.find((p: Producto) => p.idProducto === prodSolicitud.idProducto);
                    return {
                        ...prodSolicitud,
                        nombreProducto: prodCompleto?.nombreProducto || prodSolicitud.nombreProducto || `Producto #${prodSolicitud.idProducto}`
                    };
                });

                setProductosEnriquecidos(productosConNombres);
            } catch (error) {
                // Si falla, usar los productos originales
                setProductosEnriquecidos(solicitud.productos);
            } finally {
                setCargandoProductos(false);
            }
        };

        cargarNombresProductos();
    }, [solicitud]);

    const calcularTotal = (): number => {
        return solicitud.productos.reduce((total, prod) => total + prod.precio, 0);
    };

    return (
        <div className="modal-overlay" onClick={onCerrar}>
            <div className="modal-content modal-lg" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Detalles de Solicitud #{solicitud.idSolicitud}</h2>
                    <button className="modal-close" onClick={onCerrar}>✕</button>
                </div>

                <div className="modal-body">
                    {/* Información General */}
                    <div className="detalle-seccion">
                        <h3 className="detalle-titulo">Información General</h3>
                        <div className="detalle-grid">
                            <div className="detalle-item">
                                <strong>Estado:</strong>
                                <span className={`badge-estado-solicitud estado-sol-${solicitud.estadoSolicitud.toLowerCase()}`}>
                                    {NOMBRES_ESTADOS_SOLICITUD[solicitud.estadoSolicitud as EstadoSolicitud]}
                                </span>
                            </div>
                            <div className="detalle-item">
                                <strong>Cliente:</strong>
                                <span>{solicitud.nombreCliente}</span>
                            </div>
                            <div className="detalle-item">
                                <strong>Fecha de Solicitud:</strong>
                                <span>{solicitud.fechaSolicitud}</span>
                            </div>
                            <div className="detalle-item">
                                <strong>Fecha Límite de Pago:</strong>
                                <span>{solicitud.fechaLimitePago}</span>
                            </div>
                            <div className="detalle-item">
                                <strong>Dirección de Entrega:</strong>
                                <span>{solicitud.direccionEntrega}</span>
                            </div>
                            <div className="detalle-item">
                                <strong>Modificaciones Restantes:</strong>
                                <span>{solicitud.modificacionesRestantes}</span>
                            </div>
                        </div>
                    </div>

                    {/* Productos */}
                    <div className="detalle-seccion">
                        <h3 className="detalle-titulo">Productos Solicitados</h3>
                        {cargandoProductos ? (
                            <div style={{ textAlign: 'center', padding: '20px' }}>
                                <p>⏳ Cargando información de productos...</p>
                            </div>
                        ) : (
                            <div className="tabla-productos-detalle">
                                <table className="tabla-productos">
                                    <thead>
                                        <tr>
                                            <th>Producto</th>
                                            <th>Cantidad</th>
                                            <th>Precio</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {productosEnriquecidos.map((producto) => (
                                            <tr key={producto.idProducto}>
                                                <td>{producto.nombreProducto || 'Sin nombre'}</td>
                                                <td>{producto.cantidadSolicitada}</td>
                                                <td>${producto.precio.toLocaleString()}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <td colSpan={3} style={{ textAlign: 'right', fontWeight: 'bold' }}>
                                                Total:
                                            </td>
                                            <td style={{ fontWeight: 'bold', fontSize: '16px' }}>
                                                ${calcularTotal().toLocaleString()}
                                            </td>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        )}
                    </div>
                </div>

                <div className="modal-footer">
                    <Button variant="outline" onClick={onCerrar}>
                        Cerrar
                    </Button>
                </div>
            </div>
        </div>
    );
}
