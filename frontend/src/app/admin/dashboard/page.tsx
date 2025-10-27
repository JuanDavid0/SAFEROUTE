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

            {/* Contenido principal */}
            <div className="dashboard-content">
                <div className="dashboard-module-card">
                    <h2 className="dashboard-module-title">Gestión de Pedidos</h2>
                    <p className="dashboard-module-description">
                        Aquí irá el módulo de gestión de pedidos...
                    </p>
                </div>
            </div>
        </DashboardLayout>
    );
}
