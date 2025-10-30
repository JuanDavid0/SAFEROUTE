'use client';

/**
 * Página de Gestión de Etiquetas - ADMIN
 * Permite seleccionar un pedido y descargar las etiquetas en PDF
 */

import { useEffect, useState } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import etiquetasService, {
    PedidoEtiqueta,
    EtiquetasPedidoResponse,
} from '@/services/etiquetasService';

export default function EtiquetasPage() {
    // ===========================
    // ESTADOS
    // ===========================
    const [pedidos, setPedidos] = useState<PedidoEtiqueta[]>([]);
    const [pedidoSeleccionado, setPedidoSeleccionado] = useState<number | null>(
        null
    );
    const [etiquetasPreview, setEtiquetasPreview] =
        useState<EtiquetasPedidoResponse | null>(null);
    const [cargando, setCargando] = useState<boolean>(false);
    const [cargandoPreview, setCargandoPreview] = useState<boolean>(false);
    const [mensaje, setMensaje] = useState<{
        tipo: 'success' | 'error' | 'info';
        texto: string;
    } | null>(null);

    // ===========================
    // EFECTOS
    // ===========================

    // Cargar pedidos al montar el componente
    useEffect(() => {
        cargarPedidos();
    }, []);

    // ===========================
    // FUNCIONES
    // ===========================

    /**
     * Cargar lista de pedidos
     */
    const cargarPedidos = async () => {
        try {
            setCargando(true);
            const data = await etiquetasService.obtenerPedidos();


            // Validar que sea un array
            if (!Array.isArray(data)) {
                
                setPedidos([]);
                mostrarMensaje(
                    'error',
                    'Error: La respuesta del servidor no tiene el formato esperado'
                );
                return;
            }

            // Filtrar solo pedidos con estado ENT (Entregados)
            const pedidosEntregados = data.filter(
                (pedido) => pedido.estadoPedido === 'ENT'
            );
            

            setPedidos(pedidosEntregados);

            if (pedidosEntregados.length === 0) {
                mostrarMensaje('info', 'No hay pedidos entregados disponibles');
            }
        } catch (error: any) {
            
            setPedidos([]);
            mostrarMensaje(
                'error',
                error.message || 'Error al cargar los pedidos'
            );
        } finally {
            setCargando(false);
        }
    };

    /**
     * Seleccionar un pedido y cargar su preview de etiquetas
     */
    const handleSeleccionarPedido = async (idPedido: number) => {
        try {
            setPedidoSeleccionado(idPedido);
            setCargandoPreview(true);
            setEtiquetasPreview(null);

            const data = await etiquetasService.obtenerEtiquetasPedido(idPedido);
            setEtiquetasPreview(data);
            
        } catch (error: any) {
            
            mostrarMensaje(
                'error',
                error.message || 'Error al cargar las etiquetas'
            );
            setPedidoSeleccionado(null);
        } finally {
            setCargandoPreview(false);
        }
    };

    /**
     * Descargar PDF de etiquetas
     */
    const handleDescargarPDF = async () => {
        if (!pedidoSeleccionado) return;

        try {
            setCargando(true);
            const nombreArchivo = `Etiquetas_Pedido_${pedidoSeleccionado}`;

            await etiquetasService.descargarEtiquetasPDF(
                pedidoSeleccionado,
                nombreArchivo
            );

            mostrarMensaje('success', '✅ PDF de etiquetas descargado exitosamente');
        } catch (error: any) {
            
            mostrarMensaje('error', error.message || 'Error al descargar el PDF');
        } finally {
            setCargando(false);
        }
    };

    /**
     * Mostrar mensaje temporal
     */
    const mostrarMensaje = (
        tipo: 'success' | 'error' | 'info',
        texto: string
    ) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 5000);
    };

    // ===========================
    // RENDER
    // ===========================

    return (
        <DashboardLayout role="ADM">
            <div className="etiquetas-container">
                {/* Header */}
                <div className="etiquetas-header">
                    <h1 className="etiquetas-titulo">🏷️ Gestión de Etiquetas</h1>
                    <p className="etiquetas-descripcion">
                        Selecciona un pedido para ver y descargar sus etiquetas
                    </p>
                </div>

                {/* Mensajes */}
                {mensaje && (
                    <div className={`mensaje mensaje-${mensaje.tipo}`}>
                        {mensaje.texto}
                    </div>
                )}

                {/* Contenido principal */}
                <div className="etiquetas-contenido">
                    {/* Tabla de pedidos */}
                    <div className="etiquetas-seccion">
                        <h2 className="etiquetas-subtitulo">Pedidos Disponibles</h2>

                        {cargando && !pedidoSeleccionado ? (
                            <div className="etiquetas-loading">⏳ Cargando pedidos...</div>
                        ) : pedidos.length === 0 ? (
                            <div className="etiquetas-empty">No hay pedidos disponibles</div>
                        ) : (
                            <div className="tabla-container">
                                <table className="tabla-etiquetas">
                                    <thead>
                                        <tr>
                                            <th>ID Pedido</th>
                                            <th>Estado</th>
                                            <th>Fecha Creación</th>
                                            <th>Fecha Cierre</th>
                                            <th>Admin ID</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {pedidos.map((pedido) => (
                                            <tr
                                                key={pedido.idPedido}
                                                className={
                                                    pedidoSeleccionado === pedido.idPedido
                                                        ? 'selected'
                                                        : ''
                                                }
                                            >
                                                <td>#{pedido.idPedido}</td>
                                                <td>
                                                    <span
                                                        className={`badge-estado-${pedido.estadoPedido.toLowerCase()}`}
                                                    >
                                                        {pedido.estadoPedido}
                                                    </span>
                                                </td>
                                                <td>{pedido.fechaCreado}</td>
                                                <td>{pedido.fechaCierre || 'Pendiente'}</td>
                                                <td>{pedido.idAdmin}</td>
                                                <td>
                                                    <button
                                                        className="btn-seleccionar"
                                                        onClick={() =>
                                                            handleSeleccionarPedido(pedido.idPedido)
                                                        }
                                                        disabled={cargandoPreview}
                                                    >
                                                        {pedidoSeleccionado === pedido.idPedido
                                                            ? '✓ Seleccionado'
                                                            : 'Ver Etiquetas'}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>

                    {/* Preview de etiquetas */}
                    {pedidoSeleccionado && (
                        <div className="etiquetas-seccion preview-seccion">
                            <div className="preview-header">
                                <h2 className="etiquetas-subtitulo">
                                    Vista Previa - Pedido #{pedidoSeleccionado}
                                </h2>
                                <button
                                    className="btn-descargar"
                                    onClick={handleDescargarPDF}
                                    disabled={cargando}
                                >
                                    {cargando ? '⏳ Descargando...' : '📥 Descargar PDF'}
                                </button>
                            </div>

                            {cargandoPreview ? (
                                <div className="etiquetas-loading">
                                    ⏳ Cargando vista previa...
                                </div>
                            ) : etiquetasPreview ? (
                                <>
                                    <div className="preview-info">
                                        <p>
                                            <strong>Fecha de Entrega:</strong>{' '}
                                            {etiquetasPreview.fechaEntrega}
                                        </p>
                                        <p>
                                            <strong>Total de Etiquetas:</strong>{' '}
                                            {etiquetasPreview.totalEtiquetas}
                                        </p>
                                    </div>

                                    <div className="tabla-container">
                                        <table className="tabla-preview">
                                            <thead>
                                                <tr>
                                                    <th>N°</th>
                                                    <th>ID Solicitud</th>
                                                    <th>Cliente</th>
                                                    <th>Dirección</th>
                                                    <th>Teléfono</th>
                                                    <th>Etiqueta</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {etiquetasPreview.etiquetas.map((etiqueta) => (
                                                    <tr key={etiqueta.idSolicitud}>
                                                        <td>{etiqueta.numeroEtiqueta}</td>
                                                        <td>#{etiqueta.idSolicitud}</td>
                                                        <td>{etiqueta.nombreCliente}</td>
                                                        <td>{etiqueta.direccion}</td>
                                                        <td>{etiqueta.telefono}</td>
                                                        <td>
                                                            {etiqueta.numeroEtiqueta} de{' '}
                                                            {etiqueta.totalEtiquetas}
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </>
                            ) : (
                                <div className="etiquetas-empty">
                                    No se pudieron cargar las etiquetas
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </DashboardLayout>
    );
}
