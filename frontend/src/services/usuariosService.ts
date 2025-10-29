/**
 * Servicio de Gestión de Usuarios
 * Maneja la obtención y eliminación de administradores
 */

import apiClient from '@/lib/axios';

// ===========================
// INTERFACES
// ===========================

/**
 * Usuario/Administrador del sistema
 */
export interface Usuario {
	idUsuario: number;
	cedula: string;
	nombres: string;
	apellidos: string;
	nombreCompleto?: string;
	telefono?: string;
	direccion?: string;
	roles: string[];
	estado?: string; // Estado del usuario: "ACT" (Activo) o "INA" (Inactivo)
}

// ===========================
// FUNCIONES DEL SERVICIO
// ===========================

/**
 * Obtener lista de usuarios administradores activos
 * Solo muestra usuarios con rol ADM que estén activos
 */
export const obtenerUsuarios = async (): Promise<Usuario[]> => {
	try {
		const response = await apiClient.get('/usuarios');

		let usuarios: Usuario[] = [];

		// Asegurar que siempre retornemos un array
		if (Array.isArray(response.data)) {
			usuarios = response.data;
		} else if (response.data && Array.isArray(response.data.usuarios)) {
			usuarios = response.data.usuarios;
		} else if (response.data && Array.isArray(response.data.data)) {
			usuarios = response.data.data;
		} else {
			console.warn('⚠️ Respuesta inesperada del servidor:', response.data);
			return [];
		}

		// Filtrar solo usuarios con rol ADM y estado ACT (Activo)
		const usuariosAdminActivos = usuarios.filter((usuario) => {
			// Verificar que tenga el rol ADM
			const esAdmin = usuario.roles && usuario.roles.includes('ADM');
			
			// Verificar que esté activo (estado = "ACT")
			const estaActivo = usuario.estado === 'ACTIVO';
			
			return esAdmin && estaActivo;
		});

		

		return usuariosAdminActivos;
	} catch (error) {
		
		return [];
	}
};

/**
 * Eliminar un usuario/administrador por ID
 */
export const eliminarUsuario = async (idUsuario: number): Promise<void> => {
	try {
		
		
		const response = await apiClient.delete(`/usuarios/${idUsuario}`);

		
	} catch (error: any) {
		
		
		// Propagar el error con mensaje claro
		if (error.response?.data?.message) {
			throw new Error(error.response.data.message);
		}
		
		if (error.response?.data?.error) {
			throw new Error(error.response.data.error);
		}

		throw new Error('Error al eliminar el usuario. Por favor, intenta de nuevo.');
	}
};

// ===========================
// EXPORT DEFAULT
// ===========================

const usuariosService = {
	obtenerUsuarios,
	eliminarUsuario,
};

export default usuariosService;
