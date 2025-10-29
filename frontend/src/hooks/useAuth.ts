/**
 * Hook personalizado para proteger rutas
 * Redirige a login si el usuario no está autenticado o el token expiró
 */

'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';

export const useAuth = (requiredRole?: 'CLI' | 'ADM' | 'SAD') => {
  const router = useRouter();
  const pathname = usePathname();
  const { isAuthenticated, user, checkTokenExpiration, logout } = useAuthStore();
  const hasChecked = useRef(false);
  const [isMounted, setIsMounted] = useState(false);

  // Esperar a que el componente esté montado en el cliente
  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    // No hacer nada durante SSR o antes de que el componente esté montado
    if (!isMounted) return;

    // NO validar en rutas públicas (pedidos con hash)
    if (pathname?.startsWith('/pedido/')) {
      
      return;
    }

    // Solo ejecutar la validación una vez después de que el componente monte
    // y dar tiempo a que Zustand hidrate el estado
    const timeoutId = setTimeout(() => {
      if (hasChecked.current) return;
      hasChecked.current = true;

      

      // Si no está autenticado, redirigir a login
      if (!isAuthenticated) {
        
        router.push('/login');
        return;
      }

      // Verificar expiración del token
      const isTokenValid = checkTokenExpiration();
      if (!isTokenValid) {
        
        router.push('/login');
        return;
      }

      // Validar rol requerido
      if (requiredRole && user?.rol.nombreRol !== requiredRole) {
        
        // Si el rol no coincide, redirigir a la página apropiada
        if (user?.rol.nombreRol === 'CLI') {
          router.push('/cliente');
        } else if (user?.rol.nombreRol === 'ADM') {
          router.push('/admin');
        } else if (user?.rol.nombreRol === 'SAD') {
          router.push('/super-admin');
        }
        return;
      }

      
    }, 200); // Aumentado a 200ms para dar más tiempo a la hidratación

    // Verificar expiración periódicamente (cada 1 minuto)
    const intervalId = setInterval(() => {
      if (!isAuthenticated) return;
      
      const isStillValid = checkTokenExpiration();
      if (!isStillValid) {
        
        logout();
        router.push('/login');
      }
    }, 60000); // 1 minuto

    return () => {
      clearTimeout(timeoutId);
      clearInterval(intervalId);
    };
  }, [isMounted, pathname]); // Agregar pathname como dependencia

  return { user, isAuthenticated };
};
