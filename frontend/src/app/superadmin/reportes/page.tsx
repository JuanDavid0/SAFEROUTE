/**
 * Reportes - Super Administrador
 * Módulo de reportes con navegación por pestañas y gráficos
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { useToast } from '@/components/ui';
import { extractErrorInfo } from '@/utils/errorHandler';
import * as reportesService from '@/services/reportesService';
import {
    BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
    XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

// Tipos de reportes
type TipoReporte = 'resumen' | 'mas-vendidos' | 'ingresos' | 'mayor-ganancia' | 'clientes';

// Colores para gráficos
const COLORES = ['#1F4E5F', '#E46B6B', '#6BBF8E', '#F57C00', '#7B1FA2', '#1976D2', '#388E3C'];

export default function ReportesPage() {
    const { showSuccess, showError } = useToast();
    const [reporteActivo, setReporteActivo] = useState<TipoReporte>('resumen');
    const [cargando, setCargando] = useState(false);

    return (
        <DashboardLayout role="SAD">
            {/* Los mensajes ahora se muestran con el sistema de Toast */}

            {/* Encabezado */}
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Reportes</h1>
                <p className="dashboard-page-subtitle">
                    Análisis de datos y estadísticas del negocio
                </p>
            </div>

            {/* Navegación por pestañas */}
            <div className="reportes-tabs">
                <button
                    className={`reporte-tab ${reporteActivo === 'resumen' ? 'active' : ''}`}
                    onClick={() => setReporteActivo('resumen')}
                >
                    📊 Resumen General
                </button>
                <button
                    className={`reporte-tab ${reporteActivo === 'mas-vendidos' ? 'active' : ''}`}
                    onClick={() => setReporteActivo('mas-vendidos')}
                >
                    🏆 Más Vendidos
                </button>
                <button
                    className={`reporte-tab ${reporteActivo === 'ingresos' ? 'active' : ''}`}
                    onClick={() => setReporteActivo('ingresos')}
                >
                    💰 Ingresos
                </button>
                <button
                    className={`reporte-tab ${reporteActivo === 'mayor-ganancia' ? 'active' : ''}`}
                    onClick={() => setReporteActivo('mayor-ganancia')}
                >
                    💎 Mayor Ganancia
                </button>
                <button
                    className={`reporte-tab ${reporteActivo === 'clientes' ? 'active' : ''}`}
                    onClick={() => setReporteActivo('clientes')}
                >
                    👥 Clientes Frecuentes
                </button>
            </div>

            {/* Contenido del reporte */}
            <div className="dashboard-content">
                {reporteActivo === 'resumen' && (
                    <ReporteResumen
                        cargando={cargando}
                        setCargando={setCargando}
                        showError={showError}
                    />
                )}
                {reporteActivo === 'mas-vendidos' && (
                    <ReporteProductosMasVendidos
                        cargando={cargando}
                        setCargando={setCargando}
                        showError={showError}
                    />
                )}
                {reporteActivo === 'ingresos' && (
                    <ReporteIngresos
                        cargando={cargando}
                        setCargando={setCargando}
                        showError={showError}
                    />
                )}
                {reporteActivo === 'mayor-ganancia' && (
                    <ReporteMayorGanancia
                        cargando={cargando}
                        setCargando={setCargando}
                        showError={showError}
                    />
                )}
                {reporteActivo === 'clientes' && (
                    <ReporteClientesFrecuentes
                        cargando={cargando}
                        setCargando={setCargando}
                        showError={showError}
                    />
                )}
            </div>
        </DashboardLayout>
    );
}

// ===========================
// COMPONENTE: Resumen General
// ===========================
interface ReporteProps {
    cargando: boolean;
    setCargando: (value: boolean) => void;
    showError: (message: string, details?: string, errorCode?: string) => void;
}

