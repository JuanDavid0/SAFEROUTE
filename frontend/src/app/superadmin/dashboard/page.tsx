/**
 * Dashboard de Super Administrador (SAD)
 * Vista principal con navegación lateral y área de contenido
 */

'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';

export default function SuperAdminDashboardPage() {
    return (
        <DashboardLayout role="SAD">
            {/* Encabezado de la página */}
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Panel de Super Administrador</h1>
            </div>
        </DashboardLayout>
    );
}
