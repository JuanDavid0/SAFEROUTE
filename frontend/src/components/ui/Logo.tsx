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
            {/* Logo de SAFE ROUTE desde Firebase */}
            <div className={`${sizeClasses[size]} flex items-center justify-center`}>
                <img
                    src="https://firebasestorage.googleapis.com/v0/b/saferoute-f90f8.firebasestorage.app/o/SF2.png?alt=media&token=bde250f8-5d60-49dc-b52d-c61f61fb0f85"
                    alt="SAFE ROUTE Logo"
                    className="w-full h-full object-contain"
                />
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
