/**
 * Store de Autenticación con Zustand
 * Maneja el estado global de autenticación
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { Usuario, JwtResponse } from '@/types';

interface AuthState {
  user: Usuario | null;
  token: string | null;
  isAuthenticated: boolean;
  
  // Acciones
  setAuth: (data: JwtResponse, user: Usuario) => void;
  logout: () => void;
  updateUser: (user: Usuario) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      
      setAuth: (data: JwtResponse, user: Usuario) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('token', data.token);
        }
        set({
          user,
          token: data.token,
          isAuthenticated: true,
        });
      },
      
      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('token');
        }
        set({
          user: null,
          token: null,
          isAuthenticated: false,
        });
      },
      
      updateUser: (user: Usuario) => {
        set({ user });
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
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
