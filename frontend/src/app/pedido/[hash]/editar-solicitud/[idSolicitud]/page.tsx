'use client';

/**
 * Página para Editar una Solicitud Existente
 * Permite modificar dirección, agregar/eliminar productos y cambiar cantidades
 */

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useToast } from '@/components/ui';
import { extractErrorInfo } from '@/utils/errorHandler';
import {
    modificarSolicitud,
    agregarProductoASolicitud,
    actualizarCantidadProducto,
    eliminarProductoDeSolicitud,
    obtenerMisSolicitudes,
    type SolicitudCliente,
    type ProductoSolicitud,
} from '@/services/solicitudClienteService';
import { obtenerProductosPorIdsPublico } from '@/services/productosService';
import solicitudPublicaService from '@/services/solicitudPublicaService';
import { type Producto } from '@/types';
import { Loading } from '@/components/ui';

export default function EditarSolicitudPage() {
    const { showSuccess, showError } = useToast();
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

    // Restricciones de cantidad por producto (cantidadMin y cantidadMax)
    const [restriccionesCantidad, setRestriccionesCantidad] = useState<Map<number, { cantidadMin: number; cantidadMax: number | null }>>(new Map());

    // Productos disponibles para agregar
    const [productosDisponibles, setProductosDisponibles] = useState<Producto[]>([]);
    const [mostrarAgregarProducto, setMostrarAgregarProducto] = useState(false);
    const [productoSeleccionado, setProductoSeleccionado] = useState<number | null>(null);
    const [cantidadNueva, setCantidadNueva] = useState(1);

    // Estados de carga y mensajes
    const [cargando, setCargando] = useState(false);
    const [guardando, setGuardando] = useState(false);

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
                    // Sesión corrupta, limpiar
                    localStorage.removeItem(`otp_session_${hashPedido}`);
                }
            }
        }

        if (!token) {
            showError('Sesión Expirada', 'Por favor, autentícate nuevamente.');
            setTimeout(() => {
                router.push(`/pedido/${hashPedido}/mis-solicitudes`);
            }, 2000);
            return;
        }
        setTokenOtp(token);

        // Cargar datos actuales de la solicitud desde el backend
        cargarSolicitudActual(token);
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
     * Cargar datos actuales de la solicitud desde el backend
     */
    const cargarSolicitudActual = async (token: string) => {
        try {
            setCargando(true);
            // Obtener la solicitud actualizada del backend
            const response = await obtenerMisSolicitudes(hashPedido, token);
            const solicitudActual = response.data.find((s: SolicitudCliente) => s.idSolicitud === idSolicitud);

            if (solicitudActual) {
                setDireccion(solicitudActual.direccionEntrega);
                setProductosActuales(solicitudActual.productos);
                setModificacionesRestantes(solicitudActual.modificacionesRestantes);
            }
        } catch (error) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    /**
     * Cargar productos disponibles del pedido
     */
    const cargarProductosDisponibles = async () => {
        try {
            // 1. Obtener los productos del pedido usando el hash
            const pedidoData = await solicitudPublicaService.obtenerPedidoPorHash(hashPedido);
            const idsProductosPedido = pedidoData.data.productos.map((p: any) => p.idProducto);
            setProductosPedido(idsProductosPedido);

            // 2. Guardar restricciones de cantidad (cantidadMin y cantidadMax)
            const restricciones = new Map<number, { cantidadMin: number; cantidadMax: number | null }>();
            pedidoData.data.productos.forEach((p: any) => {
                restricciones.set(p.idProducto, {
                    cantidadMin: p.cantidadMin,
                    cantidadMax: p.cantidadMax
                });
            });
            setRestriccionesCantidad(restricciones);

            // 3. Obtener los productos del catálogo usando método público
            const productosDelPedido = await obtenerProductosPorIdsPublico(idsProductosPedido);

            setProductosDisponibles(productosDelPedido);

            // 4. Enriquecer los productos actuales con los nombres completos
            setProductosActuales(prevProductos =>
                prevProductos.map(prodActual => {
                    const prodCompleto = productosDelPedido.find((p: Producto) => p.idProducto === prodActual.idProducto);
                    return {
                        ...prodActual,
                        nombreProducto: prodCompleto?.nombreProducto || prodActual.nombreProducto || 'Producto'
                    };
                })
            );
        } catch (error) {
            showError('Error al Cargar', 'No se pudieron cargar los productos del pedido');
        }
    };

    /**
     * Guardar cambios de dirección
     */
    const handleGuardarDireccion = async () => {
        if (!tokenOtp) return;

        if (!direccion.trim()) {
            showError('Dirección Requerida', 'La dirección no puede estar vacía');
            return;
        }

        setGuardando(true);
        try {
            const response = await modificarSolicitud(
                idSolicitud,
                { direccionEntrega: direccion },
                tokenOtp
            );
            setModificacionesRestantes(modificacionesRestantes - 1);
            showSuccess('Dirección Actualizada', 'Dirección actualizada exitosamente');
        } catch (error: any) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setGuardando(false);
        }
    };

    /**
     * Actualizar cantidad de un producto existente
     */
    const handleActualizarCantidad = async (idProducto: number, nuevaCantidad: number) => {
        if (!tokenOtp) return;

        // Obtener restricciones de cantidad para el producto
        const restricciones = restriccionesCantidad.get(idProducto);

        if (!restricciones) {
            showError('Error', 'No se encontraron restricciones para este producto');
            return;
        }

        // Validar cantidad mínima
        if (nuevaCantidad < restricciones.cantidadMin) {
            showError(
                'Cantidad Insuficiente',
                `La cantidad debe ser al menos ${restricciones.cantidadMin}`
            );
            return;
        }

        // Validar cantidad máxima (solo si existe)
        if (restricciones.cantidadMax !== null && nuevaCantidad > restricciones.cantidadMax) {
            showError(
                'Cantidad Excedida',
                `La cantidad no puede exceder ${restricciones.cantidadMax}`
            );
            return;
        }

        setGuardando(true);
        try {
            const response = await actualizarCantidadProducto(
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
            showSuccess('Cantidad Actualizada', 'Cantidad actualizada exitosamente');
        } catch (error: any) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setGuardando(false);
        }
    };

    /**
     * Agregar nuevo producto a la solicitud
     */
    const handleAgregarProducto = async () => {
        if (!tokenOtp || !productoSeleccionado) return;

        // Obtener restricciones de cantidad para el producto seleccionado
        const restricciones = restriccionesCantidad.get(productoSeleccionado);

        if (!restricciones) {
            showError('Error', 'No se encontraron restricciones para este producto');
            return;
        }

        // Validar cantidad mínima
        if (cantidadNueva < restricciones.cantidadMin) {
            showError(
                'Cantidad Insuficiente',
                `La cantidad debe ser al menos ${restricciones.cantidadMin}`
            );
            return;
        }

        // Validar cantidad máxima (solo si existe)
        if (restricciones.cantidadMax !== null && cantidadNueva > restricciones.cantidadMax) {
            showError(
                'Cantidad Excedida',
                `La cantidad no puede exceder ${restricciones.cantidadMax}`
            );
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
            showSuccess('Producto Agregado', 'Producto agregado exitosamente');
        } catch (error: any) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
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
            showSuccess('Producto Eliminado', 'Producto eliminado exitosamente');
        } catch (error: any) {
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setGuardando(false);
        }
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
                <h1 className="solicitud-publica-titulo"> Editar Solicitud #{idSolicitud}</h1>
                <p className="solicitud-publica-descripcion">
                    Modificaciones restantes: <strong>{modificacionesRestantes}</strong>
                </p>
                <button
                    className="btn-volver"
                    onClick={() => router.push(`/pedido/${hashPedido}/mis-solicitudes`)}
                    style={{
                        marginTop: '16px',
                        padding: '10px 20px',
                        background: 'linear-gradient(135deg, #6c757d 0%, #5a6268 100%)',
                        color: 'white',
                        border: 'none',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontWeight: '600',
                    }}
                >
                    ← Volver a Mis Solicitudes
                </button>
            </div>

        

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
                    {guardando ? 'Guardando...' : 'Guardar Dirección'}
                </button>
            </div>

            {/* Sección: Productos Actuales */}
            <div className="solicitud-seccion">
                <div className="seccion-header">
                    <h2 className="seccion-titulo">Productos de la Solicitud</h2>
                    <button
                        className="btn-agregar-producto-header"
                        onClick={() => setMostrarAgregarProducto(!mostrarAgregarProducto)}
                        disabled={modificacionesRestantes === 0 || productosParaAgregar.length === 0}
                    >
                        {mostrarAgregarProducto ? 'Cancelar' : 'Agregar Producto'}
                    </button>
                </div>

                {/* Formulario para agregar producto */}
                {mostrarAgregarProducto && (
                    <div className="agregar-producto-form">
                        {/* Selección de Producto */}
                        <div className="form-group">
                            <label className="form-label">Producto</label>
                            <select
                                className="form-input"
                                value={productoSeleccionado || ''}
                                onChange={(e) => {
                                    setProductoSeleccionado(parseInt(e.target.value));
                                    setCantidadNueva(1); // Reset cantidad al cambiar producto
                                }}
                            >
                                <option value="">Selecciona un producto</option>
                                {productosParaAgregar.map((producto) => (
                                    <option key={producto.idProducto} value={producto.idProducto}>
                                        {producto.nombreProducto} - ${producto.precioUnitario.toLocaleString()}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* Mostrar imagen y detalles del producto seleccionado */}
                        {productoSeleccionado && (() => {
                            const producto = productosDisponibles.find(p => p.idProducto === productoSeleccionado);
                            const restricciones = restriccionesCantidad.get(productoSeleccionado);
                            
                            return producto ? (
                                <div className="producto-preview">
                                    {producto.urlImagen && (
                                        <img 
                                            src={producto.urlImagen} 
                                            alt={producto.nombreProducto}
                                            className="producto-imagen-preview"
                                        />
                                    )}
                                    <div className="producto-info-preview">
                                        <p><strong>{producto.nombreProducto}</strong></p>
                                        <p>{producto.descripcionProducto}</p>
                                        <p className="producto-precio">${producto.precioUnitario.toLocaleString()}</p>
                                        {restricciones && (
                                            <p className="producto-rango">
                                                Cantidad: {restricciones.cantidadMin}
                                                {restricciones.cantidadMax !== null ? ` - ${restricciones.cantidadMax}` : '+'} unidades
                                            </p>
                                        )}
                                    </div>
                                </div>
                            ) : null;
                        })()}

                        {/* Campo de cantidad */}
                        <div className="form-group">
                            <label className="form-label">Cantidad</label>
                            <input
                                type="text"
                                inputMode="numeric"
                                pattern="[0-9]*"
                                className="form-input"
                                value={cantidadNueva}
                                onChange={(e) => {
                                    const valor = e.target.value.replace(/\D/g, '');
                                    setCantidadNueva(valor ? parseInt(valor) : 0);
                                }}
                                placeholder="Ingresa la cantidad"
                            />
                            {productoSeleccionado && (() => {
                                const restricciones = restriccionesCantidad.get(productoSeleccionado);
                                if (!restricciones) return null;
                                
                                // Mostrar alerta si la cantidad es inválida
                                if (cantidadNueva < restricciones.cantidadMin) {
                                    return (
                                        <div className="cantidad-alerta cantidad-alerta-error">
                                            La cantidad debe ser al menos {restricciones.cantidadMin}
                                        </div>
                                    );
                                }
                                if (restricciones.cantidadMax !== null && cantidadNueva > restricciones.cantidadMax) {
                                    return (
                                        <div className="cantidad-alerta cantidad-alerta-error">
                                            La cantidad no puede exceder {restricciones.cantidadMax}
                                        </div>
                                    );
                                }
                                
                                // Mostrar mensaje de éxito si la cantidad es válida
                                if (cantidadNueva >= restricciones.cantidadMin) {
                                    return (
                                        <div className="cantidad-alerta cantidad-alerta-success">
                                            Cantidad válida
                                        </div>
                                    );
                                }
                                
                                return null;
                            })()}
                        </div>

                        <button
                            className="btn-confirmar-agregar"
                            onClick={handleAgregarProducto}
                            disabled={!productoSeleccionado || cantidadNueva < 1 || guardando}
                        >
                            {guardando ? 'Agregando...' : 'Confirmar'}
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
                                                type="text"
                                                inputMode="numeric"
                                                pattern="[0-9]*"
                                                className="input-cantidad-tabla"
                                                value={producto.cantidadSolicitada}
                                                onChange={(e) => {
                                                    const valor = e.target.value.replace(/\D/g, '');
                                                    const nuevaCantidad = valor ? parseInt(valor) : 1;
                                                    setProductosActuales(
                                                        productosActuales.map((p) =>
                                                            p.idProducto === producto.idProducto
                                                                ? { ...p, cantidadSolicitada: nuevaCantidad }
                                                                : p
                                                        )
                                                    );
                                                }}
                                                onBlur={() => {
                                                    // Guardar automáticamente cuando pierde el foco
                                                    handleActualizarCantidad(producto.idProducto, producto.cantidadSolicitada);
                                                }}
                                                onKeyDown={(e) => {
                                                    // Guardar al presionar Enter
                                                    if (e.key === 'Enter') {
                                                        e.currentTarget.blur();
                                                    }
                                                }}
                                                disabled={guardando || modificacionesRestantes === 0}
                                                placeholder="1"
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
