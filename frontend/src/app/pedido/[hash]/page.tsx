'use client';

/**
 * Página Pública de Solicitud de Pedido
 * Vista para que clientes realicen solicitudes usando el hash del pedido
 */

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import solicitudPublicaService from '@/services/solicitudPublicaService';
import { obtenerProductoPorId } from '@/services/productosService';
import type { Producto, SolicitudProductoDTO } from '@/types';
import { Loading } from '@/components/ui';

interface ProductoConInfo {
    idProducto: number;
    producto: Producto | null;
    cantidadMin: number;
    cantidadMax: number;
    cantidadSeleccionada: number;
}

export default function PedidoPublicoPage() {
    const params = useParams();
    const router = useRouter();
    const hashPedido = params.hash as string;

    // ===========================
    // ESTADOS
    // ===========================
    const [cargando, setCargando] = useState(true);
    const [enviando, setEnviando] = useState(false);
    const [idPedido, setIdPedido] = useState<number | null>(null);
    const [fechaCierre, setFechaCierre] = useState<string>('');
    const [productos, setProductos] = useState<ProductoConInfo[]>([]);
    const [productoSeleccionado, setProductoSeleccionado] = useState<number | null>(null);

    // Modal de selección inicial
    const [mostrarModalInicial, setMostrarModalInicial] = useState(true);
    const [modoVista, setModoVista] = useState<'crear' | 'editar' | null>(null);

    // Datos del cliente
    const [cedula, setCedula] = useState('');
    const [nombres, setNombres] = useState('');
    const [apellidos, setApellidos] = useState('');
    const [telefono, setTelefono] = useState('');
    const [direccion, setDireccion] = useState('');

    // Productos en la solicitud
    const [productosEnSolicitud, setProductosEnSolicitud] = useState<
        Array<{ idProducto: number; cantidadSolicitada: number; producto: Producto | null }>
    >([]);

    // Mensajes
    const [mensaje, setMensaje] = useState<{
        tipo: 'success' | 'error' | 'info';
        texto: string;
    } | null>(null);

    // ===========================
    // EFECTOS
    // ===========================

    useEffect(() => {
        cargarPedido();
    }, [hashPedido]);

    // ===========================
    // FUNCIONES
    // ===========================

    /**
     * Cargar información del pedido
     */
    const cargarPedido = async () => {
        try {
            setCargando(true);

            const response = await solicitudPublicaService.obtenerPedidoPorHash(hashPedido);

            if (response.status === 'success' && response.data) {
                const { idPedido, fechaCierre, productos: productosData } = response.data;

                setIdPedido(idPedido);
                setFechaCierre(fechaCierre);

                // Cargar información completa de cada producto
                const productosConInfo = await Promise.all(
                    productosData.map(async (p) => {
                        const productoInfo = await obtenerProductoPorId(p.idProducto);
                        return {
                            idProducto: p.idProducto,
                            producto: productoInfo,
                            cantidadMin: p.cantidadMin,
                            cantidadMax: p.cantidadMax,
                            cantidadSeleccionada: 0,
                        };
                    })
                );

                setProductos(productosConInfo);
            }
        } catch (error: unknown) {
            console.error('❌ Error:', error);
            const errorMessage = error instanceof Error ? error.message : 'Error al cargar el pedido';
            mostrarMensaje('error', errorMessage);
        } finally {
            setCargando(false);
        }
    };

    /**
     * Agregar producto a la solicitud
     */
    const agregarProducto = (idProducto: number) => {
        const producto = productos.find((p) => p.idProducto === idProducto);
        if (!producto || producto.cantidadSeleccionada === 0) {
            mostrarMensaje('error', 'Debe indicar una cantidad válida');
            return;
        }

        // Verificar si ya está en la solicitud
        const yaExiste = productosEnSolicitud.find((p) => p.idProducto === idProducto);
        if (yaExiste) {
            mostrarMensaje('error', 'Este producto ya está en la solicitud');
            return;
        }

        // Agregar a la solicitud
        setProductosEnSolicitud([
            ...productosEnSolicitud,
            {
                idProducto,
                cantidadSolicitada: producto.cantidadSeleccionada,
                producto: producto.producto,
            },
        ]);

        // Resetear cantidad seleccionada
        setProductos(
            productos.map((p) =>
                p.idProducto === idProducto ? { ...p, cantidadSeleccionada: 0 } : p
            )
        );

        setProductoSeleccionado(null);
        mostrarMensaje('success', '✅ Producto agregado a la solicitud');
    };

    /**
     * Eliminar producto de la solicitud
     */
    const eliminarProducto = (idProducto: number) => {
        setProductosEnSolicitud(productosEnSolicitud.filter((p) => p.idProducto !== idProducto));
        mostrarMensaje('info', 'Producto eliminado de la solicitud');
    };

    /**
     * Actualizar cantidad de producto
     */
    const actualizarCantidad = (idProducto: number, cantidad: number) => {
        const producto = productos.find((p) => p.idProducto === idProducto);
        if (!producto) return;

        // Validar rango
        if (cantidad < producto.cantidadMin || cantidad > producto.cantidadMax) {
            mostrarMensaje(
                'error',
                `La cantidad debe estar entre ${producto.cantidadMin} y ${producto.cantidadMax}`
            );
            return;
        }

        setProductos(
            productos.map((p) =>
                p.idProducto === idProducto ? { ...p, cantidadSeleccionada: cantidad } : p
            )
        );
    };

    /**
     * Validar formulario
     */
    const validarFormulario = (): boolean => {
        if (!cedula.trim() || cedula.length !== 10) {
            mostrarMensaje('error', 'La cédula debe tener 10 dígitos');
            return false;
        }

        if (!nombres.trim()) {
            mostrarMensaje('error', 'Los nombres son obligatorios');
            return false;
        }

        if (!apellidos.trim()) {
            mostrarMensaje('error', 'Los apellidos son obligatorios');
            return false;
        }

        if (!telefono.trim() || telefono.length !== 10) {
            mostrarMensaje('error', 'El teléfono debe tener 10 dígitos');
            return false;
        }

        if (!direccion.trim()) {
            mostrarMensaje('error', 'La dirección es obligatoria');
            return false;
        }

        if (productosEnSolicitud.length === 0) {
            mostrarMensaje('error', 'Debe agregar al menos un producto a la solicitud');
            return false;
        }

        return true;
    };

    /**
     * Crear solicitud
     */
    const crearSolicitud = async () => {
        if (!validarFormulario() || !idPedido) return;

        try {
            setEnviando(true);

            const productosParaEnviar: SolicitudProductoDTO[] = productosEnSolicitud.map((p) => ({
                idProducto: p.idProducto,
                cantidadSolicitada: p.cantidadSolicitada,
            }));

            const response = await solicitudPublicaService.crearSolicitudPublica({
                cedula: cedula.trim(),
                nombres: nombres.trim(),
                apellidos: apellidos.trim(),
                telefono: telefono.trim(),
                direccion: direccion.trim(),
                idPedido,
                productos: productosParaEnviar,
            });

            if (response.exito) {
                mostrarMensaje('success', '✅ Solicitud creada exitosamente');

                // Limpiar formulario después de 2 segundos y redirigir
                setTimeout(() => {
                    router.push('/');
                }, 2000);
            }
        } catch (error: unknown) {
            console.error('❌ Error:', error);
            const errorMessage = error instanceof Error ? error.message : 'Error al crear la solicitud';
            mostrarMensaje('error', errorMessage);
        } finally {
            setEnviando(false);
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
        return productosEnSolicitud.reduce((total, item) => {
            const precio = item.producto?.precioUnitario || 0;
            return total + precio * item.cantidadSolicitada;
        }, 0);
    };

    /**
     * Manejar selección de "Hacer Solicitud"
     */
    const manejarHacerSolicitud = () => {
        setModoVista('crear');
        setMostrarModalInicial(false);
    };

    /**
     * Manejar selección de "Editar Solicitudes"
     */
    const manejarEditarSolicitudes = () => {
        setModoVista('editar');
        setMostrarModalInicial(false);
        // Redirigir a la página de login OTP con el hash
        router.push(`/pedido/${hashPedido}/mis-solicitudes`);
    };

    // ===========================
    // RENDER
    // ===========================

    if (cargando) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gris-claro">
                <Loading />
            </div>
        );
    }

    return (
        <div className="solicitud-publica-container">
            {/* Modal de Selección Inicial */}
            {mostrarModalInicial && (
                <div className="modal-overlay">
                    <div className="modal-container">
                        <div className="modal-header-inicial">
                            <h2 className="modal-titulo">📦 Bienvenido</h2>
                            <p className="modal-descripcion">
                                ¿Qué deseas hacer?
                            </p>
                        </div>

                        <div className="modal-opciones">
                            <button
                                className="modal-opcion-btn opcion-crear"
                                onClick={manejarHacerSolicitud}
                            >
                                <div className="opcion-icono">📝</div>
                                <div className="opcion-contenido">
                                    <h3>Hacer Solicitud</h3>
                                    <p>Crea una nueva solicitud para este pedido</p>
                                </div>
                            </button>

                            <button
                                className="modal-opcion-btn opcion-editar"
                                onClick={manejarEditarSolicitudes}
                            >
                                <div className="opcion-icono">✏️</div>
                                <div className="opcion-contenido">
                                    <h3>Editar Solicitudes</h3>
                                    <p>Ver y modificar tus solicitudes existentes</p>
                                </div>
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Header */}
            <div className="solicitud-publica-header">
                <h1 className="solicitud-publica-titulo">📦 Realizar Solicitud</h1>
                <p className="solicitud-publica-descripcion">
                    Completa el formulario y selecciona los productos que deseas solicitar
                </p>
                {fechaCierre && (
                    <p className="solicitud-publica-fecha">
                        📅 Fecha límite: <strong>{new Date(fechaCierre).toLocaleDateString('es-EC')}</strong>
                    </p>
                )}
            </div>

            {/* Mensajes */}
            {mensaje && (
                <div className={`mensaje mensaje-${mensaje.tipo}`}>
                    {mensaje.texto}
                </div>
            )}

            {/* Sección 1: Datos del Cliente */}
            <div className="solicitud-seccion">
                <div className="seccion-header">
                    <h2 className="seccion-titulo">👤 Información Personal</h2>
                    <p className="seccion-descripcion">
                        Ingresa tus datos para procesar la solicitud
                    </p>
                </div>

                <div className="formulario-cliente">
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="cedula" className="form-label">
                                Cédula <span className="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="cedula"
                                className="form-input"
                                placeholder="1234567890"
                                value={cedula}
                                onChange={(e) => setCedula(e.target.value.replace(/\D/g, '').slice(0, 10))}
                                maxLength={10}
                                disabled={enviando}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="telefono" className="form-label">
                                Teléfono <span className="required">*</span>
                            </label>
                            <input
                                type="tel"
                                id="telefono"
                                className="form-input"
                                placeholder="0987654321"
                                value={telefono}
                                onChange={(e) => setTelefono(e.target.value.replace(/\D/g, '').slice(0, 10))}
                                maxLength={10}
                                disabled={enviando}
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="nombres" className="form-label">
                                Nombres <span className="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="nombres"
                                className="form-input"
                                placeholder="Juan Carlos"
                                value={nombres}
                                onChange={(e) => setNombres(e.target.value)}
                                disabled={enviando}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="apellidos" className="form-label">
                                Apellidos <span className="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="apellidos"
                                className="form-input"
                                placeholder="Pérez García"
                                value={apellidos}
                                onChange={(e) => setApellidos(e.target.value)}
                                disabled={enviando}
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="direccion" className="form-label">
                            Dirección de Entrega <span className="required">*</span>
                        </label>
                        <textarea
                            id="direccion"
                            className="form-textarea"
                            placeholder="Av. Principal N12-34 y Calle Secundaria, Edificio Torre, Depto 501"
                            value={direccion}
                            onChange={(e) => setDireccion(e.target.value)}
                            rows={3}
                            disabled={enviando}
                        />
                    </div>
                </div>
            </div>

            {/* Sección 2: Productos Disponibles */}
            <div className="solicitud-seccion">
                <div className="seccion-header">
                    <h2 className="seccion-titulo">🛍️ Productos Disponibles</h2>
                    <p className="seccion-descripcion">
                        Selecciona los productos y la cantidad que deseas
                    </p>
                </div>

                <div className="productos-grid">
                    {productos.map((item) => (
                        <div
                            key={item.idProducto}
                            className={`producto-card ${productoSeleccionado === item.idProducto ? 'producto-card-seleccionado' : ''}`}
                            onClick={() => setProductoSeleccionado(item.idProducto)}
                        >
                            {/* Imagen */}
                            {item.producto?.urlImagen ? (
                                <img
                                    src={item.producto.urlImagen}
                                    alt={item.producto.nombreProducto}
                                    className="producto-imagen"
                                />
                            ) : (
                                <div className="producto-sin-imagen">
                                    <span>📦</span>
                                </div>
                            )}

                            {/* Info */}
                            <div className="producto-info">
                                <h3 className="producto-nombre">{item.producto?.nombreProducto || 'Producto'}</h3>
                                <p className="producto-precio">
                                    ${item.producto?.precioUnitario.toFixed(2) || '0.00'}
                                </p>
                                <p className="producto-rango">
                                    Cantidad: {item.cantidadMin} - {item.cantidadMax} unidades
                                </p>

                                {/* Selector de cantidad */}
                                <div className="cantidad-selector">
                                    <label className="cantidad-label">Cantidad:</label>
                                    <input
                                        type="number"
                                        className="cantidad-input"
                                        min={item.cantidadMin}
                                        max={item.cantidadMax}
                                        value={item.cantidadSeleccionada || ''}
                                        onChange={(e) =>
                                            actualizarCantidad(item.idProducto, parseInt(e.target.value) || 0)
                                        }
                                        onClick={(e) => e.stopPropagation()}
                                    />
                                </div>

                                {/* Botón agregar */}
                                <button
                                    className="btn-agregar-producto"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        agregarProducto(item.idProducto);
                                    }}
                                    disabled={item.cantidadSeleccionada === 0}
                                >
                                    ➕ Agregar
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Sección 3: Resumen de la Solicitud */}
            {productosEnSolicitud.length > 0 && (
                <div className="solicitud-seccion">
                    <div className="seccion-header">
                        <h2 className="seccion-titulo">📋 Resumen de la Solicitud</h2>
                        <p className="seccion-descripcion">
                            Revisa los productos antes de confirmar
                        </p>
                    </div>

                    <div className="tabla-resumen-container">
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
                                {productosEnSolicitud.map((item) => (
                                    <tr key={item.idProducto}>
                                        <td>{item.producto?.nombreProducto || 'Producto'}</td>
                                        <td>{item.cantidadSolicitada}</td>
                                        <td>${item.producto?.precioUnitario.toFixed(2) || '0.00'}</td>
                                        <td>
                                            ${((item.producto?.precioUnitario || 0) * item.cantidadSolicitada).toFixed(2)}
                                        </td>
                                        <td>
                                            <button
                                                className="btn-eliminar-producto"
                                                onClick={() => eliminarProducto(item.idProducto)}
                                                title="Eliminar producto"
                                            >
                                                🗑️
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colSpan={3} className="total-label">
                                        <strong>TOTAL:</strong>
                                    </td>
                                    <td colSpan={2} className="total-valor">
                                        <strong>${calcularTotal().toFixed(2)}</strong>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>

                    {/* Botón Crear Solicitud */}
                    <div className="acciones-finales">
                        <button
                            className="btn-crear-solicitud"
                            onClick={crearSolicitud}
                            disabled={enviando}
                        >
                            {enviando ? '⏳ Creando solicitud...' : '✅ Crear Solicitud'}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
