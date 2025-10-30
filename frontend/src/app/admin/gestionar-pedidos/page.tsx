/**
 * Gestionar Pedidos - Administrador
 * Gestión de pedidos activos: consolidar, actualizar estado, cerrar
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { Button, useToast } from '@/components/ui';
import * as pedidosService from '@/services/pedidosService';
import * as solicitudesService from '@/services/solicitudesService';
import { extractErrorInfo } from '@/utils/errorHandler';

// Estados de pedido según el enum del backend
type EstadoPedido = 'CRT' | 'ACT' | 'CRM' | 'CRA' | 'PRD' | 'RCP' | 'RTA' | 'ADU' | 'ENT';

// Mapa de transiciones permitidas según el backend
const TRANSICIONES_PERMITIDAS: Record<EstadoPedido, EstadoPedido[]> = {
    'CRT': ['ACT'],
    'ACT': ['CRM', 'CRA'],
    'CRM': [],
    'CRA': [],
    'PRD': ['RCP'],
    'RCP': ['RTA'],
    'RTA': ['ADU', 'PRD'],
    'ADU': ['ENT', 'PRD'],
    'ENT': []
};

// Nombres descriptivos de estados
const NOMBRES_ESTADOS: Record<EstadoPedido, string> = {
    'CRT': 'Creado',
    'ACT': 'Activo',
    'CRM': 'Cerrado Manual',
    'CRA': 'Cerrado Automático',
    'PRD': 'Perdido',
    'RCP': 'Recuperado',
    'RTA': 'En Ruta',
    'ADU': 'Aduana',
    'ENT': 'Entregado'
};

interface PedidoConSolicitudes extends pedidosService.PedidoResponse {
    cantidadSolicitudes?: number;
    consolidado?: boolean;
}

type OrdenColumna = 'fechaCreado' | 'fechaCierre' | 'cantidadSolicitudes' | null;
type DireccionOrden = 'asc' | 'desc';

export default function GestionarPedidosPage() {
    const { showSuccess, showError, showWarning } = useToast();

    const [pedidos, setPedidos] = useState<PedidoConSolicitudes[]>([]);
    const [cargando, setCargando] = useState(false);
    const [mostrarModalEstado, setMostrarModalEstado] = useState(false);
    const [mostrarModalFecha, setMostrarModalFecha] = useState(false);
    const [pedidoSeleccionado, setPedidoSeleccionado] = useState<PedidoConSolicitudes | null>(null);
    const [nuevaFechaCierre, setNuevaFechaCierre] = useState('');

    // Estados para ordenamiento
    const [ordenColumna, setOrdenColumna] = useState<OrdenColumna>(null);
    const [direccionOrden, setDireccionOrden] = useState<DireccionOrden>('desc');

    useEffect(() => {
        cargarPedidos();
    }, []);

    const cargarPedidos = async () => {
        try {
            setCargando(true);
            const data = await pedidosService.obtenerPedidos();

            // Filtrar solo pedidos activos (no CRT ni cerrados)
            const pedidosActivos = data.filter(p =>
                p.estadoPedido !== 'CRT' &&
                p.estadoPedido !== 'CRM' &&
                p.estadoPedido !== 'CRA' &&
                p.estadoPedido !== 'ENT'
            );

            // Obtener la cantidad de solicitudes pagadas para cada pedido
            const pedidosConInfo = await Promise.all(
                pedidosActivos.map(async (p) => {
                    const cantidadSolicitudes = await solicitudesService.contarSolicitudesPagadas(p.idPedido);
                    return {
                        ...p,
                        cantidadSolicitudes,
                        consolidado: p.estadoPedido !== 'ACT' // Si no es ACT, ya fue consolidado
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

    // Función para ordenar pedidos
    const handleOrdenar = (columna: OrdenColumna) => {
        if (ordenColumna === columna) {
            // Si ya está ordenada por esta columna, cambiar dirección
            setDireccionOrden(direccionOrden === 'asc' ? 'desc' : 'asc');
        } else {
            // Nueva columna, ordenar descendente por defecto
            setOrdenColumna(columna);
            setDireccionOrden('desc');
        }
    };

    // Obtener pedidos ordenados
    const obtenerPedidosOrdenados = (): PedidoConSolicitudes[] => {
        if (!ordenColumna) return pedidos;

        const pedidosOrdenados = [...pedidos].sort((a, b) => {
            let valorA: any;
            let valorB: any;

            switch (ordenColumna) {
                case 'fechaCreado':
                    valorA = new Date(a.fechaCreado).getTime();
                    valorB = new Date(b.fechaCreado).getTime();
                    break;
                case 'fechaCierre':
                    valorA = new Date(a.fechaCierre).getTime();
                    valorB = new Date(b.fechaCierre).getTime();
                    break;
                case 'cantidadSolicitudes':
                    valorA = a.cantidadSolicitudes || 0;
                    valorB = b.cantidadSolicitudes || 0;
                    break;
                default:
                    return 0;
            }

            if (direccionOrden === 'asc') {
                return valorA > valorB ? 1 : valorA < valorB ? -1 : 0;
            } else {
                return valorA < valorB ? 1 : valorA > valorB ? -1 : 0;
            }
        });

        return pedidosOrdenados;
    };

    // Copiar URL al portapapeles
    const copiarURL = (hash: string | null) => {
        if (!hash) return;
        const url = `${window.location.origin}/pedido/${hash}`;
        navigator.clipboard.writeText(url);
        showSuccess('URL copiada', 'La URL se ha copiado al portapapeles');
    };

    const handleConsolidar = async (pedido: PedidoConSolicitudes) => {
        // Validar que tenga al menos una solicitud pagada
        if (!pedido.cantidadSolicitudes || pedido.cantidadSolicitudes === 0) {
            showError(
                'No se puede consolidar',
                'El pedido debe tener al menos una solicitud pagada para consolidar'
            );
            return;
        }

        if (!confirm(`¿Estás seguro de consolidar el pedido #${pedido.idPedido}?\n\nEsto cancelará las solicitudes vencidas y cambiará a "En Ruta" las ${pedido.cantidadSolicitudes} solicitud(es) pagada(s).`)) {
            return;
        }

        try {
            setCargando(true);

            // 1. Cancelar solicitudes vencidas
            await pedidosService.cancelarSolicitudesVencidas(pedido.idPedido);

            // 2. Consolidar pedido (cambiar a RTA las pagadas)
            await pedidosService.consolidarPedido(pedido.idPedido);

            showSuccess(
                'Pedido consolidado',
                `El pedido #${pedido.idPedido} se ha consolidado exitosamente`
            );
            await cargarPedidos();
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const handleAbrirModalEstado = (pedido: PedidoConSolicitudes) => {
        setPedidoSeleccionado(pedido);
        setMostrarModalEstado(true);
    };

    const handleCerrarModalEstado = () => {
        setPedidoSeleccionado(null);
        setMostrarModalEstado(false);
    };

    const handleActualizarEstado = async (nuevoEstado: EstadoPedido) => {
        if (!pedidoSeleccionado) return;

        try {
            setCargando(true);
            await pedidosService.actualizarEstadoPedido(pedidoSeleccionado.idPedido, nuevoEstado);
            showSuccess(
                'Estado actualizado',
                `El estado se ha actualizado a ${NOMBRES_ESTADOS[nuevoEstado]}`
            );
            handleCerrarModalEstado();
            await cargarPedidos();
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const handleCerrarManual = async (idPedido: number) => {
        if (!confirm('¿Estás seguro de cerrar manualmente este pedido?')) {
            return;
        }

        try {
            setCargando(true);
            await pedidosService.cancelarPedido(idPedido);
            showSuccess('Pedido cerrado', 'El pedido se ha cerrado exitosamente');
            await cargarPedidos();
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const handleAbrirModalFecha = (pedido: PedidoConSolicitudes) => {
        setPedidoSeleccionado(pedido);
        // Convertir fecha actual a formato YYYY-MM-DD
        const fechaActual = pedido.fechaCierre.split(' ')[0]; // Tomar solo la fecha sin hora
        setNuevaFechaCierre(fechaActual);
        setMostrarModalFecha(true);
    };

    const handleCerrarModalFecha = () => {
        setPedidoSeleccionado(null);
        setNuevaFechaCierre('');
        setMostrarModalFecha(false);
    };

    const handleActualizarFechaCierre = async () => {
        if (!pedidoSeleccionado || !nuevaFechaCierre) return;

        // Validar que la nueva fecha sea futura
        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);
        const fechaSeleccionada = new Date(nuevaFechaCierre);

        if (fechaSeleccionada < hoy) {
            showWarning('Fecha inválida', 'La fecha de cierre debe ser futura');
            return;
        }

        try {
            setCargando(true);
            await pedidosService.actualizarFechaCierre(pedidoSeleccionado.idPedido, nuevaFechaCierre);
            showSuccess(
                'Fecha actualizada',
                `La fecha de cierre se ha actualizado a ${nuevaFechaCierre}`
            );
            handleCerrarModalFecha();
            await cargarPedidos();
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    return (
        <DashboardLayout role="ADM">
            {/* Los mensajes ahora se muestran con el sistema de Toast */}

            {/* Encabezado */}
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Gestionar Pedidos</h1>
            </div>

            <div className="dashboard-content">
                <div className="gestionar-pedidos-container">
                    {cargando ? (
                        <div className="pedidos-empty-state">
                            <p>⏳ Cargando pedidos...</p>
                        </div>
                    ) : pedidos.length === 0 ? (
                        <div className="pedidos-empty-state">
                            <p>No hay pedidos activos para gestionar</p>
                        </div>
                    ) : (
                        <div className="tabla-gestionar-pedidos">
                            <table className="tabla-pedidos">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Estado</th>
                                        <th
                                            className="th-ordenable"
                                            onClick={() => handleOrdenar('fechaCreado')}
                                            title="Ordenar por fecha de creación"
                                        >
                                            Fecha Creación
                                            {ordenColumna === 'fechaCreado' && (
                                                <span className="icono-orden">
                                                    {direccionOrden === 'asc' ? ' ▲' : ' ▼'}
                                                </span>
                                            )}
                                        </th>
                                        <th
                                            className="th-ordenable"
                                            onClick={() => handleOrdenar('fechaCierre')}
                                            title="Ordenar por fecha de cierre"
                                        >
                                            Fecha Cierre
                                            {ordenColumna === 'fechaCierre' && (
                                                <span className="icono-orden">
                                                    {direccionOrden === 'asc' ? ' ▲' : ' ▼'}
                                                </span>
                                            )}
                                        </th>
                                        <th
                                            className="th-ordenable"
                                            onClick={() => handleOrdenar('cantidadSolicitudes')}
                                            title="Ordenar por número de solicitudes"
                                        >
                                            Solicitudes
                                            {ordenColumna === 'cantidadSolicitudes' && (
                                                <span className="icono-orden">
                                                    {direccionOrden === 'asc' ? ' ▲' : ' ▼'}
                                                </span>
                                            )}
                                        </th>
                                        <th>URL Pedido</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {obtenerPedidosOrdenados().map((pedido) => (
                                        <tr key={pedido.idPedido}>
                                            <td className="td-id">#{pedido.idPedido}</td>
                                            <td>
                                                <span className={`badge-estado estado-${pedido.estadoPedido.toLowerCase()}`}>
                                                    {NOMBRES_ESTADOS[pedido.estadoPedido as EstadoPedido]}
                                                </span>
                                            </td>
                                            <td>{pedido.fechaCreado}</td>
                                            <td>{pedido.fechaCierre}</td>
                                            <td className="td-solicitudes">
                                                {pedido.cantidadSolicitudes || 0}
                                            </td>
                                            <td className="td-url">
                                                <div className="url-container">
                                                    <input
                                                        type="text"
                                                        className="input-url"
                                                        value={`${typeof window !== 'undefined' ? window.location.origin : ''}/pedido/${pedido.urlHash}`}
                                                        readOnly
                                                    />
                                                    <button
                                                        className="btn-copiar-url"
                                                        onClick={() => copiarURL(pedido.urlHash)}
                                                        title="Copiar URL"
                                                    >
                                                        📋
                                                    </button>
                                                </div>
                                            </td>
                                            <td className="td-acciones">
                                                <div className="acciones-grupo">
                                                    {/* Cambiar Fecha - Solo para pedidos ACT */}
                                                    {pedido.estadoPedido === 'ACT' && (
                                                        <button
                                                            className="btn-tabla btn-fecha"
                                                            onClick={() => handleAbrirModalFecha(pedido)}
                                                            disabled={cargando}
                                                            title="Cambiar fecha de cierre"
                                                        >
                                                            📅 Fecha
                                                        </button>
                                                    )}

                                                    {/* Consolidar - Solo una vez y si es ACT */}
                                                    {!pedido.consolidado && pedido.estadoPedido === 'ACT' && (
                                                        <button
                                                            className="btn-tabla btn-consolidar"
                                                            onClick={() => handleConsolidar(pedido)}
                                                            disabled={cargando}
                                                            title="Consolidar pedido"
                                                        >
                                                            📦 Consolidar
                                                        </button>
                                                    )}

                                                    {/* Actualizar Estado - Solo después de consolidar */}
                                                    {pedido.consolidado && (
                                                        <button
                                                            className="btn-tabla btn-estado"
                                                            onClick={() => handleAbrirModalEstado(pedido)}
                                                            disabled={cargando}
                                                            title="Actualizar estado"
                                                        >
                                                            🔄 Estado
                                                        </button>
                                                    )}

                                                    {/* Cerrar Manual - Siempre disponible */}
                                                    <button
                                                        className="btn-tabla btn-cerrar"
                                                        onClick={() => handleCerrarManual(pedido.idPedido)}
                                                        disabled={cargando}
                                                        title="Cerrar manualmente"
                                                    >
                                                        ✕ Cerrar
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Modal de Actualizar Estado */}
            {mostrarModalEstado && pedidoSeleccionado && (
                <ModalActualizarEstado
                    pedido={pedidoSeleccionado}
                    onCerrar={handleCerrarModalEstado}
                    onActualizar={handleActualizarEstado}
                    cargando={cargando}
                />
            )}

            {/* Modal de Cambiar Fecha de Cierre */}
            {mostrarModalFecha && pedidoSeleccionado && (
                <ModalCambiarFecha
                    pedido={pedidoSeleccionado}
                    onCerrar={handleCerrarModalFecha}
                    onActualizar={handleActualizarFechaCierre}
                    nuevaFecha={nuevaFechaCierre}
                    setNuevaFecha={setNuevaFechaCierre}
                    cargando={cargando}
                />
            )}
        </DashboardLayout>
    );
}

// Modal para actualizar estado
interface ModalActualizarEstadoProps {
    pedido: PedidoConSolicitudes;
    onCerrar: () => void;
    onActualizar: (nuevoEstado: EstadoPedido) => void;
    cargando: boolean;
}

function ModalActualizarEstado({ pedido, onCerrar, onActualizar, cargando }: ModalActualizarEstadoProps) {
    const estadoActual = pedido.estadoPedido as EstadoPedido;
    const estadosPermitidos = TRANSICIONES_PERMITIDAS[estadoActual] || [];

    return (
        <div className="modal-overlay" onClick={onCerrar}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Actualizar Estado del Pedido #{pedido.idPedido}</h2>
                    <button className="modal-close" onClick={onCerrar}>✕</button>
                </div>

                <div className="modal-body">
                    <div className="estado-actual-info">
                        <p><strong>Estado Actual:</strong></p>
                        <span className={`badge-estado estado-${estadoActual.toLowerCase()}`}>
                            {NOMBRES_ESTADOS[estadoActual]}
                        </span>
                    </div>

                    <div className="estados-permitidos-section">
                        <p><strong>Selecciona el nuevo estado:</strong></p>
                        {estadosPermitidos.length === 0 ? (
                            <p className="text-muted">No hay transiciones permitidas desde este estado.</p>
                        ) : (
                            <div className="estados-grid">
                                {estadosPermitidos.map((estado) => (
                                    <button
                                        key={estado}
                                        className={`btn-estado-opcion estado-${estado.toLowerCase()}`}
                                        onClick={() => onActualizar(estado)}
                                        disabled={cargando}
                                    >
                                        {NOMBRES_ESTADOS[estado]}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                <div className="modal-footer">
                    <Button variant="outline" onClick={onCerrar} disabled={cargando}>
                        Cancelar
                    </Button>
                </div>
            </div>
        </div>
    );
}

// Modal para cambiar fecha de cierre
interface ModalCambiarFechaProps {
    pedido: PedidoConSolicitudes;
    onCerrar: () => void;
    onActualizar: () => void;
    nuevaFecha: string;
    setNuevaFecha: (fecha: string) => void;
    cargando: boolean;
}

function ModalCambiarFecha({ pedido, onCerrar, onActualizar, nuevaFecha, setNuevaFecha, cargando }: ModalCambiarFechaProps) {
    // Obtener la fecha mínima (mañana)
    const obtenerFechaMinima = () => {
        const manana = new Date();
        manana.setDate(manana.getDate() + 1);
        return manana.toISOString().split('T')[0];
    };

    return (
        <div className="modal-overlay" onClick={onCerrar}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Cambiar Fecha de Cierre - Pedido #{pedido.idPedido}</h2>
                    <button className="modal-close" onClick={onCerrar}>✕</button>
                </div>

                <div className="modal-body">
                    <div className="form-group">
                        <label htmlFor="fechaCierre"><strong>Fecha de Cierre Actual:</strong></label>
                        <p className="text-muted">{pedido.fechaCierre}</p>
                    </div>

                    <div className="form-group">
                        <label htmlFor="nuevaFechaCierre">
                            <strong>Nueva Fecha de Cierre:</strong>
                        </label>
                        <input
                            id="nuevaFechaCierre"
                            type="date"
                            className="form-input"
                            value={nuevaFecha}
                            onChange={(e) => setNuevaFecha(e.target.value)}
                            min={obtenerFechaMinima()}
                            disabled={cargando}
                        />
                        <p className="text-muted" style={{ fontSize: '0.875rem', marginTop: '0.5rem' }}>
                            La fecha debe ser futura
                        </p>
                    </div>
                </div>

                <div className="modal-footer">
                    <Button variant="outline" onClick={onCerrar} disabled={cargando}>
                        Cancelar
                    </Button>
                    <Button
                        variant="primary"
                        onClick={onActualizar}
                        disabled={cargando || !nuevaFecha}
                    >
                        {cargando ? 'Actualizando...' : 'Actualizar Fecha'}
                    </Button>
                </div>
            </div>
        </div>
    );
}