function ReporteResumen({ cargando, setCargando, showError }: ReporteProps) {
    const [datos, setDatos] = useState<reportesService.ReporteResumen | null>(null);

    useEffect(() => {
        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            const data = await reportesService.obtenerResumenGeneral();
            setDatos(data);
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    if (cargando) return <div className="reportes-loading">⏳ Cargando datos...</div>;
    if (!datos) return <div className="reportes-empty">No hay datos disponibles</div>;

    const datosPedidos = [
        { name: 'Activos', value: datos.pedidosActivos, color: '#388E3C' },
        { name: 'En Curso', value: datos.pedidosEnCurso, color: '#1976D2' },
        { name: 'Entregados', value: datos.pedidosEntregados, color: '#6BBF8E' },
        { name: 'Cancelados', value: datos.pedidosCancelados, color: '#E46B6B' },
    ];

    const datosSolicitudes = [
        { name: 'Pagadas', value: datos.solicitudesPagadas, color: '#388E3C' },
        { name: 'Pendientes', value: datos.solicitudesPendientes, color: '#F57C00' },
        { name: 'Confirmadas', value: datos.solicitudesConfirmadas, color: '#1976D2' },
        { name: 'Rechazadas', value: datos.solicitudesRechazadas, color: '#E46B6B' },
    ];

    return (
        <div className="reporte-container">
            <div className="reporte-header">
                <h2>Resumen General del Negocio</h2>
                <p>Vista general de todas las métricas importantes</p>
            </div>

            {/* Cards de métricas principales */}
            <div className="metricas-grid">
                <div className="metrica-card metrica-ingresos">
                    <div className="metrica-icon">💰</div>
                    <div className="metrica-info">
                        <h3>Ingresos Totales</h3>
                        <p className="metrica-valor">${datos.ingresosTotales.toLocaleString()}</p>
                    </div>
                </div>
                <div className="metrica-card metrica-costos">
                    <div className="metrica-icon">📉</div>
                    <div className="metrica-info">
                        <h3>Costos Totales</h3>
                        <p className="metrica-valor">${datos.costosTotales.toLocaleString()}</p>
                    </div>
                </div>
                <div className="metrica-card metrica-ganancia">
                    <div className="metrica-icon">💎</div>
                    <div className="metrica-info">
                        <h3>Ganancia Neta</h3>
                        <p className="metrica-valor">${datos.gananciaNeta.toLocaleString()}</p>
                    </div>
                </div>
                <div className="metrica-card metrica-clientes">
                    <div className="metrica-icon">👥</div>
                    <div className="metrica-info">
                        <h3>Clientes Frecuentes</h3>
                        <p className="metrica-valor">{datos.clientesActivos} / {datos.totalClientes}</p>
                    </div>
                </div>
            </div>

            {/* Gráficos */}
            <div className="graficos-row">
                <div className="grafico-card">
                    <h3>Estado de Pedidos</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={datosPedidos}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                label={({ name, percent }: any) => `${name}: ${(percent * 100).toFixed(0)}%`}
                                outerRadius={80}
                                fill="#8884d8"
                                dataKey="value"
                            >
                                {datosPedidos.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={entry.color} />
                                ))}
                            </Pie>
                            <Tooltip />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                <div className="grafico-card">
                    <h3>Estado de Solicitudes</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={datosSolicitudes}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                label={({ name, percent }: any) => `${name}: ${(percent * 100).toFixed(0)}%`}
                                outerRadius={80}
                                fill="#8884d8"
                                dataKey="value"
                            >
                                {datosSolicitudes.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={entry.color} />
                                ))}
                            </Pie>
                            <Tooltip />
                        </PieChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Estadísticas adicionales */}
            <div className="estadisticas-adicionales">
                <div className="stat-item">
                    <span className="stat-label">Total de Pedidos:</span>
                    <span className="stat-value">{datos.totalPedidos}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Total de Solicitudes:</span>
                    <span className="stat-value">{datos.totalSolicitudes}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Total de Productos:</span>
                    <span className="stat-value">{datos.totalProductos}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Productos Activos:</span>
                    <span className="stat-value">{datos.productosActivos}</span>
                </div>
            </div>
        </div>
    );
}

