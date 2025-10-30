/**
 * Toast - Sistema de notificaciones mejorado
 * Muestra mensajes de éxito, error y advertencia con detalles del backend
 */

'use client';

import React, { useEffect, useState } from 'react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';
export type ToastPosition = 'top-right' | 'top-center' | 'bottom-right' | 'bottom-center';

export interface ToastProps {
    type: ToastType;
    message: string;
    details?: string;
    errorCode?: string;
    validationErrors?: Record<string, string>;
    duration?: number;
    onClose: () => void;
    position?: ToastPosition;
}

export const Toast: React.FC<ToastProps> = ({
    type,
    message,
    details,
    errorCode,
    validationErrors,
    duration = 6000,
    onClose,
    position = 'top-right'
}) => {
    const [isPaused, setIsPaused] = useState(false);

    useEffect(() => {
        if (duration > 0 && !isPaused) {
            const timer = setTimeout(() => {
                onClose();
            }, duration);

            return () => clearTimeout(timer);
        }
    }, [duration, onClose, isPaused]);

    const getIcon = () => {
        switch (type) {
            case 'success':
                return '✓';
            case 'error':
                return '✕';
            case 'warning':
                return '⚠';
            case 'info':
                return 'ℹ';
            default:
                return 'ℹ';
        }
    };

    return (
        <div 
            className="toast"
            onMouseEnter={() => setIsPaused(true)}
            onMouseLeave={() => setIsPaused(false)}
        >
            <div className="toast-content">
                <div className={`toast-icon ${type}`}>
                    {getIcon()}
                </div>
                <div className="toast-message-container">
                    <div className="toast-message">{message}</div>
                    {details && <div className="toast-details">{details}</div>}
                    
                    {/* Mostrar errorCode solo si NO hay details */}
                    {!details && errorCode && errorCode !== 'UNKNOWN_ERROR' && (
                        <span className="toast-error-code">
                            {errorCode}
                        </span>
                    )}
                    
                    {/* Errores de validación */}
                    {validationErrors && Object.keys(validationErrors).length > 0 && (
                        <ul className="toast-validation-errors">
                            {Object.entries(validationErrors).map(([field, error]) => (
                                <li key={field}>
                                    <strong>{field}:</strong> {error}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                <button className="toast-close" onClick={onClose} aria-label="Cerrar">
                    ✕
                </button>
            </div>

            {/* Barra de progreso */}
            {duration > 0 && (
                <div 
                    className={`toast-progress ${type} ${isPaused ? '' : 'animate'}`}
                    style={{ animationDuration: `${duration}ms` }}
                />
            )}
        </div>
    );
};
