/**
 * Página principal de SAFE ROUTE
 * Redirige automáticamente al login o al dashboard según autenticación
 */

'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { Loading } from '@/components/ui/Loading';

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuthStore();
  const [isMounted, setIsMounted] = useState(false);

  // Esperar a que el componente monte en el cliente
  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    // No hacer nada durante SSR
    if (!isMounted) return;

    // Esperar un poco para que Zustand hidrate
    const timeoutId = setTimeout(() => {
      if (!isAuthenticated) {
        // No autenticado -> Login
        router.push('/login');
      } else if (user) {
        // Autenticado -> Redirigir según rol
        const rol = user.rol.nombreRol;

        if (rol === 'CLI') {
          router.push('/cliente');
        } else if (rol === 'ADM') {
          router.push('/admin');
        } else if (rol === 'SAD') {
          router.push('/super-admin');
        }
      }
    }, 200); // Delay para hidratación

    return () => clearTimeout(timeoutId);
  }, [isMounted]); // Solo depender de isMounted

  return (
    <div className="min-h-screen flex items-center justify-center bg-gris-claro">
      <Loading size="lg" text="Cargando..." />
    </div>
  );
}
