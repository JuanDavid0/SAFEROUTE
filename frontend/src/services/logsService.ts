/**
 * Servicio de Logs del Sistema
 * Maneja las operaciones relacionadas con los logs de auditoría
 */

import apiClient from '@/lib/axios';
import { BackendResponse, Log } from '@/types';

/**
 * Obtener todos los logs del sistema
 */
export const obtenerLogs = async (): Promise<Log[]> => {
  const response = await apiClient.get<BackendResponse<Log[]>>('/logs');
  
  if (response.data.status === 'success' && response.data.data) {
    return response.data.data;
  }
  
  return [];
};

/**
 * Obtener logs de un usuario específico
 */
export const obtenerLogsPorUsuario = async (idUsuario: number): Promise<Log[]> => {
  const response = await apiClient.get<BackendResponse<Log[]>>(`/logs/usuario/${idUsuario}`);
  
  if (response.data.status === 'success' && response.data.data) {
    return response.data.data;
  }
  
  return [];
};
