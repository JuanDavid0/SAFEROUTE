/**
 * Logs del Sistema - Super Administrador
 * Vista para consultar todos los logs de auditoría del sistema
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { useToast } from '@/components/ui';
import { extractErrorInfo } from '@/utils/errorHandler';
import * as logsService from '@/services/logsService';
import { Log } from '@/types';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function LogsPage() {
    const { showError } = useToast();
    const [logs, setLogs] = useState<Log[]>([]);
    const [logsFiltrados, setLogsFiltrados] = useState<Log[]>([]);
    const [cargando, setCargando] = useState(false);

    // Filtros
    const [filtroUsuario, setFiltroUsuario] = useState('');
    const [filtroAccion, setFiltroAccion] = useState('');
    const [filtroFecha, setFiltroFecha] = useState('');

    useEffect(() => {
        cargarLogs();
    }, []);

    useEffect(() => {
        aplicarFiltros();
    }, [logs, filtroUsuario, filtroAccion, filtroFecha]);

    const cargarLogs = async () => {
        try {
            setCargando(true);
            const data = await logsService.obtenerLogs();
            // Ordenar logs del más reciente al más antiguo
            const logsOrdenados = data.sort((a, b) => {
                return new Date(b.fechaLog).getTime() - new Date(a.fechaLog).getTime();
            });
            setLogs(logsOrdenados);
        } catch (error: any) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    const aplicarFiltros = () => {
        let resultado = [...logs];

        // Filtro por usuario
        if (filtroUsuario.trim()) {
            resultado = resultado.filter(log =>
                log.nombreUsuario.toLowerCase().includes(filtroUsuario.toLowerCase()) ||
                log.idUsuario.toString().includes(filtroUsuario)
            );
        }

        // Filtro por acción
        if (filtroAccion.trim()) {
            resultado = resultado.filter(log =>
                log.accion.toLowerCase().includes(filtroAccion.toLowerCase())
            );
        }

        // Filtro por fecha
        if (filtroFecha) {
            resultado = resultado.filter(log => {
                const fechaLog = new Date(log.fechaLog).toISOString().split('T')[0];
                return fechaLog === filtroFecha;
            });
        }

        setLogsFiltrados(resultado);
    };

    const limpiarFiltros = () => {
        setFiltroUsuario('');
        setFiltroAccion('');
        setFiltroFecha('');
    };

    const formatearFecha = (fechaISO: string) => {
        try {
            const fecha = new Date(fechaISO);
            return format(fecha, "dd/MM/yyyy HH:mm:ss", { locale: es });
        } catch (error) {
            return fechaISO;
        }
    };

    const getTipoAccion = (accion: string): string => {
        if (accion.includes('Inicio de sesión')) return 'login';
        if (accion.includes('creado') || accion.includes('Creación')) return 'create';
        if (accion.includes('actualizado') || accion.includes('Actualización')) return 'update';
        if (accion.includes('eliminado') || accion.includes('Eliminación')) return 'delete';
        if (accion.includes('Cambio de estado')) return 'status';
        return 'other';
    };

    return (
        <DashboardLayout role="SAD">
            {/* Encabezado */}
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">📋 Logs del Sistema</h1>
                <p className="dashboard-page-subtitle">
                    Auditoría completa de todas las acciones realizadas en el sistema
                </p>
            </div>

            <div className="dashboard-content">
                {/* Filtros */}
                <div className="filtros-container">
                    <div className="filtros-grid">
                        <div className="filtro-item">
                            <label htmlFor="filtroUsuario">Usuario</label>
                            <input
                                id="filtroUsuario"
                                type="text"
                                placeholder="Buscar por nombre o ID..."
                                value={filtroUsuario}
                                onChange={(e) => setFiltroUsuario(e.target.value)}
                                className="form-input"
                            />
                        </div>

                        <div className="filtro-item">
                            <label htmlFor="filtroAccion">Acción</label>
                            <input
                                id="filtroAccion"
                                type="text"
                                placeholder="Buscar en descripción..."
                                value={filtroAccion}
                                onChange={(e) => setFiltroAccion(e.target.value)}
                                className="form-input"
                            />
                        </div>

                        <div className="filtro-item">
                            <label htmlFor="filtroFecha">Fecha</label>
                            <input
                                id="filtroFecha"
                                type="date"
                                value={filtroFecha}
                                onChange={(e) => setFiltroFecha(e.target.value)}
                                className="form-input"
                            />
                        </div>

                        <div className="filtro-item filtro-actions">
                            <button
                                onClick={limpiarFiltros}
                                className="btn-secondary"
                                disabled={!filtroUsuario && !filtroAccion && !filtroFecha}
                            >
                                🔄 Limpiar Filtros
                            </button>
                            <button
                                onClick={cargarLogs}
                                className="btn-primary"
                                disabled={cargando}
                            >
                                {cargando ? '⏳ Cargando...' : '🔍 Actualizar'}
                            </button>
                        </div>
                    </div>

                    <div className="filtros-info">
                        <span className="badge-info">
                            Total de logs: <strong>{logs.length}</strong>
                        </span>
                        <span className="badge-primary">
                            Mostrando: <strong>{logsFiltrados.length}</strong>
                        </span>
                    </div>
                </div>

                {/* Tabla de Logs */}
                {cargando ? (
                    <div className="logs-empty-state">
                        <p>⏳ Cargando logs del sistema...</p>
                    </div>
                ) : logsFiltrados.length === 0 ? (
                    <div className="logs-empty-state">
                        <p>
                            {logs.length === 0
                                ? 'No hay logs registrados en el sistema'
                                : 'No se encontraron logs con los filtros aplicados'}
                        </p>
                    </div>
                ) : (
                    <div className="tabla-container">
                        <table className="tabla-logs">
                            <thead>
                                <tr>
                                    <th>ID Log</th>
                                    <th>Usuario</th>
                                    <th>Acción</th>
                                    <th>Fecha y Hora</th>
                                    <th>Tipo</th>
                                </tr>
                            </thead>
                            <tbody>
                                {logsFiltrados.map((log) => (
                                    <tr key={log.idLog}>
                                        <td className="log-id">#{log.idLog}</td>
                                        <td>
                                            <div className="usuario-info">
                                                <span className="usuario-nombre">{log.nombreUsuario}</span>
                                                <span className="usuario-id">ID: {log.idUsuario}</span>
                                            </div>
                                        </td>
                                        <td className="log-accion">{log.accion}</td>
                                        <td className="log-fecha">{formatearFecha(log.fechaLog)}</td>
                                        <td>
                                            <span className={`badge-log badge-log-${getTipoAccion(log.accion)}`}>
                                                {getTipoAccion(log.accion) === 'login' && '🔐 Login'}
                                                {getTipoAccion(log.accion) === 'create' && '➕ Creación'}
                                                {getTipoAccion(log.accion) === 'update' && '✏️ Actualización'}
                                                {getTipoAccion(log.accion) === 'delete' && '🗑️ Eliminación'}
                                                {getTipoAccion(log.accion) === 'status' && '🔄 Cambio Estado'}
                                                {getTipoAccion(log.accion) === 'other' && '📝 Otro'}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </DashboardLayout>
    );
}
