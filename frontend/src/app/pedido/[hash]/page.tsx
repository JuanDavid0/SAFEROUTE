'use client';

/**
 * Página Pública de Solicitud de Pedido
 * Vista para que clientes realicen solicitudes usando el hash del pedido
 */

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useToast } from '@/components/ui';
import { extractErrorInfo } from '@/utils/errorHandler';
import solicitudPublicaService from '@/services/solicitudPublicaService';
import { obtenerProductoPorIdPublico } from '@/services/productosService';
import type { Producto, SolicitudProductoDTO } from '@/types';
import { Loading, PrivacyPolicyModal } from '@/components/ui';

interface ProductoConInfo {
    idProducto: number;
    producto: Producto | null;
    cantidadMin: number;
    cantidadMax: number;
    cantidadSeleccionada: number;
}

export default function PedidoPublicoPage() {
    const { showSuccess, showError, showInfo, showWarning } = useToast();
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

    // Modal de políticas de privacidad
    const [mostrarModalPrivacidad, setMostrarModalPrivacidad] = useState(false);
    const [aceptaPoliticas, setAceptaPoliticas] = useState(false);
    const [aceptaTerminos, setAceptaTerminos] = useState(false);

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
                        const productoInfo = await obtenerProductoPorIdPublico(p.idProducto);
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
            console.error('Error:', error);
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
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
            showError('Cantidad Inválida', 'Debe indicar una cantidad válida');
            return;
        }

        // Validar cantidad mínima
        if (producto.cantidadSeleccionada < producto.cantidadMin) {
            showError(
                'Cantidad Insuficiente',
                `La cantidad debe ser al menos ${producto.cantidadMin}`
            );
            return;
        }

        // Validar cantidad máxima solo si existe
        if (producto.cantidadMax !== null && producto.cantidadSeleccionada > producto.cantidadMax) {
            showError(
                'Cantidad Excedida',
                `La cantidad no puede exceder ${producto.cantidadMax}`
            );
            return;
        }

        // Verificar si ya está en la solicitud
        const yaExiste = productosEnSolicitud.find((p) => p.idProducto === idProducto);
        if (yaExiste) {
            showError('Producto Duplicado', 'Este producto ya está en la solicitud');
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
        showSuccess('Producto Agregado', 'Producto agregado a la solicitud');
    };

    /**
     * Eliminar producto de la solicitud
     */
    const eliminarProducto = (idProducto: number) => {
        setProductosEnSolicitud(productosEnSolicitud.filter((p) => p.idProducto !== idProducto));
        showInfo('Producto Eliminado', 'Producto eliminado de la solicitud');
    };

    /**
     * Actualizar cantidad de producto (sin validación, solo actualiza el estado)
     */
    const actualizarCantidad = (idProducto: number, cantidad: number) => {
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
        if (!cedula.trim() || cedula.length < 8 || cedula.length > 10) {
            showError('Cédula Inválida', 'La cédula debe tener entre 8 y 10 dígitos');
            return false;
        }

        if (!nombres.trim()) {
            showError('Nombres Requeridos', 'Los nombres son obligatorios');
            return false;
        }

        if (!apellidos.trim()) {
            showError('Apellidos Requeridos', 'Los apellidos son obligatorios');
            return false;
        }

        if (!telefono.trim() || telefono.length !== 10) {
            showError('Teléfono Inválido', 'El teléfono debe tener 10 dígitos');
            return false;
        }

        if (!direccion.trim()) {
            showError('Dirección Requerida', 'La dirección es obligatoria');
            return false;
        }

        if (productosEnSolicitud.length === 0) {
            showError('Sin Productos', 'Debe agregar al menos un producto a la solicitud');
            return false;
        }

        return true;
    };

    /**
     * Abrir modal de políticas de privacidad antes de crear solicitud
     */
    const iniciarCreacionSolicitud = () => {
        if (!validarFormulario()) return;
        
        // Abrir modal de políticas de privacidad
        setMostrarModalPrivacidad(true);
    };

    /**
     * Manejar aceptación de políticas y crear solicitud
     */
    const manejarAceptacionPoliticas = (politicas: boolean, terminos: boolean) => {
        setAceptaPoliticas(politicas);
        setAceptaTerminos(terminos);
        setMostrarModalPrivacidad(false);

        // Si ambos están aceptados, crear la solicitud
        if (politicas && terminos) {
            crearSolicitud(politicas, terminos);
        } else {
            showError(
                'Consentimiento Requerido',
                'Debe aceptar las Políticas de Privacidad y los Términos y Condiciones para continuar'
            );
        }
    };

    /**
     * Crear solicitud con los consentimientos
     */
    const crearSolicitud = async (politicas: boolean, terminos: boolean) => {
        if (!idPedido) return;

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
                aceptaPoliticas: politicas,
                aceptaTerminos: terminos,
            });

            // Verificar si la solicitud fue exitosa
            if (response.exito || (response as any).status === 'success') {
                showSuccess('Solicitud Creada', 'Solicitud creada exitosamente');

                // Recargar la página después de 2 segundos para mostrar el Toast
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                // Si no hay exito pero tampoco error, mostrar advertencia
                showWarning('Atención', response.mensaje || 'La solicitud se procesó pero verifique el resultado');
            }
        } catch (error: unknown) {
            console.error('Error:', error);
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setEnviando(false);
        }
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
                            <h2 className="modal-titulo">Bienvenido</h2>
                            <p className="modal-descripcion">
                                ¿Qué deseas hacer?
                            </p>
                        </div>

                        <div className="modal-opciones">
                            <button
                                className="modal-opcion-btn opcion-crear"
                                onClick={manejarHacerSolicitud}
                            >
                                <div className="opcion-contenido">
                                    <h3>Hacer Solicitud</h3>
                                    <p>Crea una nueva solicitud para este pedido</p>
                                </div>
                            </button>

                            <button
                                className="modal-opcion-btn opcion-editar"
                                onClick={manejarEditarSolicitudes}
                            >
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
                <h1 className="solicitud-publica-titulo">Realizar Solicitud</h1>
                <p className="solicitud-publica-descripcion">
                    Completa el formulario y selecciona los productos que deseas solicitar
                </p>
                {fechaCierre && (
                    <p className="solicitud-publica-fecha">
                        Fecha límite: <strong>{new Date(fechaCierre).toLocaleDateString('es-EC')}</strong>
                    </p>
                )}
            </div>

            {/* Mensajes */}
            {/* Los mensajes ahora se muestran mediante el Toast system */}

            {/* Sección 1: Datos del Cliente */}
            <div className="solicitud-seccion">
                <div className="seccion-header">
                    <h2 className="seccion-titulo">Información Personal</h2>
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
                                placeholder="12345678"
                                value={cedula}
                                onChange={(e) => setCedula(e.target.value.replace(/\D/g, '').slice(0, 10))}
                                maxLength={10}
                                minLength={8}
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
                    <h2 className="seccion-titulo">Productos Disponibles</h2>
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
                                    <span>Sin imagen</span>
                                </div>
                            )}

                            {/* Info */}
                            <div className="producto-info">
                                <h3 className="producto-nombre">{item.producto?.nombreProducto || 'Producto'}</h3>
                                <p className="producto-precio">
                                    ${item.producto?.precioUnitario.toFixed(2) || '0.00'}
                                </p>
                                <p className="producto-rango">
                                    Cantidad: {item.cantidadMin}{item.cantidadMax !== null ? ` - ${item.cantidadMax}` : '+'} unidades
                                </p>

                                {/* Selector de cantidad */}
                                <div className="cantidad-selector">
                                    <label className="cantidad-label">Cantidad:</label>
                                    <input
                                        type="text"
                                        inputMode="numeric"
                                        pattern="[0-9]*"
                                        className="cantidad-input"
                                        value={item.cantidadSeleccionada || ''}
                                        onChange={(e) => {
                                            const valor = e.target.value.replace(/\D/g, '');
                                            const numero = valor ? parseInt(valor) : 0;
                                            actualizarCantidad(item.idProducto, numero);
                                        }}
                                        onClick={(e) => e.stopPropagation()}
                                        placeholder="0"
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
                                    Agregar
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
                        <h2 className="seccion-titulo">Resumen de la Solicitud</h2>
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
                                                Eliminar
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
                            onClick={iniciarCreacionSolicitud}
                            disabled={enviando}
                        >
                            {enviando ? 'Creando solicitud...' : 'Crear Solicitud'}
                        </button>
                    </div>
                </div>
            )}

            {/* Modal de Políticas de Privacidad */}
            <PrivacyPolicyModal
                isOpen={mostrarModalPrivacidad}
                onClose={() => setMostrarModalPrivacidad(false)}
                onAccept={manejarAceptacionPoliticas}
            />
        </div>
    );
}
