/**
 * Dashboard de Administrador (ADM)
 * Vista principal con navegación lateral y área de contenido
 */

'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';

export default function AdminDashboardPage() {
    return (
        <DashboardLayout role="ADM">
            {/* Encabezado de la página */}
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Panel de Administrador</h1>
            </div>
        </DashboardLayout>
    );
}
