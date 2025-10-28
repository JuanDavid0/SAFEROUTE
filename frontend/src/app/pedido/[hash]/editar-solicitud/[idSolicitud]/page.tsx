'use client';

/**
 * Página para Editar una Solicitud Existente
 * Permite modificar dirección, agregar/eliminar productos y cambiar cantidades
 */

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
    modificarSolicitud,
    agregarProductoASolicitud,
    actualizarCantidadProducto,
    eliminarProductoDeSolicitud,
    type SolicitudCliente,
    type ProductoSolicitud,
} from '@/services/solicitudClienteService';
import { obtenerProductos } from '@/services/productosService';
import solicitudPublicaService from '@/services/solicitudPublicaService';
import { type Producto } from '@/types';
import { Loading } from '@/components/ui';

export default function EditarSolicitudPage() {
    const params = useParams();
    const router = useRouter();
    const hashPedido = params.hash as string;
    const idSolicitud = parseInt(params.idSolicitud as string);

    // ===========================
    // ESTADOS
    // ===========================

    // Token OTP
    const [tokenOtp, setTokenOtp] = useState<string | null>(null);

    // Datos de la solicitud
    const [direccion, setDireccion] = useState('');
    const [productosActuales, setProductosActuales] = useState<ProductoSolicitud[]>([]);
    const [modificacionesRestantes, setModificacionesRestantes] = useState(0);

    // Productos del pedido (IDs permitidos)
    const [productosPedido, setProductosPedido] = useState<number[]>([]);

    // Productos disponibles para agregar
    const [productosDisponibles, setProductosDisponibles] = useState<Producto[]>([]);
    const [mostrarAgregarProducto, setMostrarAgregarProducto] = useState(false);
    const [productoSeleccionado, setProductoSeleccionado] = useState<number | null>(null);
    const [cantidadNueva, setCantidadNueva] = useState(1);

    // Estados de carga y mensajes
    const [cargando, setCargando] = useState(false);
    const [guardando, setGuardando] = useState(false);
    const [mensaje, setMensaje] = useState<{
        tipo: 'success' | 'error' | 'info';
        texto: string;
    } | null>(null);

    // Modal de confirmación
    const [modalEliminar, setModalEliminar] = useState<{
        visible: boolean;
        idProducto: number | null;
        nombreProducto: string;
    }>({ visible: false, idProducto: null, nombreProducto: '' });

    // ===========================
    // EFECTOS
    // ===========================

    useEffect(() => {
        // Recuperar token OTP de sessionStorage o localStorage
        let token = sessionStorage.getItem('otpToken');

        // Si no está en sessionStorage, verificar localStorage
        if (!token) {
            const sesionOtp = localStorage.getItem(`otp_session_${hashPedido}`);
            if (sesionOtp) {
                try {
                    const datos = JSON.parse(sesionOtp);
                    const ahora = Date.now();

                    // Verificar si la sesión no ha expirado
                    if (datos.expiracion && ahora < datos.expiracion) {
                        token = datos.token;
                        // Guardar también en sessionStorage para uso posterior
                        if (token) {
                            sessionStorage.setItem('otpToken', token);
                        }
                    } else {
                        // Sesión expirada, limpiar localStorage
                        localStorage.removeItem(`otp_session_${hashPedido}`);
                    }
                } catch (error) {
                    console.error('Error al recuperar sesión OTP:', error);
                }
            }
        }

        if (!token) {
            mostrarMensaje('error', 'Sesión expirada. Por favor, autentícate nuevamente.');
            setTimeout(() => {
                router.push(`/pedido/${hashPedido}/mis-solicitudes`);
            }, 2000);
            return;
        }
        setTokenOtp(token);

        // Recuperar datos de la solicitud de sessionStorage
        const solicitudData = sessionStorage.getItem('solicitudEditar');
        if (solicitudData) {
            const solicitud: SolicitudCliente = JSON.parse(solicitudData);
            setDireccion(solicitud.direccionEntrega);
            setProductosActuales(solicitud.productos);
            setModificacionesRestantes(solicitud.modificacionesRestantes);
        }
    }, []);

    useEffect(() => {
        if (tokenOtp) {
            cargarProductosDisponibles();
        }
    }, [tokenOtp]);

    // ===========================
    // FUNCIONES
    // ===========================

    /**
     * Cargar productos disponibles del pedido
     */
    const cargarProductosDisponibles = async () => {
        try {
            // 1. Obtener los productos del pedido usando el hash
            const pedidoData = await solicitudPublicaService.obtenerPedidoPorHash(hashPedido);
            const idsProductosPedido = pedidoData.data.productos.map(p => p.idProducto);
            setProductosPedido(idsProductosPedido);

            // 2. Obtener todos los productos del catálogo
            const todosLosProductos = await obtenerProductos();

            // 3. Filtrar solo los productos que están en el pedido
            const productosDelPedido = todosLosProductos.filter(producto =>
                idsProductosPedido.includes(producto.idProducto)
            );

            setProductosDisponibles(productosDelPedido);
        } catch (error) {
            console.error('Error al cargar productos del pedido:', error);
            mostrarMensaje('error', 'No se pudieron cargar los productos del pedido');
        }
    };

    /**
     * Guardar cambios de dirección
     */
    const handleGuardarDireccion = async () => {
        if (!tokenOtp) return;

        if (!direccion.trim()) {
            mostrarMensaje('error', 'La dirección no puede estar vacía');
            return;
        }

        setGuardando(true);
        try {
            await modificarSolicitud(
                idSolicitud,
                { direccionEntrega: direccion },
                tokenOtp
            );
            setModificacionesRestantes(modificacionesRestantes - 1);
            mostrarMensaje('success', '✅ Dirección actualizada exitosamente');
        } catch (error: any) {
            console.error('Error al modificar dirección:', error);
            const errorMsg = error.error?.details || error.message || 'Error al actualizar dirección';
            mostrarMensaje('error', errorMsg);
        } finally {
            setGuardando(false);
        }
    };

    /**
     * Actualizar cantidad de un producto existente
     */
    const handleActualizarCantidad = async (idProducto: number, nuevaCantidad: number) => {
        if (!tokenOtp) return;

        if (nuevaCantidad < 1) {
            mostrarMensaje('error', 'La cantidad debe ser al menos 1');
            return;
        }

        setGuardando(true);
        try {
            await actualizarCantidadProducto(
                idSolicitud,
                idProducto,
                { cantidad: nuevaCantidad },
                tokenOtp
            );

            // Actualizar estado local
            setProductosActuales(
                productosActuales.map((p) =>
                    p.idProducto === idProducto
                        ? { ...p, cantidadSolicitada: nuevaCantidad }
                        : p
                )
            );
            setModificacionesRestantes(modificacionesRestantes - 1);
            mostrarMensaje('success', '✅ Cantidad actualizada');
        } catch (error: any) {
            console.error('Error al actualizar cantidad:', error);
            const errorMsg = error.error?.details || error.message || 'Error al actualizar cantidad';
            mostrarMensaje('error', errorMsg);
        } finally {
            setGuardando(false);
        }
    };

    /**
     * Agregar nuevo producto a la solicitud
     */
    const handleAgregarProducto = async () => {
        if (!tokenOtp || !productoSeleccionado) return;

        if (cantidadNueva < 1) {
            mostrarMensaje('error', 'La cantidad debe ser al menos 1');
            return;
        }

        setGuardando(true);
        try {
            await agregarProductoASolicitud(
                idSolicitud,
                {
                    idProducto: productoSeleccionado,
                    cantidadSolicitada: cantidadNueva,
                },
                tokenOtp
            );

            // Agregar producto al estado local
            const producto = productosDisponibles.find((p) => p.idProducto === productoSeleccionado);
            if (producto) {
                setProductosActuales([
                    ...productosActuales,
                    {
                        idProducto: producto.idProducto,
                        nombreProducto: producto.nombreProducto,
                        cantidadSolicitada: cantidadNueva,
                        precio: producto.precioUnitario * cantidadNueva,
                    },
                ]);
            }

            setModificacionesRestantes(modificacionesRestantes - 1);
            setMostrarAgregarProducto(false);
            setProductoSeleccionado(null);
            setCantidadNueva(1);
            mostrarMensaje('success', '✅ Producto agregado exitosamente');
        } catch (error: any) {
            console.error('Error al agregar producto:', error);
            const errorMsg = error.error?.details || error.message || 'Error al agregar producto';
            mostrarMensaje('error', errorMsg);
        } finally {
            setGuardando(false);
        }
    };

    /**
     * Eliminar producto de la solicitud
     */
    const handleEliminarProducto = async () => {
        if (!tokenOtp || !modalEliminar.idProducto) return;

        setGuardando(true);
        try {
            await eliminarProductoDeSolicitud(
                idSolicitud,
                modalEliminar.idProducto,
                tokenOtp
            );

            // Eliminar del estado local
            setProductosActuales(
                productosActuales.filter((p) => p.idProducto !== modalEliminar.idProducto)
            );

            setModificacionesRestantes(modificacionesRestantes - 1);
            setModalEliminar({ visible: false, idProducto: null, nombreProducto: '' });
            mostrarMensaje('success', '✅ Producto eliminado exitosamente');
        } catch (error: any) {
            console.error('Error al eliminar producto:', error);
            const errorMsg = error.error?.details || error.message || 'Error al eliminar producto';
            mostrarMensaje('error', errorMsg);
        } finally {
            setGuardando(false);
        }
    };

    /**
     * Mostrar mensaje temporal
     */
    const mostrarMensaje = (tipo: 'success' | 'error' | 'info', texto: string) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 5000);
    };

    /**
     * Calcular total de la solicitud
     */
    const calcularTotal = (): number => {
        return productosActuales.reduce((sum, item) => sum + item.precio, 0);
    };

    /**
     * Filtrar productos ya agregados
     */
    const productosParaAgregar = productosDisponibles.filter(
        (p) => !productosActuales.some((pa) => pa.idProducto === p.idProducto)
    );

    // ===========================
    // RENDER
    // ===========================

    if (!tokenOtp) {
        return <Loading />;
    }

    return (
        <div className="solicitud-publica-container">
            {/* Header */}
            <div className="solicitud-publica-header">
                <h1 className="solicitud-publica-titulo">✏️ Editar Solicitud #{idSolicitud}</h1>
                <p className="solicitud-publica-descripcion">
                    Modificaciones restantes: <strong>{modificacionesRestantes}</strong>
                </p>
            </div>

            {/* Mensajes */}
            {mensaje && (
                <div
                    className={`otp-mensaje ${mensaje.tipo === 'success' ? 'exito' : mensaje.tipo === 'error' ? 'error' : 'info'
                        }`}
                >
                    {mensaje.texto}
                </div>
            )}

            {/* Sección: Modificar Dirección */}
            <div className="solicitud-seccion">
                <div className="seccion-header">
                    <h2 className="seccion-titulo">📍 Dirección de Entrega</h2>
                </div>

                <div className="form-group">
                    <label className="form-label">
                        Nueva Dirección <span className="required">*</span>
                    </label>
                    <textarea
                        className="form-textarea"
                        rows={3}
                        value={direccion}
                        onChange={(e) => setDireccion(e.target.value)}
                        placeholder="Ingresa la dirección completa de entrega"
                        disabled={guardando || modificacionesRestantes === 0}
                    />
                </div>

                <button
                    className="btn-guardar"
                    onClick={handleGuardarDireccion}
                    disabled={guardando || modificacionesRestantes === 0}
                >
                    {guardando ? '⏳ Guardando...' : '💾 Guardar Dirección'}
                </button>
            </div>

            {/* Sección: Productos Actuales */}
            <div className="solicitud-seccion">
                <div className="seccion-header">
                    <h2 className="seccion-titulo">📦 Productos de la Solicitud</h2>
                    <button
                        className="btn-agregar-producto-header"
                        onClick={() => setMostrarAgregarProducto(!mostrarAgregarProducto)}
                        disabled={modificacionesRestantes === 0 || productosParaAgregar.length === 0}
                    >
                        {mostrarAgregarProducto ? '✖️ Cancelar' : '➕ Agregar Producto'}
                    </button>
                </div>

                {/* Formulario para agregar producto */}
                {mostrarAgregarProducto && (
                    <div className="agregar-producto-form">
                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Producto</label>
                                <select
                                    className="form-input"
                                    value={productoSeleccionado || ''}
                                    onChange={(e) => setProductoSeleccionado(parseInt(e.target.value))}
                                >
                                    <option value="">Selecciona un producto</option>
                                    {productosParaAgregar.map((producto) => (
                                        <option key={producto.idProducto} value={producto.idProducto}>
                                            {producto.nombreProducto} - ${producto.precioUnitario.toLocaleString()}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Cantidad</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    value={cantidadNueva}
                                    onChange={(e) => setCantidadNueva(parseInt(e.target.value) || 1)}
                                    min="1"
                                />
                            </div>
                        </div>

                        <button
                            className="btn-confirmar-agregar"
                            onClick={handleAgregarProducto}
                            disabled={!productoSeleccionado || guardando}
                        >
                            {guardando ? '⏳ Agregando...' : '✅ Confirmar'}
                        </button>
                    </div>
                )}

                {/* Tabla de productos */}
                <div className="tabla-container">
                    <table className="tabla-resumen">
                        <thead>
                            <tr>
                                <th>Producto</th>
                                <th>Cantidad</th>
                                <th>Precio Unit.</th>
                                <th>Subtotal</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            {productosActuales.length === 0 ? (
                                <tr>
                                    <td colSpan={5} style={{ textAlign: 'center', padding: '40px' }}>
                                        No hay productos en esta solicitud
                                    </td>
                                </tr>
                            ) : (
                                productosActuales.map((producto) => (
                                    <tr key={producto.idProducto}>
                                        <td>{producto.nombreProducto || 'Producto'}</td>
                                        <td>
                                            <input
                                                type="number"
                                                className="input-cantidad-tabla"
                                                value={producto.cantidadSolicitada}
                                                onChange={(e) => {
                                                    const nuevaCantidad = parseInt(e.target.value) || 1;
                                                    setProductosActuales(
                                                        productosActuales.map((p) =>
                                                            p.idProducto === producto.idProducto
                                                                ? { ...p, cantidadSolicitada: nuevaCantidad }
                                                                : p
                                                        )
                                                    );
                                                }}
                                                onBlur={() => {
                                                    if (producto.cantidadSolicitada !== productosActuales.find(p => p.idProducto === producto.idProducto)?.cantidadSolicitada) {
                                                        handleActualizarCantidad(producto.idProducto, producto.cantidadSolicitada);
                                                    }
                                                }}
                                                min="1"
                                                disabled={guardando || modificacionesRestantes === 0}
                                            />
                                        </td>
                                        <td>${(producto.precio / producto.cantidadSolicitada).toLocaleString()}</td>
                                        <td>${producto.precio.toLocaleString()}</td>
                                        <td>
                                            <button
                                                className="btn-eliminar-producto"
                                                onClick={() =>
                                                    setModalEliminar({
                                                        visible: true,
                                                        idProducto: producto.idProducto,
                                                        nombreProducto: producto.nombreProducto || 'Producto',
                                                    })
                                                }
                                                disabled={guardando || modificacionesRestantes === 0}
                                            >
                                                🗑️
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colSpan={3} style={{ textAlign: 'right', fontWeight: 'bold' }}>
                                    TOTAL:
                                </td>
                                <td className="total-valor">${calcularTotal().toLocaleString()}</td>
                                <td></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>

            {/* Botón Volver */}
            <div style={{ textAlign: 'center', marginTop: '30px' }}>
                <button
                    className="btn-volver-solicitudes"
                    onClick={() => router.push(`/pedido/${hashPedido}/mis-solicitudes`)}
                >
                    ← Volver a Mis Solicitudes
                </button>
            </div>

            {/* Modal de confirmación para eliminar */}
            {modalEliminar.visible && (
                <div className="modal-overlay" onClick={() => setModalEliminar({ visible: false, idProducto: null, nombreProducto: '' })}>
                    <div className="modal-confirmacion" onClick={(e) => e.stopPropagation()}>
                        <h3 className="modal-titulo">⚠️ Confirmar Eliminación</h3>
                        <p className="modal-mensaje">
                            ¿Estás seguro de eliminar el producto <strong>{modalEliminar.nombreProducto}</strong> de tu solicitud?
                        </p>
                        <div className="modal-acciones">
                            <button
                                className="btn-modal-cancelar"
                                onClick={() => setModalEliminar({ visible: false, idProducto: null, nombreProducto: '' })}
                                disabled={guardando}
                            >
                                Cancelar
                            </button>
                            <button
                                className="btn-modal-eliminar"
                                onClick={handleEliminarProducto}
                                disabled={guardando}
                            >
                                {guardando ? '⏳ Eliminando...' : 'Sí, Eliminar'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
