/**
 * Configuración - Super Administrador
 */

'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';

export default function ConfiguracionPage() {
    return (
        <DashboardLayout role="SAD">
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Configuración</h1>
            </div>

            <div className="dashboard-content">
                <div className="dashboard-module-card">
                    <h2 className="dashboard-module-title">Configuración del Sistema</h2>
                    <p className="dashboard-module-description">
                        Este módulo se implementará en las próximas fases del proyecto.
                    </p>
                </div>
            </div>
        </DashboardLayout>
    );
}
