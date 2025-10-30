/**
 * Componente Card Reutilizable
 * Sigue los lineamientos visuales de SAFE ROUTE
 */

import React from 'react';

interface CardProps {
    children: React.ReactNode;
    className?: string;
    padding?: 'none' | 'sm' | 'md' | 'lg';
    hover?: boolean;
}

export const Card: React.FC<CardProps> = ({
    children,
    className = '',
    padding = 'md',
    hover = false,
}) => {
    const paddingClasses = {
        none: '',
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
    };

    const hoverClass = hover ? 'hover:shadow-lg' : '';

    return (
        <div className={`card ${paddingClasses[padding]} ${hoverClass} ${className}`}>
            {children}
        </div>
    );
};

export default Card;
