/**
 * Configuración de Axios para SAFE ROUTE
 * Maneja las llamadas HTTP al backend
 */

import axios from 'axios';

// URL base del backend (ajustar según ambiente)
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// Instancia de Axios configurada
export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 segundos
});

// Interceptor para agregar token JWT a todas las peticiones
api.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      // Solo agregar el token JWT si no hay un Authorization header ya configurado
      if (!config.headers.Authorization) {
        const token = localStorage.getItem('token');
        
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas
api.interceptors.response.use(
  (response) => {
    // Retornar directamente response.data (el objeto del backend)
    return response;
  },
  (error) => {
    // Manejo de errores global
    if (error.response) {
      // El servidor respondió con un código de error
      const status = error.response.status;
      
      if (status === 401) {
        // Token inválido o expirado
        if (typeof window !== 'undefined') {
          // NO redirigir si estamos en una ruta pública (pedidos con hash)
          const currentPath = window.location.pathname;
          const isPublicRoute = currentPath.startsWith('/pedido/');
          
          if (!isPublicRoute) {
            console.warn('⚠️ Token inválido (401), redirigiendo a login');
            localStorage.removeItem('token');
            localStorage.removeItem('tokenExpiration');
            window.location.href = '/login';
          } else {
            
          }
        }
      }
      
      if (status === 403) {
        
      }
      
      // Retornar el mensaje de error del backend
      return Promise.reject(error.response.data);
    } else if (error.request) {
      // La petición se hizo pero no hubo respuesta
      
      return Promise.reject({
        exito: false,
        mensaje: 'Error de conexión. Verifique su conexión a internet.',
      });
    } else {
      // Error al configurar la petición
      
      return Promise.reject({
        exito: false,
        mensaje: 'Error inesperado. Intente nuevamente.',
      });
    }
  }
);

// Instancia separada para multipart/form-data (imágenes)
export const apiMultipart = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'multipart/form-data',
  },
  timeout: 60000, // 60 segundos para archivos
});

// Agregar interceptor de autenticación a apiMultipart
apiMultipart.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
