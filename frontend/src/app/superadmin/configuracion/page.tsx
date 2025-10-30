/**
 * Configuraci�n - Super Administrador
 */

'use client';

import { useState } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { useAuthStore } from '@/stores/authStore';
import configuracionService from '@/services/configuracionService';

export default function ConfiguracionPage() {
    // ===========================
    // ESTADOS
    // ===========================
    const { user } = useAuthStore();
    const [contraseniaActual, setContraseniaActual] = useState('');
    const [contraseniaNueva, setContraseniaNueva] = useState('');
    const [confirmarContrasenia, setConfirmarContrasenia] = useState('');
    const [cargando, setCargando] = useState(false);
    const [mensaje, setMensaje] = useState<{
        tipo: 'success' | 'error' | 'info';
        texto: string;
    } | null>(null);
    const [mostrarActual, setMostrarActual] = useState(false);
    const [mostrarNueva, setMostrarNueva] = useState(false);
    const [mostrarConfirmar, setMostrarConfirmar] = useState(false);

    // ===========================
    // FUNCIONES
    // ===========================

    /**
     * Validar formulario
     */
    const validarFormulario = (): boolean => {
        if (!user?.cedula) {
            mostrarMensaje('error', 'No se pudo obtener la cédula del usuario');
            return false;
        }

        if (!contraseniaActual.trim()) {
            mostrarMensaje('error', 'La contraseña actual es obligatoria');
            return false;
        }

        if (!contraseniaNueva.trim()) {
            mostrarMensaje('error', 'La nueva contraseña es obligatoria');
            return false;
        }

        if (contraseniaNueva.length < 8) {
            mostrarMensaje('error', 'La nueva contraseña debe tener al menos 8 caracteres');
            return false;
        }

        if (contraseniaNueva !== confirmarContrasenia) {
            mostrarMensaje('error', 'Las contraseñas no coinciden');
            return false;
        }

        if (contraseniaActual === contraseniaNueva) {
            mostrarMensaje('error', 'La nueva contraseña debe ser diferente a la actual');
            return false;
        }

        return true;
    };

    /**
     * Manejar cambio de contrase�a
     */
    const handleCambiarContrasenia = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validarFormulario() || !user?.cedula) return;

        try {
            setCargando(true);

            await configuracionService.cambiarContrasenia({
                cedula: user.cedula,
                contraseniaActual,
                contraseniaNueva,
            });

            mostrarMensaje('success', '✅ Contraseña cambiada exitosamente');

            // Limpiar solo los campos de contraseña
            setContraseniaActual('');
            setContraseniaNueva('');
            setConfirmarContrasenia('');
        } catch (error: unknown) {
            
            const errorMessage = error instanceof Error ? error.message : 'Error al cambiar la contraseña';
            mostrarMensaje('error', errorMessage);
        } finally {
            setCargando(false);
        }
    };    /**
     * Mostrar mensaje temporal
     */
    const mostrarMensaje = (
        tipo: 'success' | 'error' | 'info',
        texto: string
    ) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 5000);
    };

    // ===========================
    // RENDER
    // ===========================

    return (
        <DashboardLayout role="SAD">
            <div className="configuracion-container">
                {/* Header */}
                <div className="configuracion-header">
                    <h1 className="configuracion-titulo">⚙️ Configuración</h1>
                    <p className="configuracion-descripcion">
                        Gestiona la configuración de tu cuenta
                    </p>
                </div>

                {/* Mensajes */}
                {mensaje && (
                    <div className={`mensaje mensaje-${mensaje.tipo}`}>
                        {mensaje.texto}
                    </div>
                )}

                {/* Formulario de Cambio de Contraseña */}
                <div className="configuracion-seccion">
                    <div className="seccion-header">
                        <h2 className="seccion-titulo">🔐 Cambiar Contraseña</h2>
                        <p className="seccion-descripcion">
                            Actualiza tu contraseña para mantener tu cuenta segura
                        </p>
                    </div>

                    <form onSubmit={handleCambiarContrasenia} className="formulario-contrasenia">
                        {/* Cédula (No editable) */}
                        <div className="form-group">
                            <label htmlFor="cedula" className="form-label">
                                Cédula
                            </label>
                            <input
                                type="text"
                                id="cedula"
                                className="form-input"
                                value={user?.cedula || ''}
                                disabled
                                readOnly
                            />
                        </div>

                        {/* Contrase�a Actual */}
                        <div className="form-group">
                            <label htmlFor="contraseniaActual" className="form-label">
                                Contraseña Actual <span className="required">*</span>
                            </label>
                            <div className="password-input-wrapper">
                                <input
                                    type={mostrarActual ? 'text' : 'password'}
                                    id="contraseniaActual"
                                    className="form-input"
                                    placeholder="Ingresa tu contraseña actual"
                                    value={contraseniaActual}
                                    onChange={(e) => setContraseniaActual(e.target.value)}
                                    disabled={cargando}
                                />
                                <button
                                    type="button"
                                    className="toggle-password"
                                    onClick={() => setMostrarActual(!mostrarActual)}
                                    tabIndex={-1}
                                >
                                    {mostrarActual ? '👁️' : '👁️‍🗨️'}
                                </button>
                            </div>
                        </div>

                        {/* Nueva Contraseña */}
                        <div className="form-group">
                            <label htmlFor="contraseniaNueva" className="form-label">
                                Nueva Contraseña <span className="required">*</span>
                            </label>
                            <div className="password-input-wrapper">
                                <input
                                    type={mostrarNueva ? 'text' : 'password'}
                                    id="contraseniaNueva"
                                    className="form-input"
                                    placeholder="Ingresa tu nueva contraseña"
                                    value={contraseniaNueva}
                                    onChange={(e) => setContraseniaNueva(e.target.value)}
                                    disabled={cargando}
                                />
                                <button
                                    type="button"
                                    className="toggle-password"
                                    onClick={() => setMostrarNueva(!mostrarNueva)}
                                    tabIndex={-1}
                                >
                                    {mostrarNueva ? '👁️' : '👁️‍🗨️'}
                                </button>
                            </div>
                            <small className="form-hint">
                                Mínimo 8 caracteres
                            </small>
                        </div>

                        {/* Confirmar Contraseña */}
                        <div className="form-group">
                            <label htmlFor="confirmarContrasenia" className="form-label">
                                Confirmar Nueva Contraseña <span className="required">*</span>
                            </label>
                            <div className="password-input-wrapper">
                                <input
                                    type={mostrarConfirmar ? 'text' : 'password'}
                                    id="confirmarContrasenia"
                                    className="form-input"
                                    placeholder="Confirma tu nueva contraseña"
                                    value={confirmarContrasenia}
                                    onChange={(e) => setConfirmarContrasenia(e.target.value)}
                                    disabled={cargando}
                                />
                                <button
                                    type="button"
                                    className="toggle-password"
                                    onClick={() => setMostrarConfirmar(!mostrarConfirmar)}
                                    tabIndex={-1}
                                >
                                    {mostrarConfirmar ? '👁️' : '👁️‍🗨️'}
                                </button>
                            </div>
                        </div>

                        {/* Botón de Envío */}
                        <div className="form-acciones">
                            <button
                                type="submit"
                                className="btn-cambiar-contrasenia"
                                disabled={cargando}
                            >
                                {cargando ? '⏳ Cambiando...' : '🔐 Cambiar Contraseña'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </DashboardLayout>
    );
}
