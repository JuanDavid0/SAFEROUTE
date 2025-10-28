/**
 * Sidebar - Barra lateral de navegación
 * Componente reutilizable para dashboards de ADM y SAD
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { Usuario } from '@/types';

interface MenuOption {
    label: string;
    path: string;
    icon?: string;
    subOptions?: { label: string; path: string }[];
}

interface SidebarProps {
    role: 'ADM' | 'SAD';
    user?: Usuario | null;
}

export const Sidebar: React.FC<SidebarProps> = ({ role, user }) => {
    const pathname = usePathname();
    const router = useRouter();
    const { logout } = useAuthStore();

    // Opciones del menú según el rol
    const menuOptions: MenuOption[] = role === 'SAD' ? [
        {
            label: 'Pedidos',
            path: '/superadmin/pedidos',
            subOptions: [
                { label: 'Consultar Solicitudes', path: '/superadmin/pedidos/consultar' },
                { label: 'Gestionar pedidos', path: '/superadmin/gestionar-pedidos' },
            ]
        },
        {
            label: 'Productos',
            path: '/superadmin/productos',
        },
        {
            label: 'Reportes',
            path: '/superadmin/reportes',
        },
        {
            label: 'Informe contable',
            path: '/superadmin/informe-contable',
        },
        {
            label: 'Etiquetas',
            path: '/superadmin/etiquetas',
        },
        {
            label: 'Usuarios',
            path: '/superadmin/usuarios',
        },
        {
            label: 'Configuración',
            path: '/superadmin/configuracion',
        },
    ] : [
        {
            label: 'Pedidos',
            path: '/admin/pedidos',
            subOptions: [
                { label: 'Consultar Solicitudes', path: '/admin/pedidos/consultar' },
                { label: 'Gestionar pedidos', path: '/admin/gestionar-pedidos' },
            ]
        },
        {
            label: 'Productos',
            path: '/admin/productos',
        },
        {
            label: 'Reportes',
            path: '/admin/reportes',
        },
        {
            label: 'Informe contable',
            path: '/admin/informe-contable',
        },
        {
            label: 'Etiquetas',
            path: '/admin/etiquetas',
        },
    ];

    const handleLogout = () => {
        logout();
        router.push('/login');
    };

    const isActiveRoute = (path: string) => {
        return pathname.startsWith(path);
    };

    return (
        <aside className="sidebar-container">
            {/* Header - Logo/Nombre del sistema */}
            <div className="sidebar-header">
                <h1 className="sidebar-title">SAFE ROUTE</h1>
            </div>

            {/* Navegación */}
            <nav className="sidebar-nav">
                <ul className="sidebar-menu">
                    {menuOptions.map((option, index) => (
                        <li key={index} className="sidebar-menu-item">
                            <Link
                                href={option.path}
                                className={`sidebar-link ${isActiveRoute(option.path) ? 'sidebar-link-active' : ''
                                    }`}
                            >
                                {option.label}
                            </Link>

                            {/* Sub-opciones */}
                            {option.subOptions && isActiveRoute(option.path) && (
                                <ul className="sidebar-submenu">
                                    {option.subOptions.map((subOption, subIndex) => (
                                        <li key={subIndex}>
                                            <Link
                                                href={subOption.path}
                                                className={`sidebar-sublink ${pathname === subOption.path ? 'sidebar-sublink-active' : ''
                                                    }`}
                                            >
                                                {subOption.label}
                                            </Link>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </li>
                    ))}
                </ul>
            </nav>

            {/* Información del Usuario */}
            {user && (
                <div className="sidebar-user-info">
                    <div className="sidebar-user-avatar">
                        {user.nombres.charAt(0)}{user.apellidos.charAt(0)}
                    </div>
                    <div className="sidebar-user-details">
                        <p className="sidebar-user-name">
                            {user.nombres} {user.apellidos}
                        </p>
                        <p className="sidebar-user-role">
                            {user.rol?.nombreRol === 'SAD' ? 'Super Administrador' : 'Administrador'}
                        </p>
                    </div>
                </div>
            )}

            {/* Cerrar Sesión */}
            <div className="sidebar-footer">
                <button
                    onClick={handleLogout}
                    className="sidebar-logout"
                >
                    Cerrar sesión
                </button>
            </div>
        </aside>
    );
};
