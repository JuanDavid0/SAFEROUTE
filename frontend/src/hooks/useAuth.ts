/**
 * Hook personalizado para proteger rutas
 * Redirige a login si el usuario no está autenticado
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';

export const useAuth = (requiredRole?: 'CLI' | 'ADM' | 'SAD') => {
  const router = useRouter();
  const { isAuthenticated, user } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    if (requiredRole && user?.rol.nombreRol !== requiredRole) {
      // Si el rol no coincide, redirigir a la página apropiada
      if (user?.rol.nombreRol === 'CLI') {
        router.push('/cliente');
      } else if (user?.rol.nombreRol === 'ADM') {
        router.push('/admin');
      } else if (user?.rol.nombreRol === 'SAD') {
        router.push('/super-admin');
      }
    }
  }, [isAuthenticated, user, requiredRole, router]);

  return { user, isAuthenticated };
};
