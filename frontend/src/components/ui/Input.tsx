/**
 * Componente Input Reutilizable
 * Sigue los lineamientos visuales de SAFE ROUTE
 */

import React, { forwardRef } from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    helperText?: string;
    fullWidth?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
    ({ label, error, helperText, fullWidth = false, className = '', ...props }, ref) => {
        const containerClass = fullWidth ? 'w-full' : '';
        const inputClass = error ? 'input-base input-error' : 'input-base';

        return (
            <div className={`${containerClass}`}>
                {label && (
                    <label className="block text-negro font-medium text-sm mb-2">
                        {label}
                        {props.required && <span className="text-rojo-intenso ml-1">*</span>}
                    </label>
                )}

                <input
                    ref={ref}
                    className={`${inputClass} ${className}`}
                    {...props}
                />

                {error && (
                    <p className="error-message mt-1">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-4 w-4"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                        >
                            <path
                                fillRule="evenodd"
                                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                                clipRule="evenodd"
                            />
                        </svg>
                        {error}
                    </p>
                )}

                {helperText && !error && (
                    <p className="text-gris-medio text-sm mt-1">{helperText}</p>
                )}
            </div>
        );
    }
);

Input.displayName = 'Input';

export default Input;
