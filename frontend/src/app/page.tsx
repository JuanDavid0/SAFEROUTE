/**
 * Página principal de SAFE ROUTE
 * Redirige automáticamente al login
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { Loading } from '@/components/ui/Loading';

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, user, rehydrateCheck } = useAuthStore();

  useEffect(() => {
    // Ensure persisted token is validated on app load
    try {
      rehydrateCheck();
    } catch (e) {
      // ignore
    }

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
  }, [isAuthenticated, user, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gris-claro">
      <Loading size="lg" text="Cargando..." />
    </div>
  );
}
