/**
 * DashboardLayout - Layout compartido para dashboards
 * Incluye Sidebar y área de contenido principal
 * Protege rutas según el rol del usuario
 */

'use client';

import React from 'react';
import { Sidebar } from './Sidebar';
import { useDashboard } from '@/hooks/useDashboard';
import { Loading } from '@/components/ui';

interface DashboardLayoutProps {
    children: React.ReactNode;
    role: 'ADM' | 'SAD';
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children, role }) => {
    const { isAuthorized, user } = useDashboard({ requiredRole: role });

    // Mostrar loading mientras se verifica autenticación
    if (!isAuthorized) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gris-claro">
                <Loading />
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            {/* Barra lateral izquierda */}
            <Sidebar role={role} user={user} />

            {/* Área principal de contenido */}
            <main className="dashboard-main">
                {children}
            </main>
        </div>
    );
};
