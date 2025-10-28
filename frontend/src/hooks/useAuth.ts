/**
 * Hook personalizado para proteger rutas
 * Redirige a login si el usuario no está autenticado o el token expiró
 */

'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';

export const useAuth = (requiredRole?: 'CLI' | 'ADM' | 'SAD') => {
  const router = useRouter();
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

    // Solo ejecutar la validación una vez después de que el componente monte
    // y dar tiempo a que Zustand hidrate el estado
    const timeoutId = setTimeout(() => {
      if (hasChecked.current) return;
      hasChecked.current = true;

      console.log('🔍 Verificando autenticación...', { isAuthenticated, user: user?.rol.nombreRol });

      // Si no está autenticado, redirigir a login
      if (!isAuthenticated) {
        console.log('⚠️ No autenticado, redirigiendo a login');
        router.push('/login');
        return;
      }

      // Verificar expiración del token
      const isTokenValid = checkTokenExpiration();
      if (!isTokenValid) {
        console.log('⚠️ Token expirado, redirigiendo a login');
        router.push('/login');
        return;
      }

      // Validar rol requerido
      if (requiredRole && user?.rol.nombreRol !== requiredRole) {
        console.log(`⚠️ Rol incorrecto. Esperado: ${requiredRole}, Actual: ${user?.rol.nombreRol}`);
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

      console.log('✅ Autenticación válida');
    }, 100); // Pequeño delay para que Zustand termine de hidratar

    // Verificar expiración periódicamente (cada 1 minuto)
    const intervalId = setInterval(() => {
      if (!isAuthenticated) return;
      
      const isStillValid = checkTokenExpiration();
      if (!isStillValid) {
        console.log('⏰ Token expirado durante la sesión, cerrando...');
        logout();
        router.push('/login');
      }
    }, 60000); // 1 minuto

    return () => {
      clearTimeout(timeoutId);
      clearInterval(intervalId);
    };
  }, [isMounted]); // Solo depender de isMounted

  return { user, isAuthenticated };
};

