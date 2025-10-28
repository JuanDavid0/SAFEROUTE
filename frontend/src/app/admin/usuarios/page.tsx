'use client';

/**
 * Página de Gestión de Usuarios - ADMIN
 * Permite ver y eliminar administradores del sistema
 */

import { useEffect, useState } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import usuariosService, { Usuario } from '@/services/usuariosService';

export default function UsuariosPage() {
    // ===========================
    // ESTADOS
    // ===========================
    const [usuarios, setUsuarios] = useState<Usuario[]>([]);
    const [usuarioSeleccionado, setUsuarioSeleccionado] = useState<Usuario | null>(
        null
    );
    const [mostrarModal, setMostrarModal] = useState<boolean>(false);
    const [cargando, setCargando] = useState<boolean>(false);
    const [mensaje, setMensaje] = useState<{
        tipo: 'success' | 'error' | 'info';
        texto: string;
    } | null>(null);

    // ===========================
    // EFECTOS
    // ===========================

    // Cargar usuarios al montar el componente
    useEffect(() => {
        cargarUsuarios();
    }, []);

    // ===========================
    // FUNCIONES
    // ===========================

    /**
     * Cargar lista de usuarios
     */
    const cargarUsuarios = async () => {
        try {
            setCargando(true);
            const data = await usuariosService.obtenerUsuarios();

            console.log('👥 Usuarios cargados:', data);
            console.log('👥 Total:', data.length);

            // Validar que sea un array
            if (!Array.isArray(data)) {
                console.error('❌ La respuesta no es un array:', data);
                setUsuarios([]);
                mostrarMensaje(
                    'error',
                    'Error: La respuesta del servidor no tiene el formato esperado'
                );
                return;
            }

            // Filtrar solo administradores (ADM)
            const administradores = data.filter((usuario) =>
                usuario.roles.includes('ADM')
            );

            setUsuarios(administradores);

            if (administradores.length === 0) {
                mostrarMensaje('info', 'No hay administradores registrados');
            }
        } catch (error: any) {
            console.error('❌ Error al cargar usuarios:', error);
            setUsuarios([]);
            mostrarMensaje(
                'error',
                error.message || 'Error al cargar los usuarios'
            );
        } finally {
            setCargando(false);
        }
    };

    /**
     * Abrir modal de confirmación para eliminar
     */
    const handleAbrirModalEliminar = (usuario: Usuario) => {
        setUsuarioSeleccionado(usuario);
        setMostrarModal(true);
    };

    /**
     * Cerrar modal de confirmación
     */
    const handleCerrarModal = () => {
        setUsuarioSeleccionado(null);
        setMostrarModal(false);
    };

    /**
     * Eliminar usuario
     */
    const handleEliminarUsuario = async () => {
        if (!usuarioSeleccionado) return;

        try {
            setCargando(true);
            await usuariosService.eliminarUsuario(usuarioSeleccionado.idUsuario);

            mostrarMensaje(
                'success',
                `✅ Administrador ${usuarioSeleccionado.nombres} ${usuarioSeleccionado.apellidos} eliminado exitosamente`
            );

            // Recargar lista de usuarios
            await cargarUsuarios();

            // Cerrar modal
            handleCerrarModal();
        } catch (error: any) {
            console.error('❌ Error al eliminar usuario:', error);
            mostrarMensaje('error', error.message || 'Error al eliminar el usuario');
        } finally {
            setCargando(false);
        }
    };

    /**
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
        <DashboardLayout role="ADM">
            <div className="usuarios-container">
                {/* Header */}
                <div className="usuarios-header">
                    <h1 className="usuarios-titulo">👥 Gestión de Usuarios</h1>
                    <p className="usuarios-descripcion">
                        Administra los usuarios administradores del sistema
                    </p>
                </div>

                {/* Mensajes */}
                {mensaje && (
                    <div className={`mensaje mensaje-${mensaje.tipo}`}>
                        {mensaje.texto}
                    </div>
                )}

                {/* Contenido principal */}
                <div className="usuarios-contenido">
                    <div className="usuarios-seccion">
                        <h2 className="usuarios-subtitulo">
                            Administradores Registrados
                        </h2>

                        {cargando ? (
                            <div className="usuarios-loading">⏳ Cargando usuarios...</div>
                        ) : usuarios.length === 0 ? (
                            <div className="usuarios-empty">
                                No hay administradores registrados
                            </div>
                        ) : (
                            <div className="tabla-container">
                                <table className="tabla-usuarios">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Cédula</th>
                                            <th>Nombres</th>
                                            <th>Apellidos</th>
                                            <th>Teléfono</th>
                                            <th>Dirección</th>
                                            <th>Roles</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {usuarios.map((usuario) => (
                                            <tr key={usuario.idUsuario}>
                                                <td>#{usuario.idUsuario}</td>
                                                <td>{usuario.cedula}</td>
                                                <td>{usuario.nombres}</td>
                                                <td>{usuario.apellidos}</td>
                                                <td>{usuario.telefono || 'N/A'}</td>
                                                <td>{usuario.direccion || 'N/A'}</td>
                                                <td>
                                                    <div className="roles-container">
                                                        {usuario.roles.map((rol) => (
                                                            <span
                                                                key={rol}
                                                                className={`badge-rol badge-rol-${rol.toLowerCase()}`}
                                                            >
                                                                {rol}
                                                            </span>
                                                        ))}
                                                    </div>
                                                </td>
                                                <td>
                                                    <button
                                                        className="btn-eliminar"
                                                        onClick={() =>
                                                            handleAbrirModalEliminar(usuario)
                                                        }
                                                    >
                                                        🗑️ Eliminar
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>

                {/* Modal de confirmación */}
                {mostrarModal && usuarioSeleccionado && (
                    <div className="modal-overlay" onClick={handleCerrarModal}>
                        <div
                            className="modal-contenido"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="modal-header">
                                <h2>⚠️ Confirmar Eliminación</h2>
                            </div>

                            <div className="modal-body">
                                <p>
                                    ¿Estás seguro de que deseas eliminar al administrador?
                                </p>
                                <div className="usuario-info">
                                    <p>
                                        <strong>Nombre:</strong>{' '}
                                        {usuarioSeleccionado.nombres}{' '}
                                        {usuarioSeleccionado.apellidos}
                                    </p>
                                    <p>
                                        <strong>Cédula:</strong> {usuarioSeleccionado.cedula}
                                    </p>
                                    <p>
                                        <strong>ID:</strong> #{usuarioSeleccionado.idUsuario}
                                    </p>
                                </div>
                                <p className="advertencia">
                                    ⚠️ Esta acción no se puede deshacer
                                </p>
                            </div>

                            <div className="modal-footer">
                                <button
                                    className="btn-cancelar"
                                    onClick={handleCerrarModal}
                                    disabled={cargando}
                                >
                                    Cancelar
                                </button>
                                <button
                                    className="btn-confirmar-eliminar"
                                    onClick={handleEliminarUsuario}
                                    disabled={cargando}
                                >
                                    {cargando ? '⏳ Eliminando...' : '🗑️ Eliminar'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </DashboardLayout>
    );
}
