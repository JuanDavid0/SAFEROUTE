/**
 * Pedidos - Super Administrador
 * Gestión de pedidos
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { Button, Input } from '@/components/ui';
import { obtenerProductos } from '@/services/productosService';
import { crearPedido, obtenerPedidos, cancelarPedido, actualizarEstadoPedido, agregarProductoAlPedido, modificarProductoDelPedido, eliminarProductoDelPedido, type PedidoResponse, type ProductoPedidoRequest } from '@/services/pedidosService';
import { useAuthStore } from '@/stores/authStore';

// Tipo temporal para productos en el formulario
interface ProductoFormulario {
    idProducto: number;
    nombreProducto: string;
    precioUnitario: number;
    seleccionado: boolean;
    cantidadMin: string;
    cantidadMax: string;
}

export default function PedidosPage() {
    // Estados del formulario
    const [fechaCierre, setFechaCierre] = useState('');
    const [busquedaProducto, setBusquedaProducto] = useState('');
    const [productosFormulario, setProductosFormulario] = useState<ProductoFormulario[]>([]);

    // Estados de la lista de pedidos
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [cargando, setCargando] = useState(false);
    const [mensaje, setMensaje] = useState({ tipo: '', texto: '' });

    // Estados de la UI
    const [mostrarFormulario, setMostrarFormulario] = useState(false);
    const [mostrarModalEdicion, setMostrarModalEdicion] = useState(false);
    const [pedidoEditando, setPedidoEditando] = useState<PedidoResponse | null>(null);

    // Obtener usuario autenticado
    const user = useAuthStore((state) => state.user);

    // Cargar productos disponibles al montar el componente
    useEffect(() => {
        cargarProductos();
        cargarPedidos();
    }, []);

    const cargarProductos = async () => {
        try {
            const productos = await obtenerProductos();
            const productosConFormato: ProductoFormulario[] = productos.map(producto => ({
                idProducto: producto.idProducto,
                nombreProducto: producto.nombreProducto,
                precioUnitario: producto.precioUnitario,
                seleccionado: false,
                cantidadMin: '',
                cantidadMax: ''
            }));
            setProductosFormulario(productosConFormato);
        } catch (error) {
            console.error('Error al cargar productos:', error);
            mostrarMensaje('error', 'Error al cargar la lista de productos');
        }
    };

    const cargarPedidos = async () => {
        try {
            setCargando(true);
            const data = await obtenerPedidos();
            setPedidos(data);
        } catch (error) {
            console.error('Error al cargar pedidos:', error);
            mostrarMensaje('error', 'Error al cargar la lista de pedidos');
        } finally {
            setCargando(false);
        }
    };

    const validarFormulario = (): string | null => {
        if (!fechaCierre) {
            return 'Debe seleccionar una fecha de cierre';
        }

        const productosSeleccionados = productosFormulario.filter(p => p.seleccionado);
        if (productosSeleccionados.length === 0) {
            return 'Debe seleccionar al menos un producto';
        }

        for (const producto of productosSeleccionados) {
            if (!producto.cantidadMin || parseInt(producto.cantidadMin) <= 0) {
                return `El producto "${producto.nombreProducto}" debe tener una cantidad mínima válida`;
            }

            if (producto.cantidadMax) {
                const min = parseInt(producto.cantidadMin);
                const max = parseInt(producto.cantidadMax);
                if (max < min) {
                    return `El producto "${producto.nombreProducto}" tiene una cantidad máxima menor a la mínima`;
                }
            }
        }

        return null;
    };

    const handleCrearPedido = async () => {
        // Validar formulario
        const errorValidacion = validarFormulario();
        if (errorValidacion) {
            mostrarMensaje('error', errorValidacion);
            return;
        }

        if (user?.idUsuario === null || user?.idUsuario === undefined) {
            mostrarMensaje('error', 'No se pudo obtener el ID del usuario. Por favor, inicie sesión nuevamente.');
            console.error('Usuario no tiene idUsuario:', user);
            return;
        }

        try {
            setCargando(true);

            // Construir lista de productos seleccionados
            const productosSeleccionados = productosFormulario
                .filter(p => p.seleccionado)
                .map(p => ({
                    idProducto: p.idProducto,
                    cantidadMin: parseInt(p.cantidadMin),
                    ...(p.cantidadMax && parseInt(p.cantidadMax) > 0 ? { cantidadMax: parseInt(p.cantidadMax) } : {})
                }));

            // Crear pedido
            await crearPedido(user.idUsuario, {
                productos: productosSeleccionados,
                fechaCierre
            });

            mostrarMensaje('success', 'Pedido creado exitosamente');

            // Limpiar formulario
            setFechaCierre('');
            setProductosFormulario(prev => prev.map(p => ({
                ...p,
                seleccionado: false,
                cantidadMin: '',
                cantidadMax: ''
            })));
            setMostrarFormulario(false);

            // Recargar lista de pedidos
            cargarPedidos();
        } catch (error: any) {
            console.error('Error al crear pedido:', error);
            mostrarMensaje('error', error.message || 'Error al crear el pedido. Por favor, intente nuevamente.');
        } finally {
            setCargando(false);
        }
    };

    const handleActivarPedido = async (idPedido: number) => {
        if (!confirm('¿Está seguro de activar este pedido?')) return;

        try {
            setCargando(true);
            await actualizarEstadoPedido(idPedido, 'ACT');
            mostrarMensaje('success', 'Pedido activado exitosamente');
            await cargarPedidos();
        } catch (error: any) {
            mostrarMensaje('error', error.message || 'Error al activar pedido');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const handleAbrirEdicion = (pedido: PedidoResponse) => {
        setPedidoEditando(pedido);
        setMostrarModalEdicion(true);
    };

    const handleCerrarEdicion = () => {
        setPedidoEditando(null);
        setMostrarModalEdicion(false);
    };

    const handleAgregarProducto = async (idProducto: number, cantidadMin: number, cantidadMax?: number) => {
        if (!pedidoEditando) return;

        try {
            setCargando(true);
            const producto: ProductoPedidoRequest = {
                idProducto,
                cantidadMin,
                ...(cantidadMax && cantidadMax > 0 ? { cantidadMax } : {})
            };

            const pedidoActualizado = await agregarProductoAlPedido(pedidoEditando.idPedido, producto);
            setPedidoEditando(pedidoActualizado);
            mostrarMensaje('success', 'Producto agregado exitosamente');
            await cargarPedidos();
        } catch (error: any) {
            mostrarMensaje('error', error.message || 'Error al agregar producto');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const handleModificarCantidades = async (idProducto: number, cantidadMin?: number, cantidadMax?: number) => {
        if (!pedidoEditando) return;

        try {
            setCargando(true);
            const datos: Partial<ProductoPedidoRequest> = {};
            if (cantidadMin !== undefined) datos.cantidadMin = cantidadMin;
            if (cantidadMax !== undefined) datos.cantidadMax = cantidadMax;

            const pedidoActualizado = await modificarProductoDelPedido(
                pedidoEditando.idPedido,
                idProducto,
                datos
            );
            setPedidoEditando(pedidoActualizado);
            mostrarMensaje('success', 'Cantidades actualizadas exitosamente');
            await cargarPedidos();
        } catch (error: any) {
            mostrarMensaje('error', error.message || 'Error al actualizar cantidades');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const handleEliminarProducto = async (idProducto: number) => {
        if (!pedidoEditando) return;
        if (!confirm('¿Estás seguro de eliminar este producto del pedido?')) return;

        try {
            setCargando(true);
            const pedidoActualizado = await eliminarProductoDelPedido(pedidoEditando.idPedido, idProducto);
            setPedidoEditando(pedidoActualizado);
            mostrarMensaje('success', 'Producto eliminado exitosamente');
            await cargarPedidos();
        } catch (error: any) {
            mostrarMensaje('error', error.message || 'Error al eliminar producto');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const handleCancelarPedido = async (idPedido: number) => {
        if (!confirm('¿Está seguro de que desea cancelar este pedido?')) {
            return;
        }

        try {
            setCargando(true);
            await cancelarPedido(idPedido);

            mostrarMensaje('success', 'Pedido cancelado exitosamente');
            cargarPedidos();
        } catch (error: any) {
            console.error('Error al cancelar pedido:', error);
            mostrarMensaje('error', error.message || 'Error al cancelar el pedido. Por favor, intente nuevamente.');
        } finally {
            setCargando(false);
        }
    };

    const mostrarMensaje = (tipo: 'success' | 'error', texto: string) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje({ tipo: '', texto: '' }), 5000);
    };

    const handleToggleProducto = (idProducto: number) => {
        setProductosFormulario(prev =>
            prev.map(p =>
                p.idProducto === idProducto
                    ? { ...p, seleccionado: !p.seleccionado, cantidadMin: '', cantidadMax: '' }
                    : p
            )
        );
    };

    const handleCantidadChange = (idProducto: number, campo: 'cantidadMin' | 'cantidadMax', valor: string) => {
        setProductosFormulario(prev =>
            prev.map(p =>
                p.idProducto === idProducto
                    ? { ...p, [campo]: valor }
                    : p
            )
        );
    };

    const handleLimpiarFormulario = () => {
        setFechaCierre('');
        setBusquedaProducto('');
        setProductosFormulario(prev => prev.map(p => ({ ...p, seleccionado: false, cantidadMin: '', cantidadMax: '' })));
    };

    const productosFiltrados = productosFormulario.filter(p =>
        p.nombreProducto.toLowerCase().includes(busquedaProducto.toLowerCase())
    );

    const productosSeleccionados = productosFormulario.filter(p => p.seleccionado).length;

    // Filtrar solo pedidos con estado "CRT" (Creados)
    const pedidosCreados = pedidos.filter(p => p.estadoPedido === 'CRT');

    return (
        <DashboardLayout role="SAD">
            {/* Mensaje de éxito/error */}
            {mensaje.texto && (
                <div className={`alert alert-${mensaje.tipo}`}>
                    {mensaje.texto}
                </div>
            )}

            {/* Encabezado */}
            <div className="dashboard-page-header">
                <div className="flex items-center justify-between">
                    <h1 className="dashboard-page-title">Gestión de Pedidos</h1>
                    <Button
                        variant="primary"
                        onClick={() => {
                            if (mostrarFormulario) {
                                handleLimpiarFormulario();
                            }
                            setMostrarFormulario(!mostrarFormulario);
                        }}
                    >
                        {mostrarFormulario ? '✕ Cancelar' : '+ Crear Pedido'}
                    </Button>
                </div>
            </div>

            <div className="dashboard-content">
                {/* Formulario de creación (condicional) */}
                {mostrarFormulario && (
                    <div className="pedidos-form-card">
                        <h2 className="pedidos-form-title">Nuevo Pedido</h2>

                        {/* Fecha de Cierre */}
                        <div className="pedidos-fecha-section">
                            <label className="form-label">📅 Fecha de Cierre del Pedido *</label>
                            <Input
                                type="date"
                                value={fechaCierre}
                                onChange={(e) => setFechaCierre(e.target.value)}
                                placeholder="Seleccionar fecha"
                            />
                        </div>

                        {/* Sección de Productos */}
                        <div className="pedidos-productos-section">
                            <h3 className="pedidos-section-title">🛒 Productos Disponibles</h3>

                            {/* Barra de búsqueda */}
                            <div className="pedidos-search-bar">
                                <div className="pedidos-search-container">
                                    <svg
                                        className="pedidos-search-icon"
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                                        />
                                    </svg>
                                    <input
                                        type="text"
                                        className="pedidos-search-input"
                                        placeholder="Buscar producto..."
                                        value={busquedaProducto}
                                        onChange={(e) => setBusquedaProducto(e.target.value)}
                                    />
                                </div>
                            </div>

                            {/* Tabla de Productos */}
                            <div className="pedidos-table-container">
                                <table className="pedidos-table">
                                    <thead>
                                        <tr>
                                            <th className="pedidos-th-checkbox"></th>
                                            <th className="pedidos-th">Producto</th>
                                            <th className="pedidos-th">Precio Unit.</th>
                                            <th className="pedidos-th">Cantidad Mín. *</th>
                                            <th className="pedidos-th">Cantidad Máx.</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {productosFiltrados.length === 0 ? (
                                            <tr>
                                                <td colSpan={5} className="pedidos-td-empty">
                                                    No se encontraron productos
                                                </td>
                                            </tr>
                                        ) : (
                                            productosFiltrados.map((producto) => (
                                                <tr
                                                    key={producto.idProducto}
                                                    className={producto.seleccionado ? 'pedidos-tr-selected' : ''}
                                                >
                                                    <td className="pedidos-td-checkbox">
                                                        <input
                                                            type="checkbox"
                                                            className="pedidos-checkbox"
                                                            checked={producto.seleccionado}
                                                            onChange={() => handleToggleProducto(producto.idProducto)}
                                                        />
                                                    </td>
                                                    <td className="pedidos-td">
                                                        <span className="pedidos-producto-nombre">
                                                            {producto.nombreProducto}
                                                        </span>
                                                    </td>
                                                    <td className="pedidos-td">
                                                        <span className="pedidos-precio">
                                                            ${producto.precioUnitario.toFixed(2)}
                                                        </span>
                                                    </td>
                                                    <td className="pedidos-td">
                                                        {producto.seleccionado ? (
                                                            <Input
                                                                type="number"
                                                                min="1"
                                                                placeholder="Ej: 10"
                                                                value={producto.cantidadMin}
                                                                onChange={(e) =>
                                                                    handleCantidadChange(
                                                                        producto.idProducto,
                                                                        'cantidadMin',
                                                                        e.target.value
                                                                    )
                                                                }
                                                            />
                                                        ) : (
                                                            <span className="pedidos-td-disabled">-</span>
                                                        )}
                                                    </td>
                                                    <td className="pedidos-td">
                                                        {producto.seleccionado ? (
                                                            <Input
                                                                type="number"
                                                                min="1"
                                                                placeholder="Opcional"
                                                                value={producto.cantidadMax}
                                                                onChange={(e) =>
                                                                    handleCantidadChange(
                                                                        producto.idProducto,
                                                                        'cantidadMax',
                                                                        e.target.value
                                                                    )
                                                                }
                                                            />
                                                        ) : (
                                                            <span className="pedidos-td-disabled">-</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Resumen */}
                        <div className="pedidos-resumen">
                            <span className="pedidos-resumen-icon">📊</span>
                            <span className="pedidos-resumen-text">
                                {productosSeleccionados} producto{productosSeleccionados !== 1 ? 's' : ''} seleccionado{productosSeleccionados !== 1 ? 's' : ''}
                            </span>
                        </div>

                        {/* Botones de acción */}
                        <div className="pedidos-form-actions">
                            <Button variant="outline" onClick={handleLimpiarFormulario} disabled={cargando}>
                                Limpiar
                            </Button>
                            <Button variant="primary" onClick={handleCrearPedido} disabled={cargando}>
                                {cargando ? 'Creando...' : '✓ Crear Pedido'}
                            </Button>
                        </div>
                    </div>
                )}

                {/* Listado de pedidos */}
                <div className="pedidos-list-section">
                    <h2 className="pedidos-list-title">
                        Pedidos Creados (Pendientes de Activar) ({pedidosCreados.length})
                    </h2>

                    {cargando && !mostrarFormulario ? (
                        <div className="pedidos-empty-state">
                            <p>⏳ Cargando pedidos...</p>
                        </div>
                    ) : pedidosCreados.length === 0 ? (
                        <div className="pedidos-empty-state">
                            <p>No hay pedidos pendientes de activar</p>
                        </div>
                    ) : (
                        <div className="pedidos-grid">
                            {pedidosCreados.map((pedido) => (
                                <div key={pedido.idPedido} className="pedido-card">
                                    <div className="pedido-header">
                                        <div className="pedido-info-header">
                                            <h3 className="pedido-titulo">Pedido #{pedido.idPedido}</h3>
                                            <span className={`pedido-estado estado-${pedido.estadoPedido.toLowerCase()}`}>
                                                {pedido.estadoPedido}
                                            </span>
                                        </div>
                                        <div className="pedido-fechas">
                                            <div className="pedido-fecha-item">
                                                <span className="fecha-label">Creado</span>
                                                <span className="fecha-valor">
                                                    {new Date(pedido.fechaCreado).toLocaleDateString('es-ES')}
                                                </span>
                                            </div>
                                            <div className="pedido-fecha-item">
                                                <span className="fecha-label">Cierre</span>
                                                <span className="fecha-valor">
                                                    {new Date(pedido.fechaCierre).toLocaleDateString('es-ES')}
                                                </span>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="pedido-productos">
                                        <h4 className="pedido-productos-titulo">Productos ({pedido.productos.length})</h4>
                                        <div className="pedido-productos-lista">
                                            {pedido.productos.map((producto, index) => (
                                                <div key={index} className="pedido-producto-item">
                                                    <span className="producto-id">ID: {producto.idProducto}</span>
                                                    <span className="producto-cantidad">
                                                        Min: {producto.cantidadMin}
                                                        {producto.cantidadMax && ` | Max: ${producto.cantidadMax}`}
                                                    </span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    <div className="pedido-actions">
                                        <button
                                            className="btn-action btn-edit"
                                            onClick={() => handleAbrirEdicion(pedido)}
                                            disabled={cargando}
                                        >
                                            ✏️ Editar
                                        </button>
                                        <button
                                            className="btn-action btn-primary"
                                            onClick={() => handleActivarPedido(pedido.idPedido)}
                                            disabled={cargando}
                                        >
                                            ✓ Activar Pedido
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Modal de Edición de Pedido */}
                {mostrarModalEdicion && pedidoEditando && (
                    <ModalEdicionPedido
                        pedido={pedidoEditando}
                        productosDisponibles={productosFormulario}
                        onCerrar={handleCerrarEdicion}
                        onAgregarProducto={handleAgregarProducto}
                        onModificarCantidades={handleModificarCantidades}
                        onEliminarProducto={handleEliminarProducto}
                        cargando={cargando}
                    />
                )}
            </div>
        </DashboardLayout>
    );
}

// Componente Modal de Edición
interface ModalEdicionPedidoProps {
    pedido: PedidoResponse;
    productosDisponibles: ProductoFormulario[];
    onCerrar: () => void;
    onAgregarProducto: (idProducto: number, cantidadMin: number, cantidadMax?: number) => Promise<void>;
    onModificarCantidades: (idProducto: number, cantidadMin?: number, cantidadMax?: number) => Promise<void>;
    onEliminarProducto: (idProducto: number) => Promise<void>;
    cargando: boolean;
}

function ModalEdicionPedido({
    pedido,
    productosDisponibles,
    onCerrar,
    onAgregarProducto,
    onModificarCantidades,
    onEliminarProducto,
    cargando
}: ModalEdicionPedidoProps) {
    const [nuevoProducto, setNuevoProducto] = useState({ idProducto: 0, cantidadMin: '', cantidadMax: '' });
    const [productoEditando, setProductoEditando] = useState<{ idProducto: number; cantidadMin: string; cantidadMax: string } | null>(null);

    const productosNoPedido = productosDisponibles.filter(
        p => !pedido.productos.some(pp => pp.idProducto === p.idProducto)
    );

    const handleAgregar = () => {
        if (!nuevoProducto.idProducto || !nuevoProducto.cantidadMin || parseInt(nuevoProducto.cantidadMin) <= 0) {
            alert('Selecciona un producto y especifica una cantidad mínima válida');
            return;
        }

        const cantMax = nuevoProducto.cantidadMax ? parseInt(nuevoProducto.cantidadMax) : undefined;
        onAgregarProducto(nuevoProducto.idProducto, parseInt(nuevoProducto.cantidadMin), cantMax);
        setNuevoProducto({ idProducto: 0, cantidadMin: '', cantidadMax: '' });
    };

    const handleModificar = () => {
        if (!productoEditando) return;

        const cantMin = productoEditando.cantidadMin ? parseInt(productoEditando.cantidadMin) : undefined;
        const cantMax = productoEditando.cantidadMax ? parseInt(productoEditando.cantidadMax) : undefined;

        if (cantMin !== undefined && cantMin <= 0) {
            alert('La cantidad mínima debe ser mayor a 0');
            return;
        }

        if (cantMax !== undefined && cantMin !== undefined && cantMax < cantMin) {
            alert('La cantidad máxima debe ser mayor o igual a la mínima');
            return;
        }

        onModificarCantidades(productoEditando.idProducto, cantMin, cantMax);
        setProductoEditando(null);
    };

    return (
        <div className="modal-overlay" onClick={onCerrar}>
            <div className="modal-content modal-lg" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Editar Pedido #{pedido.idPedido}</h2>
                    <button className="modal-close" onClick={onCerrar}>✕</button>
                </div>

                <div className="modal-body">
                    {/* Productos actuales del pedido */}
                    <div className="modal-section">
                        <h3 className="modal-section-title">Productos del Pedido</h3>
                        <div className="productos-pedido-lista">
                            {pedido.productos.map((prod) => (
                                <div key={prod.idProducto} className="producto-pedido-item">
                                    {productoEditando?.idProducto === prod.idProducto ? (
                                        <>
                                            <div className="producto-info">
                                                <span className="producto-id-badge">ID: {prod.idProducto}</span>
                                            </div>
                                            <div className="producto-cantidades-edit">
                                                <Input
                                                    type="number"
                                                    placeholder="Mín"
                                                    value={productoEditando.cantidadMin}
                                                    onChange={(e) => setProductoEditando({ ...productoEditando, cantidadMin: e.target.value })}
                                                />
                                                <Input
                                                    type="number"
                                                    placeholder="Máx"
                                                    value={productoEditando.cantidadMax}
                                                    onChange={(e) => setProductoEditando({ ...productoEditando, cantidadMax: e.target.value })}
                                                />
                                            </div>
                                            <div className="producto-acciones">
                                                <button className="btn-sm btn-success" onClick={handleModificar} disabled={cargando}>
                                                    ✓
                                                </button>
                                                <button className="btn-sm btn-secondary" onClick={() => setProductoEditando(null)} disabled={cargando}>
                                                    ✕
                                                </button>
                                            </div>
                                        </>
                                    ) : (
                                        <>
                                            <div className="producto-info">
                                                <span className="producto-id-badge">ID: {prod.idProducto}</span>
                                                <span className="producto-cantidades-texto">
                                                    Mín: {prod.cantidadMin} {prod.cantidadMax && `| Máx: ${prod.cantidadMax}`}
                                                </span>
                                            </div>
                                            <div className="producto-acciones">
                                                <button
                                                    className="btn-sm btn-edit"
                                                    onClick={() => setProductoEditando({
                                                        idProducto: prod.idProducto,
                                                        cantidadMin: prod.cantidadMin.toString(),
                                                        cantidadMax: prod.cantidadMax?.toString() || ''
                                                    })}
                                                    disabled={cargando}
                                                >
                                                    ✏️
                                                </button>
                                                <button
                                                    className="btn-sm btn-delete"
                                                    onClick={() => onEliminarProducto(prod.idProducto)}
                                                    disabled={cargando}
                                                >
                                                    🗑️
                                                </button>
                                            </div>
                                        </>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Agregar nuevo producto */}
                    {productosNoPedido.length > 0 && (
                        <div className="modal-section">
                            <h3 className="modal-section-title">Agregar Producto</h3>
                            <div className="agregar-producto-form">
                                <select
                                    className="form-select"
                                    value={nuevoProducto.idProducto}
                                    onChange={(e) => setNuevoProducto({ ...nuevoProducto, idProducto: parseInt(e.target.value) })}
                                    disabled={cargando}
                                >
                                    <option value={0}>Seleccionar producto...</option>
                                    {productosNoPedido.map((p) => (
                                        <option key={p.idProducto} value={p.idProducto}>
                                            ID: {p.idProducto} - {p.nombreProducto}
                                        </option>
                                    ))}
                                </select>
                                <Input
                                    type="number"
                                    placeholder="Cantidad Mín *"
                                    value={nuevoProducto.cantidadMin}
                                    onChange={(e) => setNuevoProducto({ ...nuevoProducto, cantidadMin: e.target.value })}
                                    disabled={cargando}
                                />
                                <Input
                                    type="number"
                                    placeholder="Cantidad Máx"
                                    value={nuevoProducto.cantidadMax}
                                    onChange={(e) => setNuevoProducto({ ...nuevoProducto, cantidadMax: e.target.value })}
                                    disabled={cargando}
                                />
                                <button className="btn-action btn-primary" onClick={handleAgregar} disabled={cargando}>
                                    {cargando ? '⏳' : '+ Agregar'}
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                <div className="modal-footer">
                    <Button variant="outline" onClick={onCerrar} disabled={cargando}>
                        Cerrar
                    </Button>
                </div>
            </div>
        </div>
    );
}
