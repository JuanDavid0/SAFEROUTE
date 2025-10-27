/**
 * useDashboard - Hook para dashboards protegidos
 * Verifica autenticación, rol y maneja redirecciones
 */

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';

interface UseDashboardOptions {
    requiredRole: 'ADM' | 'SAD' | 'CLI';
    redirectTo?: string;
}

export const useDashboard = ({ requiredRole, redirectTo = '/login' }: UseDashboardOptions) => {
    const router = useRouter();
    const { isAuthenticated, user } = useAuthStore();

    useEffect(() => {
        // Verificar autenticación
        if (!isAuthenticated) {
            console.warn('⚠️ Usuario no autenticado. Redirigiendo a login...');
            router.push(redirectTo);
            return;
        }

        // Verificar rol
        const userRole = user?.rol?.nombreRol;
        if (userRole !== requiredRole) {
            console.warn(`⚠️ Acceso denegado. Se requiere rol: ${requiredRole}, rol actual: ${userRole}`);
            
            // Redirigir según el rol actual
            switch (userRole) {
                case 'SAD':
                    router.push('/superadmin/dashboard');
                    break;
                case 'ADM':
                    router.push('/admin/dashboard');
                    break;
                case 'CLI':
                    router.push('/cliente/dashboard');
                    break;
                default:
                    router.push('/login');
            }
        }
    }, [isAuthenticated, user, requiredRole, router, redirectTo]);

    return {
        isAuthenticated,
        user,
        isAuthorized: isAuthenticated && user?.rol?.nombreRol === requiredRole,
    };
};
