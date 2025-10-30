'use client';

/**
 * Página de Gestión de Usuarios - SUPERADMIN
 * Permite ver y eliminar administradores del sistema
 */

import { useEffect, useState } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import usuariosService, { Usuario } from '@/services/usuariosService';
import authService from '@/services/authService';

export default function UsuariosPage() {
    // ===========================
    // ESTADOS - LISTA DE USUARIOS
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
    // ESTADOS - CREAR ADMINISTRADOR
    // ===========================
    const [mostrarFormulario, setMostrarFormulario] = useState<boolean>(false);
    const [nombreAdmin, setNombreAdmin] = useState('');
    const [apellidoAdmin, setApellidoAdmin] = useState('');
    const [cedulaAdmin, setCedulaAdmin] = useState('');
    const [telefonoAdmin, setTelefonoAdmin] = useState('');
    const [direccionAdmin, setDireccionAdmin] = useState('N/A');
    const [contraseniaAdmin, setContraseniaAdmin] = useState('');
    const [confirmarContraseniaAdmin, setConfirmarContraseniaAdmin] = useState('');
    const [mostrarContraseniaAdmin, setMostrarContraseniaAdmin] = useState(false);
    const [mostrarConfirmarAdmin, setMostrarConfirmarAdmin] = useState(false);
    const [cargandoAdmin, setCargandoAdmin] = useState(false);

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

            
            

            // Validar que sea un array
            if (!Array.isArray(data)) {
                
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

    /**
     * Validar formulario de crear administrador
     */
    const validarFormularioAdmin = (): boolean => {
        if (!nombreAdmin.trim()) {
            mostrarMensaje('error', 'El nombre es obligatorio');
            return false;
        }

        if (!apellidoAdmin.trim()) {
            mostrarMensaje('error', 'El apellido es obligatorio');
            return false;
        }

        if (!cedulaAdmin.trim()) {
            mostrarMensaje('error', 'La cédula es obligatoria');
            return false;
        }

        if (cedulaAdmin.length !== 10) {
            mostrarMensaje('error', 'La cédula debe tener 10 dígitos');
            return false;
        }

        if (!telefonoAdmin.trim()) {
            mostrarMensaje('error', 'El teléfono es obligatorio');
            return false;
        }

        if (telefonoAdmin.length !== 10) {
            mostrarMensaje('error', 'El teléfono debe tener 10 dígitos');
            return false;
        }

        if (!contraseniaAdmin.trim()) {
            mostrarMensaje('error', 'La contraseña es obligatoria');
            return false;
        }

        if (contraseniaAdmin.length < 8) {
            mostrarMensaje('error', 'La contraseña debe tener al menos 8 caracteres');
            return false;
        }

        if (contraseniaAdmin !== confirmarContraseniaAdmin) {
            mostrarMensaje('error', 'Las contraseñas no coinciden');
            return false;
        }

        return true;
    };

    /**
     * Manejar creación de administrador
     */
    const handleCrearAdministrador = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validarFormularioAdmin()) return;

        try {
            setCargandoAdmin(true);

            await authService.crearAdministrador({
                nombres: nombreAdmin,
                apellidos: apellidoAdmin,
                cedula: cedulaAdmin,
                telefono: telefonoAdmin,
                direccion: direccionAdmin,
                contrasenia: contraseniaAdmin,
            });

            mostrarMensaje('success', '✅ Administrador creado exitosamente');

            // Limpiar formulario
            handleCancelarCreacion();

            // Recargar lista de usuarios
            await cargarUsuarios();
        } catch (error: unknown) {
            
            const errorMessage = error instanceof Error ? error.message : 'Error al crear el administrador';
            mostrarMensaje('error', errorMessage);
        } finally {
            setCargandoAdmin(false);
        }
    };

    /**
     * Cancelar creación de administrador
     */
    const handleCancelarCreacion = () => {
        setNombreAdmin('');
        setApellidoAdmin('');
        setCedulaAdmin('');
        setTelefonoAdmin('');
        setDireccionAdmin('N/A');
        setContraseniaAdmin('');
        setConfirmarContraseniaAdmin('');
        setMostrarContraseniaAdmin(false);
        setMostrarConfirmarAdmin(false);
        setMostrarFormulario(false);
    };

    // ===========================
    // RENDER
    // ===========================

    return (
        <DashboardLayout role="SAD">
            <div className="usuarios-container">
                {/* Header */}
                <div className="usuarios-header">
                    <div className="header-left">
                        <h1 className="usuarios-titulo">👥 Gestión de Usuarios</h1>
                        <p className="usuarios-descripcion">
                            Administra los usuarios administradores del sistema: crear y eliminar
                        </p>
                    </div>
                    <div className="header-right">
                        <button
                            className="btn-nuevo-admin"
                            onClick={() => setMostrarFormulario(!mostrarFormulario)}
                        >
                            ➕ Nuevo Administrador
                        </button>
                    </div>
                </div>

                {/* Mensajes */}
                {mensaje && (
                    <div className={`mensaje mensaje-${mensaje.tipo}`}>
                        {mensaje.texto}
                    </div>
                )}

                {/* Formulario de Crear Administrador */}
                {mostrarFormulario && (
                    <div className="crear-admin-section">
                        <h3 className="section-subtitle">➕ Crear Nuevo Administrador</h3>
                        <form onSubmit={handleCrearAdministrador} className="form-crear-admin">
                            <div className="form-grid">
                                {/* Nombres */}
                                <div className="form-group">
                                    <label htmlFor="nombreAdmin" className="form-label">
                                        Nombres *
                                    </label>
                                    <input
                                        type="text"
                                        id="nombreAdmin"
                                        className="form-input"
                                        placeholder="Ej: Juan Carlos"
                                        value={nombreAdmin}
                                        onChange={(e) => setNombreAdmin(e.target.value)}
                                        disabled={cargandoAdmin}
                                        required
                                    />
                                </div>

                                {/* Apellidos */}
                                <div className="form-group">
                                    <label htmlFor="apellidoAdmin" className="form-label">
                                        Apellidos *
                                    </label>
                                    <input
                                        type="text"
                                        id="apellidoAdmin"
                                        className="form-input"
                                        placeholder="Ej: Pérez García"
                                        value={apellidoAdmin}
                                        onChange={(e) => setApellidoAdmin(e.target.value)}
                                        disabled={cargandoAdmin}
                                        required
                                    />
                                </div>

                                {/* Cédula */}
                                <div className="form-group">
                                    <label htmlFor="cedulaAdmin" className="form-label">
                                        Cédula *
                                    </label>
                                    <input
                                        type="text"
                                        id="cedulaAdmin"
                                        className="form-input"
                                        placeholder="1234567890"
                                        value={cedulaAdmin}
                                        onChange={(e) => {
                                            const valor = e.target.value.replace(/\D/g, '');
                                            if (valor.length <= 10) {
                                                setCedulaAdmin(valor);
                                            }
                                        }}
                                        disabled={cargandoAdmin}
                                        maxLength={10}
                                        required
                                    />
                                    <small className="form-help">10 dígitos sin guiones</small>
                                </div>

                                {/* Teléfono */}
                                <div className="form-group">
                                    <label htmlFor="telefonoAdmin" className="form-label">
                                        Teléfono *
                                    </label>
                                    <input
                                        type="text"
                                        id="telefonoAdmin"
                                        className="form-input"
                                        placeholder="0987654321"
                                        value={telefonoAdmin}
                                        onChange={(e) => {
                                            const valor = e.target.value.replace(/\D/g, '');
                                            if (valor.length <= 10) {
                                                setTelefonoAdmin(valor);
                                            }
                                        }}
                                        disabled={cargandoAdmin}
                                        maxLength={10}
                                        required
                                    />
                                    <small className="form-help">10 dígitos sin guiones</small>
                                </div>

                                {/* Dirección */}
                                <div className="form-group form-group-full">
                                    <label htmlFor="direccionAdmin" className="form-label">
                                        Dirección (Opcional)
                                    </label>
                                    <input
                                        type="text"
                                        id="direccionAdmin"
                                        className="form-input"
                                        placeholder="Ej: Av. Principal #123"
                                        value={direccionAdmin}
                                        onChange={(e) => setDireccionAdmin(e.target.value || 'N/A')}
                                        disabled={cargandoAdmin}
                                    />
                                </div>

                                {/* Contraseña */}
                                <div className="form-group">
                                    <label htmlFor="contraseniaAdmin" className="form-label">
                                        Contraseña *
                                    </label>
                                    <div className="input-with-icon">
                                        <input
                                            type={mostrarContraseniaAdmin ? 'text' : 'password'}
                                            id="contraseniaAdmin"
                                            className="form-input"
                                            placeholder="Mínimo 8 caracteres"
                                            value={contraseniaAdmin}
                                            onChange={(e) => setContraseniaAdmin(e.target.value)}
                                            disabled={cargandoAdmin}
                                            required
                                        />
                                        <button
                                            type="button"
                                            className="toggle-password"
                                            onClick={() => setMostrarContraseniaAdmin(!mostrarContraseniaAdmin)}
                                            disabled={cargandoAdmin}
                                        >
                                            {mostrarContraseniaAdmin ? '🙈' : '👁️'}
                                        </button>
                                    </div>
                                    <small className="form-help">Mínimo 8 caracteres</small>
                                </div>

                                {/* Confirmar Contraseña */}
                                <div className="form-group">
                                    <label htmlFor="confirmarContraseniaAdmin" className="form-label">
                                        Confirmar Contraseña *
                                    </label>
                                    <div className="input-with-icon">
                                        <input
                                            type={mostrarConfirmarAdmin ? 'text' : 'password'}
                                            id="confirmarContraseniaAdmin"
                                            className="form-input"
                                            placeholder="Repite la contraseña"
                                            value={confirmarContraseniaAdmin}
                                            onChange={(e) => setConfirmarContraseniaAdmin(e.target.value)}
                                            disabled={cargandoAdmin}
                                            required
                                        />
                                        <button
                                            type="button"
                                            className="toggle-password"
                                            onClick={() => setMostrarConfirmarAdmin(!mostrarConfirmarAdmin)}
                                            disabled={cargandoAdmin}
                                        >
                                            {mostrarConfirmarAdmin ? '🙈' : '👁️'}
                                        </button>
                                    </div>
                                </div>
                            </div>

                            {/* Botones de acción */}
                            <div className="form-acciones">
                                <button
                                    type="button"
                                    className="btn-cancelar-form"
                                    onClick={handleCancelarCreacion}
                                    disabled={cargandoAdmin}
                                >
                                    ❌ Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="btn-crear-admin"
                                    disabled={cargandoAdmin}
                                >
                                    {cargandoAdmin ? '⏳ Creando...' : '➕ Crear Administrador'}
                                </button>
                            </div>
                        </form>
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
