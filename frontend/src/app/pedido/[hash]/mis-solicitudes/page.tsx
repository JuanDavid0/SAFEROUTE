'use client';

/**
 * Página de Gestión de Solicitudes del Cliente
 * Login OTP y CRUD de solicitudes
 */

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useToast } from '@/components/ui';
import { extractErrorInfo } from '@/utils/errorHandler';
import { solicitarOtp, verificarOtp } from '@/services/otpService';
import {
    obtenerMisSolicitudes,
    cancelarSolicitud,
    type SolicitudCliente,
} from '@/services/solicitudClienteService';
import { Loading } from '@/components/ui';

export default function MisSolicitudesPage() {
    const { showSuccess, showError, showInfo } = useToast();
    const params = useParams();
    const router = useRouter();
    const hashPedido = params.hash as string;

    // ===========================
    // ESTADOS
    // ===========================

    // Estado de autenticación
    const [autenticado, setAutenticado] = useState(false);
    const [tokenOtp, setTokenOtp] = useState<string | null>(null);

    // Estados del formulario OTP
    const [cedula, setCedula] = useState('');
    const [codigoOtp, setCodigoOtp] = useState('');
    const [otpSolicitado, setOtpSolicitado] = useState(false);
    const [intentosRestantes, setIntentosRestantes] = useState(3);
    const [tiempoRestante, setTiempoRestante] = useState(0);

    // Estados de carga y mensajes
    const [cargando, setCargando] = useState(false);

    // Lista de solicitudes
    const [solicitudes, setSolicitudes] = useState<SolicitudCliente[]>([]);
    const [cargandoSolicitudes, setCargandoSolicitudes] = useState(false);

    // Modal de confirmación
    const [modalCancelar, setModalCancelar] = useState<{
        visible: boolean;
        idSolicitud: number | null;
    }>({ visible: false, idSolicitud: null });

    // ===========================
    // EFECTOS
    // ===========================

    // Recuperar sesión OTP al cargar la página
    useEffect(() => {
        const sesionOtp = localStorage.getItem(`otp_session_${hashPedido}`);
        if (sesionOtp) {
            try {
                const datos = JSON.parse(sesionOtp);
                const ahora = Date.now();

                // Verificar si la sesión no ha expirado (válida por 1 hora)
                if (datos.expiracion && ahora < datos.expiracion) {
                    setTokenOtp(datos.token);
                    setAutenticado(true);
                    setCedula(datos.cedula);
                } else {
                    // Sesión expirada, limpiar
                    localStorage.removeItem(`otp_session_${hashPedido}`);
                }
            } catch (error) {
                console.error('Error al recuperar sesión OTP:', error);
                localStorage.removeItem(`otp_session_${hashPedido}`);
            }
        }

        // Limpiar sesión cuando se cierre la pestaña/navegador
        const handleBeforeUnload = () => {
            // Comentar esta línea si quieres que persista incluso al cerrar el navegador
            // localStorage.removeItem(`otp_session_${hashPedido}`);
        };

        window.addEventListener('beforeunload', handleBeforeUnload);
        return () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
        };
    }, [hashPedido]);

    // Temporizador del OTP
    useEffect(() => {
        if (tiempoRestante > 0) {
            const timer = setTimeout(() => {
                setTiempoRestante(tiempoRestante - 1);
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [tiempoRestante]);

    // Cargar solicitudes cuando se autentica
    useEffect(() => {
        if (autenticado && tokenOtp) {
            cargarSolicitudes();
        }
    }, [autenticado, tokenOtp]);

    // ===========================
    // FUNCIONES
    // ===========================

    /**
     * Solicitar código OTP
     */
    const handleSolicitarOtp = async () => {
        if (!cedula || cedula.length < 8 || cedula.length > 10) {
            showError('Cédula Inválida', 'Ingresa una cédula válida de 8 a 10 dígitos');
            return;
        }

        setCargando(true);
        try {
            const response = await solicitarOtp(cedula);

            if (response.data.exito) {
                setOtpSolicitado(true);
                setTiempoRestante(300); // 5 minutos
                showSuccess('Código Enviado', response.data.mensaje);
            } else {
                showError('Error al Enviar', response.data.mensaje);
            }
        } catch (error: any) {
            console.error('Error al solicitar OTP:', error);
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    /**
     * Verificar código OTP
     */
    const handleVerificarOtp = async () => {
        if (!codigoOtp || codigoOtp.length !== 6) {
            showError('Código Inválido', 'Ingresa el código de 6 dígitos');
            return;
        }

        if (intentosRestantes <= 0) {
            showError('Intentos Agotados', 'Has agotado los intentos. Solicita un nuevo código');
            setOtpSolicitado(false);
            setCodigoOtp('');
            setIntentosRestantes(3);
            return;
        }

        setCargando(true);
        try {
            const response = await verificarOtp(cedula, codigoOtp);

            if (response.data.exito && response.data.token) {
                setTokenOtp(response.data.token);
                setAutenticado(true);

                // Guardar sesión en localStorage (válida por 1 hora)
                const sesionOtp = {
                    token: response.data.token,
                    cedula: cedula,
                    expiracion: Date.now() + (60 * 60 * 1000), // 1 hora
                };
                localStorage.setItem(`otp_session_${hashPedido}`, JSON.stringify(sesionOtp));

                showSuccess('Autenticación Exitosa', 'Has iniciado sesión correctamente');
            } else {
                setIntentosRestantes(intentosRestantes - 1);
                showError(
                    'Código Incorrecto',
                    `Te quedan ${intentosRestantes - 1} intentos`
                );
            }
        } catch (error: any) {
            console.error('❌ Error al verificar OTP:', error);
            setIntentosRestantes(intentosRestantes - 1);
            const errorInfo = extractErrorInfo(error);
            showError(
                errorInfo.message,
                `${errorInfo.details}. Intentos restantes: ${intentosRestantes - 1}`,
                errorInfo.errorCode
            );

            if (intentosRestantes - 1 <= 0) {
                setTimeout(() => {
                    setOtpSolicitado(false);
                    setCodigoOtp('');
                    setIntentosRestantes(3);
                }, 2000);
            }
        } finally {
            setCargando(false);
        }
    };

    /**
     * Cargar solicitudes del cliente
     */
    const cargarSolicitudes = async () => {
        if (!tokenOtp) return;

        setCargandoSolicitudes(true);
        try {
            const response = await obtenerMisSolicitudes(hashPedido, tokenOtp);
            setSolicitudes(response.data);
        } catch (error: any) {
            console.error('❌ Error al cargar solicitudes:', error);
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargandoSolicitudes(false);
        }
    };

    /**
     * Confirmar cancelación de solicitud
     */
    const confirmarCancelar = (idSolicitud: number) => {
        setModalCancelar({ visible: true, idSolicitud });
    };

    /**
     * Cancelar solicitud
     */
    const handleCancelarSolicitud = async () => {
        if (!modalCancelar.idSolicitud || !tokenOtp) return;

        setCargando(true);
        try {
            await cancelarSolicitud(modalCancelar.idSolicitud, tokenOtp);
            showSuccess('Solicitud Cancelada', 'Solicitud cancelada exitosamente');
            setModalCancelar({ visible: false, idSolicitud: null });
            cargarSolicitudes(); // Recargar lista
        } catch (error: any) {
            console.error('❌ Error al cancelar solicitud:', error);
            const errorInfo = extractErrorInfo(error);
            showError(errorInfo.message, errorInfo.details, errorInfo.errorCode);
        } finally {
            setCargando(false);
        }
    };

    /**
     * Redirigir a editar solicitud
     */
    const handleEditarSolicitud = (solicitud: SolicitudCliente) => {
        // Guardar el token y los datos de la solicitud en sessionStorage
        if (tokenOtp) {
            sessionStorage.setItem('otpToken', tokenOtp);
            sessionStorage.setItem('solicitudEditar', JSON.stringify(solicitud));

            // Asegurar que también existe en localStorage para persistencia
            const sesionOtp = localStorage.getItem(`otp_session_${hashPedido}`);
            if (!sesionOtp) {
                // Si por alguna razón no está en localStorage, guardarlo
                const nuevaSesion = {
                    token: tokenOtp,
                    cedula: cedula,
                    expiracion: Date.now() + (60 * 60 * 1000), // 1 hora
                };
                localStorage.setItem(`otp_session_${hashPedido}`, JSON.stringify(nuevaSesion));
            }

            router.push(`/pedido/${hashPedido}/editar-solicitud/${solicitud.idSolicitud}`);
        } else {
            showError('Sesión Expirada', 'Por favor, autentícate nuevamente.');
        }
    };

    /**
     * Cerrar sesión OTP
     */
    const handleCerrarSesion = () => {
        localStorage.removeItem(`otp_session_${hashPedido}`);
        sessionStorage.removeItem('otpToken');
        sessionStorage.removeItem('solicitudEditar');
        setTokenOtp(null);
        setAutenticado(false);
        setCedula('');
        setOtpSolicitado(false);
        setCodigoOtp('');
        setSolicitudes([]);
        showInfo('Sesión Cerrada', 'Sesión cerrada exitosamente');
    };

    /**
     * Formatear tiempo restante
     */
    const formatearTiempo = (segundos: number): string => {
        const mins = Math.floor(segundos / 60);
        const secs = segundos % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    /**
     * Obtener color del estado
     */
    const obtenerColorEstado = (estado: string): string => {
        const colores: { [key: string]: string } = {
            PDP: '#FFA500', // Pendiente de pago - Naranja
            PGD: '#6BBF8E', // Pagado - Verde
            APB: '#1F4E5F', // Aprobado - Azul petróleo
            CAN: '#E46B6B', // Cancelado - Coral rojo
            RCH: '#F21D1D', // Rechazado - Rojo intenso
            ENT: '#6BBF8E', // Entregado - Verde
        };
        return colores[estado] || '#404040';
    };

    /**
     * Obtener nombre del estado
     */
    const obtenerNombreEstado = (estado: string): string => {
        const nombres: { [key: string]: string } = {
            PDP: 'Pendiente de Pago',
            PGD: 'Pagado',
            APB: 'Aprobado',
            CAN: 'Cancelado',
            RCH: 'Rechazado',
            ENT: 'Entregado',
        };
        return nombres[estado] || estado;
    };

    // ===========================
    // RENDER
    // ===========================

    return (
        <div className="solicitud-publica-container">
            {/* Header */}
            <div className="solicitud-publica-header">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 className="solicitud-publica-titulo">
                            {autenticado ? 'Mis Solicitudes' : ' Acceso a Solicitudes'}
                        </h1>
                        <p className="solicitud-publica-descripcion">
                            {autenticado
                                ? 'Gestiona tus solicitudes existentes'
                                : 'Ingresa tu cédula para acceder a tus solicitudes'}
                        </p>
                    </div>
                    {autenticado && (
                        <button
                            className="btn-cerrar-sesion"
                            onClick={handleCerrarSesion}
                            title="Cerrar sesión"
                        >
                            🚪 Cerrar Sesión
                        </button>
                    )}
                </div>
            </div>

            {/* Mensajes */}
            {/* Los mensajes ahora se muestran mediante el Toast system */}

            {/* Login OTP */}
            {!autenticado && (
                <div className="otp-card">
                    <div className="otp-header">
                        <h2 className="otp-titulo"> Verificación de Identidad</h2>
                        <p className="otp-subtitulo">
                            {!otpSolicitado
                                ? 'Ingresa tu cédula para recibir un código de verificación'
                                : 'Ingresa el código enviado a tu teléfono'}
                        </p>
                    </div>

                    <div className="otp-form">
                        {/* Paso 1: Solicitar OTP */}
                        {!otpSolicitado && (
                            <>
                                <div className="otp-form-group">
                                    <label htmlFor="cedula" className="otp-label">
                                        Cédula <span className="required">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        id="cedula"
                                        className="otp-input"
                                        placeholder="12345678"
                                        value={cedula}
                                        onChange={(e) =>
                                            setCedula(e.target.value.replace(/\D/g, '').slice(0, 10))
                                        }
                                        maxLength={10}
                                        minLength={8}
                                        disabled={cargando}
                                    />
                                </div>
                                <button
                                    className="otp-btn primary"
                                    onClick={handleSolicitarOtp}
                                    disabled={cargando || cedula.length < 8 || cedula.length > 10}
                                >
                                    {cargando ? (
                                        <>⏳ Enviando...</>
                                    ) : (
                                        <>📲 Enviar Código</>
                                    )}
                                </button>
                            </>
                        )}

                        {/* Paso 2: Verificar OTP */}
                        {otpSolicitado && (
                            <>
                                {tiempoRestante > 0 && (
                                    <div className="otp-timer">
                                        ⏱️ Código válido por: {formatearTiempo(tiempoRestante)}
                                    </div>
                                )}

                                <div className="otp-intentos">
                                     Intentos restantes: <strong>{intentosRestantes}</strong>
                                </div>

                                <div className="otp-form-group">
                                    <label htmlFor="codigoOtp" className="otp-label">
                                        Código de Verificación <span className="required">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        id="codigoOtp"
                                        className="otp-input codigo-input"
                                        placeholder="123456"
                                        value={codigoOtp}
                                        onChange={(e) =>
                                            setCodigoOtp(e.target.value.replace(/\D/g, '').slice(0, 6))
                                        }
                                        maxLength={6}
                                        disabled={cargando || tiempoRestante === 0}
                                        autoFocus
                                    />
                                </div>

                                <button
                                    className="otp-btn primary"
                                    onClick={handleVerificarOtp}
                                    disabled={cargando || codigoOtp.length !== 6 || tiempoRestante === 0}
                                >
                                    {cargando ? (
                                        <>Verificando...</>
                                    ) : (
                                        <>Verificar Código</>
                                    )}
                                </button>

                                <button
                                    className="otp-btn secondary"
                                    onClick={() => {
                                        setOtpSolicitado(false);
                                        setCodigoOtp('');
                                        setIntentosRestantes(3);
                                    }}
                                    disabled={cargando}
                                >
                                    Solicitar Nuevo Código
                                </button>
                            </>
                        )}
                    </div>

                    {/* Botón volver */}
                    <div className="otp-volver">
                        <a
                            className="otp-link"
                            onClick={() => router.push(`/pedido/${hashPedido}`)}
                            style={{ cursor: 'pointer' }}
                        >
                            ← Volver al formulario
                        </a>
                    </div>
                </div>
            )}

            {/* Lista de Solicitudes */}
            {autenticado && (
                <div className="solicitud-seccion">
                    <div className="seccion-header">
                        <h2 className="seccion-titulo">Tus Solicitudes</h2>
                        <p className="seccion-descripcion">
                            {solicitudes.length > 0
                                ? `Tienes ${solicitudes.length} solicitud(es) registrada(s)`
                                : 'No tienes solicitudes registradas'}
                        </p>
                    </div>

                    {cargandoSolicitudes ? (
                        <div className="flex items-center justify-center py-8">
                            <Loading />
                        </div>
                    ) : solicitudes.length === 0 ? (
                        <div className="sin-solicitudes">
                            <p>📭 No se encontraron solicitudes para este pedido</p>
                        </div>
                    ) : (
                        <div className="solicitudes-grid">
                            {solicitudes.map((solicitud) => (
                                <div key={solicitud.idSolicitud} className="solicitud-card">
                                    {/* Header de la card */}
                                    <div className="solicitud-card-header">
                                        <div className="solicitud-numero">
                                            Solicitud #{solicitud.idSolicitud}
                                        </div>
                                        <div
                                            className="solicitud-estado"
                                            style={{
                                                backgroundColor: obtenerColorEstado(solicitud.estadoSolicitud),
                                            }}
                                        >
                                            {obtenerNombreEstado(solicitud.estadoSolicitud)}
                                        </div>
                                    </div>

                                    {/* Información de la solicitud */}
                                    <div className="solicitud-card-body">
                                        <div className="solicitud-info-item">
                                            <span className="info-label">📅 Fecha:</span>
                                            <span className="info-valor">
                                                {new Date(solicitud.fechaSolicitud).toLocaleDateString('es-EC')}
                                            </span>
                                        </div>

                                        <div className="solicitud-info-item">
                                            <span className="info-label">📍 Dirección:</span>
                                            <span className="info-valor">{solicitud.direccionEntrega}</span>
                                        </div>

                                        <div className="solicitud-info-item">
                                            <span className="info-label">💰 Total:</span>
                                            <span className="info-valor total-precio">
                                                $
                                                {solicitud.productos
                                                    .reduce((sum, p) => sum + p.precio, 0)
                                                    .toFixed(2)}
                                            </span>
                                        </div>

                                        <div className="solicitud-info-item">
                                            <span className="info-label"> Modificaciones restantes:</span>
                                            <span className="info-valor">
                                                {solicitud.modificacionesRestantes}
                                            </span>
                                        </div>

                                        <div className="solicitud-info-item">
                                            <span className="info-label">⏰ Límite de pago:</span>
                                            <span className="info-valor">
                                                {new Date(solicitud.fechaLimitePago).toLocaleDateString('es-EC')}
                                            </span>
                                        </div>

                                        {/* Productos */}
                                        <div className="solicitud-productos">
                                            <strong className="productos-titulo">Productos:</strong>
                                            <ul className="productos-lista">
                                                {solicitud.productos.map((producto) => (
                                                    <li key={producto.idProducto} className="producto-item">
                                                        <span>
                                                            {producto.nombreProducto || `Producto #${producto.idProducto}`}
                                                        </span>
                                                        <span className="producto-cantidad">
                                                            x{producto.cantidadSolicitada}
                                                        </span>
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    </div>

                                    {/* Acciones */}
                                    {solicitud.estadoSolicitud === 'PDP' && (
                                        <div className="solicitud-card-acciones">
                                            <button
                                                className="btn-editar-solicitud"
                                                onClick={() => handleEditarSolicitud(solicitud)}
                                            >
                                                Modificar
                                            </button>
                                            <button
                                                className="btn-cancelar-solicitud"
                                                onClick={() => confirmarCancelar(solicitud.idSolicitud)}
                                            >
                                                Cancelar
                                            </button>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Modal de Confirmación Cancelar */}
            {modalCancelar.visible && (
                <div className="modal-overlay">
                    <div className="modal-container modal-confirmacion">
                        <div className="modal-header">
                            <h3 className="modal-titulo">⚠️ Confirmar Cancelación</h3>
                        </div>
                        <div className="modal-body">
                            <p>
                                ¿Estás seguro de que deseas cancelar la solicitud #
                                {modalCancelar.idSolicitud}?
                            </p>
                            <p className="text-sm text-gray-600">Esta acción no se puede deshacer.</p>
                        </div>
                        <div className="modal-acciones">
                            <button
                                className="btn-modal-cancelar"
                                onClick={() => setModalCancelar({ visible: false, idSolicitud: null })}
                                disabled={cargando}
                            >
                                No, volver
                            </button>
                            <button
                                className="btn-modal-confirmar"
                                onClick={handleCancelarSolicitud}
                                disabled={cargando}
                            >
                                {cargando ? '⏳ Cancelando...' : 'Sí, cancelar'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