// ===========================
// COMPONENTE: Productos Más Vendidos
// ===========================
function ReporteProductosMasVendidos({ cargando, setCargando, showError }: ReporteProps) {
    const [datos, setDatos] = useState<reportesService.ProductoMasVendido[]>([]);
    const [limite, setLimite] = useState<number>(10);

    useEffect(() => {
        cargarDatos();
    }, [limite]);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            const data = await reportesService.obtenerProductosMasVendidos(limite);
            setDatos(data);
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    if (cargando) return <div className="reportes-loading">⏳ Cargando datos...</div>;

    return (
        <div className="reporte-container">
            <div className="reporte-header">
                <div>
                    <h2>Productos Más Vendidos</h2>
                    <p>Los productos con mayor cantidad de ventas</p>
                </div>
                <div className="reporte-filtros">
                    <label>Top:</label>
                    <select value={limite} onChange={(e) => setLimite(Number(e.target.value))} className="form-select">
                        <option value={5}>5 productos</option>
                        <option value={10}>10 productos</option>
                        <option value={20}>20 productos</option>
                    </select>
                </div>
            </div>

            {datos.length === 0 ? (
                <div className="reportes-empty">No hay datos disponibles</div>
            ) : (
                <>
                    {/* Gráfico de barras */}
                    <div className="grafico-card grafico-full">
                        <h3>Cantidad Vendida por Producto</h3>
                        <ResponsiveContainer width="100%" height={400}>
                            <BarChart data={datos}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="nombreProducto" angle={-45} textAnchor="end" height={100} />
                                <YAxis />
                                <Tooltip formatter={(value) => value.toLocaleString()} />
                                <Legend />
                                <Bar dataKey="cantidadTotalVendida" fill="#1F4E5F" name="Cantidad Vendida" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Tabla de datos */}
                    <div className="tabla-reporte">
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Producto</th>
                                    <th>Categoría</th>
                                    <th>Cantidad</th>
                                    <th>Ingresos</th>
                                    <th>Ganancia</th>
                                </tr>
                            </thead>
                            <tbody>
                                {datos.map((producto, index) => (
                                    <tr key={producto.idProducto}>
                                        <td>{index + 1}</td>
                                        <td><strong>{producto.nombreProducto}</strong></td>
                                        <td>{producto.categoria}</td>
                                        <td>{producto.cantidadTotalVendida.toLocaleString()}</td>
                                        <td>${producto.ingresosGenerados.toLocaleString()}</td>
                                        <td className="ganancia-positiva">${producto.gananciaNeta.toLocaleString()}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </div>
    );
}

// ===========================
// COMPONENTE: Ingresos
// ===========================
function ReporteIngresos({ cargando, setCargando, showError }: ReporteProps) {
    const [datos, setDatos] = useState<reportesService.ReporteIngresos | null>(null);
    const [fechaInicio, setFechaInicio] = useState<string>('2025-01-01');
    const [fechaFin, setFechaFin] = useState<string>('2025-12-31');
    const [agrupacion, setAgrupacion] = useState<'TRIMESTRAL' | 'ANUAL'>('ANUAL');

    useEffect(() => {
        cargarDatos();
    }, [fechaInicio, fechaFin, agrupacion]);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            const data = await reportesService.obtenerReporteIngresos(fechaInicio, fechaFin, agrupacion);
            setDatos(data);
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    if (cargando) return <div className="reportes-loading">⏳ Cargando datos...</div>;
    if (!datos) return <div className="reportes-empty">No hay datos disponibles</div>;

    return (
        <div className="reporte-container">
            <div className="reporte-header">
                <div>
                    <h2>Reporte de Ingresos</h2>
                    <p>Análisis de ingresos, costos y ganancias por periodo</p>
                </div>
                <div className="reporte-filtros">
                    <input
                        type="date"
                        value={fechaInicio}
                        onChange={(e) => setFechaInicio(e.target.value)}
                        className="form-control"
                    />
                    <input
                        type="date"
                        value={fechaFin}
                        onChange={(e) => setFechaFin(e.target.value)}
                        className="form-control"
                    />
                    <select value={agrupacion} onChange={(e) => setAgrupacion(e.target.value as any)} className="form-select">
                        <option value="TRIMESTRAL">Trimestral</option>
                        <option value="ANUAL">Anual</option>
                    </select>
                </div>
            </div>

            {/* Resumen financiero */}
            <div className="metricas-grid">
                <div className="metrica-card">
                    <h3>Total Ingresos</h3>
                    <p className="metrica-valor">${datos.totalIngresos.toLocaleString()}</p>
                </div>
                <div className="metrica-card">
                    <h3>Total Costos</h3>
                    <p className="metrica-valor">${datos.totalCostos.toLocaleString()}</p>
                </div>
                <div className="metrica-card">
                    <h3>Ganancia Neta</h3>
                    <p className="metrica-valor ganancia-positiva">${datos.gananciaNeta.toLocaleString()}</p>
                </div>
                <div className="metrica-card">
                    <h3>Solicitudes Pagadas</h3>
                    <p className="metrica-valor">{datos.totalSolicitudesPagadas}</p>
                </div>
            </div>

            {/* Gráfico de líneas */}
            <div className="grafico-card grafico-full">
                <h3>Evolución de Ingresos, Costos y Ganancias</h3>
                <ResponsiveContainer width="100%" height={400}>
                    <LineChart data={datos.datosPorPeriodo}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="periodo" />
                        <YAxis />
                        <Tooltip formatter={(value) => `$${Number(value).toLocaleString()}`} />
                        <Legend />
                        <Line type="monotone" dataKey="ingresos" stroke="#388E3C" strokeWidth={2} name="Ingresos" />
                        <Line type="monotone" dataKey="costos" stroke="#E46B6B" strokeWidth={2} name="Costos" />
                        <Line type="monotone" dataKey="ganancia" stroke="#1F4E5F" strokeWidth={2} name="Ganancia" />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

// ===========================
// COMPONENTE: Mayor Ganancia
// ===========================
function ReporteMayorGanancia({ cargando, setCargando, showError }: ReporteProps) {
    const [datos, setDatos] = useState<reportesService.ProductoMayorGanancia[]>([]);

    useEffect(() => {
        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            const data = await reportesService.obtenerProductosMayorGanancia();
            setDatos(data);
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    if (cargando) return <div className="reportes-loading">⏳ Cargando datos...</div>;

    return (
        <div className="reporte-container">
            <div className="reporte-header">
                <h2>Productos con Mayor Ganancia</h2>
                <p>Los productos que generan mayor rentabilidad</p>
            </div>

            {datos.length === 0 ? (
                <div className="reportes-empty">No hay datos disponibles</div>
            ) : (
                <>
                    {/* Gráfico de barras comparativo */}
                    <div className="grafico-card grafico-full">
                        <h3>Comparación de Ingresos vs Costos</h3>
                        <ResponsiveContainer width="100%" height={400}>
                            <BarChart data={datos}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="nombreProducto" angle={-45} textAnchor="end" height={100} />
                                <YAxis />
                                <Tooltip formatter={(value) => `$${Number(value).toLocaleString()}`} />
                                <Legend />
                                <Bar dataKey="ingresosGenerados" fill="#388E3C" name="Ingresos" />
                                <Bar dataKey="costosAsociados" fill="#E46B6B" name="Costos" />
                                <Bar dataKey="gananciaNeta" fill="#1F4E5F" name="Ganancia Neta" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Tabla de datos */}
                    <div className="tabla-reporte">
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Producto</th>
                                    <th>Categoría</th>
                                    <th>Ingresos</th>
                                    <th>Costos</th>
                                    <th>Ganancia Neta</th>
                                    <th>Margen</th>
                                </tr>
                            </thead>
                            <tbody>
                                {datos.map((producto, index) => {
                                    const margen = ((producto.gananciaNeta / producto.ingresosGenerados) * 100).toFixed(1);
                                    return (
                                        <tr key={producto.idProducto}>
                                            <td>{index + 1}</td>
                                            <td><strong>{producto.nombreProducto}</strong></td>
                                            <td>{producto.categoria}</td>
                                            <td>${producto.ingresosGenerados.toLocaleString()}</td>
                                            <td>${producto.costosAsociados.toLocaleString()}</td>
                                            <td className="ganancia-positiva">${producto.gananciaNeta.toLocaleString()}</td>
                                            <td className="ganancia-positiva">{margen}%</td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </div>
    );
}

// ===========================
// COMPONENTE: Clientes Frecuentes
// ===========================
function ReporteClientesFrecuentes({ cargando, setCargando, showError }: ReporteProps) {
    const [datos, setDatos] = useState<reportesService.ClienteFrecuente[]>([]);

    useEffect(() => {
        cargarDatos();
    }, []);

    const cargarDatos = async () => {
        try {
            setCargando(true);
            const data = await reportesService.obtenerClientesFrecuentes();
            setDatos(data);
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    if (cargando) return <div className="reportes-loading">⏳ Cargando datos...</div>;

    return (
        <div className="reporte-container">
            <div className="reporte-header">
                <h2>Clientes Frecuentes</h2>
                <p>Los clientes con mayor actividad y gastos</p>
            </div>

            {datos.length === 0 ? (
                <div className="reportes-empty">No hay datos disponibles</div>
            ) : (
                <>
                    {/* Gráfico de barras - Gasto total por cliente */}
                    <div className="grafico-card grafico-full">
                        <h3>Gasto Total por Cliente</h3>
                        <ResponsiveContainer width="100%" height={400}>
                            <BarChart data={datos.slice(0, 10)}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="nombreCompleto" angle={-45} textAnchor="end" height={100} />
                                <YAxis />
                                <Tooltip formatter={(value) => `$${Number(value).toLocaleString()}`} />
                                <Legend />
                                <Bar dataKey="montoTotalGastado" fill="#1F4E5F" name="Gasto Total" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Tabla de datos */}
                    <div className="tabla-reporte">
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Cliente</th>
                                    <th>Cédula</th>
                                    <th>Solicitudes</th>
                                    <th>Pagadas</th>
                                    <th>Gasto Total</th>
                                    <th>Promedio</th>
                                    <th>Primera Compra</th>
                                </tr>
                            </thead>
                            <tbody>
                                {datos.map((cliente, index) => (
                                    <tr key={cliente.cedula}>
                                        <td>{index + 1}</td>
                                        <td><strong>{cliente.nombreCompleto}</strong></td>
                                        <td>{cliente.cedula}</td>
                                        <td>{cliente.totalSolicitudes}</td>
                                        <td className="ganancia-positiva">{cliente.solicitudesPagadas}</td>
                                        <td>${cliente.montoTotalGastado.toLocaleString()}</td>
                                        <td>${cliente.promedioGastoPorSolicitud.toLocaleString()}</td>
                                        <td>{cliente.primeraCompra}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </div>
    );
}
