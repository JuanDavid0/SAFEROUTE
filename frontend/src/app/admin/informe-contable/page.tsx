/**
 * Informe Contable - Administrador
 */

'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';

export default function InformeContablePage() {
    return (
        <DashboardLayout role="ADM">
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Informe Contable</h1>
            </div>

            <div className="dashboard-content">
                <div className="dashboard-module-card">
                    <h2 className="dashboard-module-title">Módulo de Informe Contable</h2>
                    <p className="dashboard-module-description">
                        Este módulo se implementará en las próximas fases del proyecto.
                    </p>
                </div>
            </div>
        </DashboardLayout>
    );
}
