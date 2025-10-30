/**
 * ToastContainer - Contenedor para múltiples toasts
 * Gestiona la cola de notificaciones
 */

'use client';

import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { Toast, ToastProps, ToastType, ToastPosition } from './Toast';

export interface ToastItem extends Omit<ToastProps, 'onClose'> {
    id: string;
}

export interface ToastContextType {
    showToast: (toast: Omit<ToastItem, 'id'>) => void;
    showSuccess: (message: string, details?: string) => void;
    showError: (message: string, details?: string, errorCode?: string, validationErrors?: Record<string, string>) => void;
    showWarning: (message: string, details?: string) => void;
    showInfo: (message: string, details?: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within ToastProvider');
    }
    return context;
};

export interface ToastProviderProps {
    children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ children }) => {
    const [toasts, setToasts] = useState<ToastItem[]>([]);

    const showToast = useCallback((toast: Omit<ToastItem, 'id'>) => {
        const id = Math.random().toString(36).substr(2, 9);
        setToasts(prev => [...prev, { ...toast, id }]);
    }, []);

    const removeToast = useCallback((id: string) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    }, []);

    const showSuccess = useCallback((message: string, details?: string) => {
        showToast({
            type: 'success',
            message,
            details,
            duration: 6000, // 6 segundos
            position: 'top-right'
        });
    }, [showToast]);

    const showError = useCallback((
        message: string, 
        details?: string, 
        errorCode?: string,
        validationErrors?: Record<string, string>
    ) => {
        showToast({
            type: 'error',
            message,
            details,
            errorCode,
            validationErrors,
            duration: validationErrors ? 12000 : 8000, // 8-12 segundos según errores
            position: 'top-right'
        });
    }, [showToast]);

    const showWarning = useCallback((message: string, details?: string) => {
        showToast({
            type: 'warning',
            message,
            details,
            duration: 7000, // 7 segundos
            position: 'top-right'
        });
    }, [showToast]);

    const showInfo = useCallback((message: string, details?: string) => {
        showToast({
            type: 'info',
            message,
            details,
            duration: 6000, // 6 segundos
            position: 'top-right'
        });
    }, [showToast]);

    const value: ToastContextType = {
        showToast,
        showSuccess,
        showError,
        showWarning,
        showInfo
    };

    return (
        <ToastContext.Provider value={value}>
            {children}
            <div className="toast-container top-right">
                {toasts.map(toast => (
                    <Toast
                        key={toast.id}
                        {...toast}
                        onClose={() => removeToast(toast.id)}
                    />
                ))}
            </div>
        </ToastContext.Provider>
    );
};
