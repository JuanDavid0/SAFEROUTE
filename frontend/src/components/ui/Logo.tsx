/**
 * Componente Logo de SAFE ROUTE
 * Reutilizable en toda la aplicación
 */

import React from 'react';

interface LogoProps {
    size?: 'sm' | 'md' | 'lg';
    showText?: boolean;
    className?: string;
}

export const Logo: React.FC<LogoProps> = ({
    size = 'md',
    showText = true,
    className = ''
}) => {
    const sizeClasses = {
        sm: 'w-12 h-12',
        md: 'w-16 h-16',
        lg: 'w-24 h-24',
    };

    const textSizeClasses = {
        sm: 'text-xl',
        md: 'text-2xl',
        lg: 'text-4xl',
    };

    return (
        <div className={`flex flex-col items-center gap-3 ${className}`}>
            {/* Ícono circular del logo */}
            <div className={`${sizeClasses[size]} bg-azul-petroleo rounded-full flex items-center justify-center shadow-lg`}>
                <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                    className="w-3/5 h-3/5 text-blanco"
                >
                    {/* Ícono de ruta/camino */}
                    <path
                        d="M12 2L2 7L12 12L22 7L12 2Z"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        fill="currentColor"
                        opacity="0.8"
                    />
                    <path
                        d="M2 17L12 22L22 17"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                    <path
                        d="M2 12L12 17L22 12"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </div>

            {/* Texto del logo */}
            {showText && (
                <h1 className={`${textSizeClasses[size]} font-bold text-azul-petroleo tracking-wide`}>
                    SAFE ROUTE
                </h1>
            )}
        </div>
    );
};

export default Logo;
