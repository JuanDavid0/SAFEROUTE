/**
 * Pedidos - Administrador
 * Vista base para la sección de Pedidos
 */

'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';

export default function PedidosPage() {
    return (
        <DashboardLayout role="ADM">
            <div className="dashboard-page-header">
                <h1 className="dashboard-page-title">Pedidos</h1>
            </div>

            <div className="dashboard-content">
                <div className="dashboard-module-card">
                    <h2 className="dashboard-module-title">Gestión de Pedidos</h2>
                    <p className="dashboard-module-description">
                        Aquí irá el módulo completo de gestión de pedidos...
                    </p>
                </div>
            </div>
        </DashboardLayout>
    );
}
