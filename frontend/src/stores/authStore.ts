/**
 * Store de Autenticación con Zustand
 * Maneja el estado global de autenticación con persistencia en localStorage
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Usuario, JwtResponse } from '@/types';

interface AuthState {
  user: Usuario | null;
  token: string | null;
  tokenExpiration: number | null; // Timestamp de expiración del token
  isAuthenticated: boolean;
  
  // Acciones
  setAuth: (data: JwtResponse, user: Usuario) => void;
  logout: () => void;
  updateUser: (user: Usuario) => void;
  checkTokenExpiration: () => boolean; // Retorna true si el token es válido
}

/**
 * Decodifica el JWT para obtener la fecha de expiración
 */
const decodeJWT = (token: string): { exp?: number } => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    
    return {};
  }
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      tokenExpiration: null,
      isAuthenticated: false,
      
      setAuth: (data: JwtResponse, user: Usuario) => {
        // Decodificar el token para obtener la expiración
        const decoded = decodeJWT(data.token);
        const expirationTimestamp = decoded.exp ? decoded.exp * 1000 : null; // Convertir a milisegundos
        
        if (typeof window !== 'undefined') {
          localStorage.setItem('token', data.token);
          if (expirationTimestamp) {
            localStorage.setItem('tokenExpiration', expirationTimestamp.toString());
          }
        }
        
        
        if (expirationTimestamp) {
          const expiresIn = Math.round((expirationTimestamp - Date.now()) / 1000 / 60);
          
        }
        
        set({
          user,
          token: data.token,
          tokenExpiration: expirationTimestamp,
          isAuthenticated: true,
        });
      },
      
      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('token');
          localStorage.removeItem('tokenExpiration');
        }
        
        set({
          user: null,
          token: null,
          tokenExpiration: null,
          isAuthenticated: false,
        });
      },
      
      updateUser: (user: Usuario) => {
        set({ user });
      },
      
      checkTokenExpiration: () => {
        const { token, tokenExpiration, logout } = get();
        
        if (!token || !tokenExpiration) {
          return false;
        }
        
        const now = Date.now();
        const isExpired = now >= tokenExpiration;
        
        if (isExpired) {
          console.warn('⚠️ Token expirado, cerrando sesión automáticamente');
          logout();
          return false;
        }
        
        return true;
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => 
        typeof window !== 'undefined' ? localStorage : {
          getItem: () => null,
          setItem: () => {},
          removeItem: () => {},
        }
      ),
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        tokenExpiration: state.tokenExpiration,
        isAuthenticated: state.isAuthenticated,
      }),
      // Al hidratar el estado desde localStorage, validar expiración
      onRehydrateStorage: () => (state) => {
        if (state) {
          
          const isValid = state.checkTokenExpiration();
          if (!isValid) {
            
          } else {
            
            const timeRemaining = state.tokenExpiration 
              ? Math.round((state.tokenExpiration - Date.now()) / 1000 / 60)
              : 0;
            
          }
        }
      },
    }
  )
);
