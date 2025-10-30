/**
 * Componente Navbar
 * Barra de navegación principal
 */

'use client';

import React from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'next/navigation';
import { ROLES } from '@/utils/constants';

export const Navbar: React.FC = () => {
    const { user, logout, isAuthenticated } = useAuthStore();
    const router = useRouter();

    const handleLogout = () => {
        logout();
        router.push('/login');
    };

    if (!isAuthenticated || !user) return null;

    return (
        <nav className="bg-azul-petroleo text-blanco shadow-md">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Logo */}
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold text-blanco">SAFE ROUTE</h1>
                        <span className="px-3 py-1 bg-coral-rojo rounded-full text-xs font-semibold">
                            {ROLES[user.rol.nombreRol as keyof typeof ROLES]}
                        </span>
                    </div>

                    {/* Usuario Info */}
                    <div className="flex items-center gap-4">
                        <div className="text-right">
                            <p className="font-semibold text-sm">
                                {user.nombres} {user.apellidos}
                            </p>
                            <p className="text-xs text-verde-claro">
                                {user.cedula}
                            </p>
                        </div>

                        <button
                            onClick={handleLogout}
                            className="px-4 py-2 bg-coral-rojo hover:bg-rojo-intenso rounded-lg font-semibold transition-colors duration-200"
                        >
                            Cerrar Sesión
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
