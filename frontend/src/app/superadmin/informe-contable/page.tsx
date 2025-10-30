/**
 * Informe Contable - Super Administrador
 * Generación de informes en Excel por cliente o pedido
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { useToast } from '@/components/ui';
import { extractErrorInfo } from '@/utils/errorHandler';
import * as informeService from '@/services/informeService';

type TipoInforme = 'cliente' | 'pedido';

export default function InformeContablePage() {
    const { showSuccess, showError } = useToast();
    const [tipoInforme, setTipoInforme] = useState<TipoInforme>('cliente');
    const [cargando, setCargando] = useState(false);

    // Estados para clientes
    const [clientes, setClientes] = useState<informeService.UsuarioInforme[]>(
        []
    );
    const [clienteSeleccionado, setClienteSeleccionado] = useState<number | null>(
        null
    );

    // Estados para pedidos
    const [pedidos, setPedidos] = useState<informeService.PedidoInforme[]>([]);
    const [pedidoSeleccionado, setPedidoSeleccionado] = useState<number | null>(
        null
    );

    // Cargar datos al cambiar tipo de informe
    useEffect(() => {
        cargarDatos();
    }, [tipoInforme]);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            if (tipoInforme === 'cliente') {
                
                const data = await informeService.obtenerUsuarios();
                // Filtrar solo clientes ACTIVOS
                const clientesActivos = data.filter(
                    (usuario) =>
                        usuario.roles?.includes('CLI') && usuario.estado === 'ACTIVO'
                );
                
                setClientes(clientesActivos);
                setClienteSeleccionado(null);
            } else {
                
                const data = await informeService.obtenerPedidosEntregados();
                // Filtrar solo pedidos con estado ENT (Entregados)
                const pedidosEntregados = data.filter(
                    (pedido) => pedido.estadoPedido === 'ENT'
                );
                
                setPedidos(pedidosEntregados);
                setPedidoSeleccionado(null);
            }
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };    const handleSeleccionarCliente = (idCliente: number) => {
        setClienteSeleccionado(idCliente);
    };

    const handleSeleccionarPedido = (idPedido: number) => {
        setPedidoSeleccionado(idPedido);
    };

    const handleDescargarCliente = async () => {
        if (!clienteSeleccionado) return;

        try {
            setCargando(true);
            const cliente = clientes.find((c) => c.idUsuario === clienteSeleccionado);
            if (!cliente) return;

            // Construir nombre del archivo de forma segura
            const nombreArchivo = cliente.nombreCompleto
                || `${cliente.nombres || ''}_${cliente.apellidos || ''}`.trim()
                || `Cliente_${clienteSeleccionado}`;

            await informeService.descargarInformeCliente(
                clienteSeleccionado,
                nombreArchivo
            );
            showSuccess('Informe descargado', 'El informe se ha generado exitosamente');
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const handleDescargarPedido = async () => {
        if (!pedidoSeleccionado) return;

        try {
            setCargando(true);
            await informeService.descargarInformePedido(pedidoSeleccionado);
            showSuccess('Informe descargado', 'El informe se ha generado exitosamente');
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    return (
        <DashboardLayout role="SAD">
            {/* Los mensajes ahora se muestran con el sistema de Toast */}

            {/* Encabezado */}
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Informe Contable</h1>
                <p className="dashboard-page-subtitle">
                    Generación de informes en Excel por cliente o pedido
                </p>
            </div>

            <div className="dashboard-content">
                {/* Selector de tipo de informe */}
                <div className="informe-selector-tipo">
                    <button
                        className={`tipo-btn ${tipoInforme === 'cliente' ? 'active' : ''}`}
                        onClick={() => setTipoInforme('cliente')}
                    >
                        👤 Informe por Cliente
                    </button>
                    <button
                        className={`tipo-btn ${tipoInforme === 'pedido' ? 'active' : ''}`}
                        onClick={() => setTipoInforme('pedido')}
                    >
                        📦 Informe por Pedido
                    </button>
                </div>

                {/* Listado de clientes */}
                {tipoInforme === 'cliente' && (
                    <div className="informe-seccion">
                        <h2 className="informe-subtitulo">Seleccionar Cliente</h2>
                        {cargando ? (
                            <div className="informe-loading">⏳ Cargando clientes...</div>
                        ) : clientes.length === 0 ? (
                            <div className="informe-empty">No hay clientes disponibles</div>
                        ) : (
                            <>
                                <div className="informe-grid">
                                    {clientes.map((cliente) => (
                                        <div
                                            key={cliente.idUsuario}
                                            className={`informe-card ${clienteSeleccionado === cliente.idUsuario ? 'selected' : ''}`}
                                            onClick={() => handleSeleccionarCliente(cliente.idUsuario)}
                                        >
                                            <div className="informe-card-header">
                                                <span className="informe-icon">👤</span>
                                                <h3>
                                                    {cliente.nombres} {cliente.apellidos}
                                                </h3>
                                            </div>
                                            <div className="informe-card-body">
                                                <p>
                                                    <strong>Cédula:</strong> {cliente.cedula}
                                                </p>
                                                {cliente.telefono && (
                                                    <p>
                                                        <strong>Teléfono:</strong> {cliente.telefono}
                                                    </p>
                                                )}
                                                {cliente.direccion && (
                                                    <p>
                                                        <strong>Dirección:</strong> {cliente.direccion}
                                                    </p>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                {/* Botón de descarga para clientes */}
                                {clienteSeleccionado && (
                                    <div className="informe-acciones">
                                        <button
                                            className="btn-descargar"
                                            onClick={handleDescargarCliente}
                                            disabled={cargando}
                                        >
                                            {cargando ? '⏳ Descargando...' : '📥 Descargar Excel'}
                                        </button>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                )}

                {/* Listado de pedidos */}
                {tipoInforme === 'pedido' && (
                    <div className="informe-seccion">
                        <h2 className="informe-subtitulo">Seleccionar Pedido Entregado</h2>
                        {cargando ? (
                            <div className="informe-loading">⏳ Cargando pedidos...</div>
                        ) : pedidos.length === 0 ? (
                            <div className="informe-empty">
                                No hay pedidos entregados disponibles
                            </div>
                        ) : (
                            <>
                                <div className="informe-grid">
                                    {pedidos.map((pedido) => (
                                        <div
                                            key={pedido.idPedido}
                                            className={`informe-card ${pedidoSeleccionado === pedido.idPedido ? 'selected' : ''}`}
                                            onClick={() => handleSeleccionarPedido(pedido.idPedido)}
                                        >
                                            <div className="informe-card-header">
                                                <span className="informe-icon">📦</span>
                                                <h3>Pedido #{pedido.idPedido}</h3>
                                            </div>
                                            <div className="informe-card-body">
                                                <p>
                                                    <strong>Estado:</strong>{' '}
                                                    <span className={`badge-estado-${pedido.estadoPedido.toLowerCase()}`}>
                                                        {pedido.estadoPedido}
                                                    </span>
                                                </p>
                                                <p>
                                                    <strong>Fecha:</strong> {pedido.fechaCreado}
                                                </p>
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                {/* Botón de descarga para pedidos */}
                                {pedidoSeleccionado && (
                                    <div className="informe-acciones">
                                        <button
                                            className="btn-descargar"
                                            onClick={handleDescargarPedido}
                                            disabled={cargando}
                                        >
                                            {cargando ? '⏳ Descargando...' : '📥 Descargar Excel'}
                                        </button>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                )}
            </div>
        </DashboardLayout>
    );
}
