/**
 * Informe Contable - Super Administrador
 * Generación de informes en Excel por cliente o pedido
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import * as informeService from '@/services/informeService';

type TipoInforme = 'cliente' | 'pedido';

export default function InformeContablePage() {
    const [tipoInforme, setTipoInforme] = useState<TipoInforme>('cliente');
    const [cargando, setCargando] = useState(false);
    const [mensaje, setMensaje] = useState<{
        tipo: 'success' | 'error';
        texto: string;
    } | null>(null);

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

    const mostrarMensaje = (tipo: 'success' | 'error', texto: string) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 5000);
    };

    // Cargar datos al cambiar tipo de informe
    useEffect(() => {
        cargarDatos();
    }, [tipoInforme]);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            if (tipoInforme === 'cliente') {
                console.log('👥 Cargando clientes...');
                const data = await informeService.obtenerUsuarios();
                // Filtrar solo clientes ACTIVOS
                const clientesActivos = data.filter(
                    (usuario) =>
                        usuario.roles?.includes('CLI')
                );
                console.log(`✅ ${clientesActivos.length} clientes activos encontrados`);
                setClientes(clientesActivos);
                setClienteSeleccionado(null);
            } else {
                console.log('📦 Cargando pedidos entregados...');
                const data = await informeService.obtenerPedidosEntregados();
                // Filtrar solo pedidos con estado ENT (Entregados)
                const pedidosEntregados = data.filter(
                    (pedido) => pedido.estadoPedido === 'ENT'
                );
                console.log(`✅ ${pedidosEntregados.length} pedidos entregados encontrados`);
                setPedidos(pedidosEntregados);
                setPedidoSeleccionado(null);
            }
        } catch (error: any) {
            console.error('❌ Error al cargar datos:', error);
            mostrarMensaje('error', error.message || 'Error al cargar datos');
        } finally {
            setCargando(false);
        }
    };

    const handleSeleccionarCliente = (idCliente: number) => {
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
            mostrarMensaje('success', '✅ Informe descargado exitosamente');
        } catch (error: any) {
            console.error('❌ Error al descargar informe:', error);
            mostrarMensaje('error', error.message || 'Error al descargar informe');
        } finally {
            setCargando(false);
        }
    };

    const handleDescargarPedido = async () => {
        if (!pedidoSeleccionado) return;

        try {
            setCargando(true);
            await informeService.descargarInformePedido(pedidoSeleccionado);
            mostrarMensaje('success', '✅ Informe descargado exitosamente');
        } catch (error: any) {
            console.error('❌ Error al descargar informe:', error);
            mostrarMensaje('error', error.message || 'Error al descargar informe');
        } finally {
            setCargando(false);
        }
    };

    return (
        <DashboardLayout role="SAD">
            {/* Mensaje de feedback */}
            {mensaje && (
                <div className={`alert alert-${mensaje.tipo}`}>{mensaje.texto}</div>
            )}

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
