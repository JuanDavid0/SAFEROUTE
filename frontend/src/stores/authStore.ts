/**
 * Store de Autenticación con Zustand
 * Maneja el estado global de autenticación
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { getExpiry, isExpired } from '@/lib/jwt';
import type { Usuario, JwtResponse } from '@/types';

interface AuthState {
  user: Usuario | null;
  token: string | null;
  isAuthenticated: boolean;
  
  // Acciones
  setAuth: (data: JwtResponse, user: Usuario) => void;
  logout: () => void;
  updateUser: (user: Usuario) => void;
  rehydrateCheck: () => void;
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

      rehydrateCheck: () => {
        try {
          if (typeof window === 'undefined') return;
          const token = localStorage.getItem('token');
          if (!token) return;

          if (isExpired(token)) {
            localStorage.removeItem('token');
            set({ user: null, token: null, isAuthenticated: false });
            return;
          }

          const exp = getExpiry(token);
          if (exp) {
            const ms = exp * 1000 - Date.now();
            if (ms > 0) {
              setTimeout(() => {
                localStorage.removeItem('token');
                set({ user: null, token: null, isAuthenticated: false });
                try {
                  window.location.href = '/login';
                } catch (e) {
                  // ignore
                }
              }, ms);
            }
          }

          // Ensure store reflects that we have a valid token
          set((s) => ({ token, isAuthenticated: true }));
        } catch (e) {
          // ignore
        }
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
