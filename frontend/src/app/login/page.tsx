/**
 * FASE 2: Vista de Login (Funcional)
 * Para SAD y ADM
 * 
 * Funcionalidades:
 * - Validación de cédula (10 dígitos)
 * - Autenticación con backend
 * - Manejo de errores con sistema de Toast
 * - Redirección según rol
 */

'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Logo, Input, Button, useToast } from '@/components/ui';
import { useAuthStore } from '@/stores/authStore';
import { login } from '@/services/authService';
import { validarCedula } from '@/utils/validators';
import { MENSAJES_ERROR } from '@/utils/constants';
import { extractErrorInfo } from '@/utils/errorHandler';

export default function LoginPage() {
    const router = useRouter();
    const setAuth = useAuthStore((state) => state.setAuth);
    const { showSuccess, showError, showWarning } = useToast();

    // Estados del formulario
    const [cedula, setCedula] = useState('');
    const [contrasenia, setContrasenia] = useState('');

    // Estados de validación y UI
    const [errores, setErrores] = useState({
        cedula: '',
        contrasenia: '',
    });
    const [isLoading, setIsLoading] = useState(false);

    /**
     * Valida los campos del formulario
     */
    const validarFormulario = (): boolean => {
        const nuevosErrores = {
            cedula: '',
            contrasenia: '',
        };

        // Validar cédula
        if (!cedula.trim()) {
            nuevosErrores.cedula = MENSAJES_ERROR.REQUIRED;
        } else if (!validarCedula(cedula)) {
            nuevosErrores.cedula = MENSAJES_ERROR.INVALID_CEDULA;
        }

        // Validar contraseña
        if (!contrasenia) {
            nuevosErrores.contrasenia = MENSAJES_ERROR.REQUIRED;
        }

        setErrores(nuevosErrores);
        return !nuevosErrores.cedula && !nuevosErrores.contrasenia;
    };

    /**
     * Maneja el envío del formulario
     */
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        e.stopPropagation();

        // Validar formulario
        if (!validarFormulario()) {
            showWarning(
                'Formulario incompleto',
                'Por favor, corrija los errores antes de continuar'
            );
            return;
        }

        setIsLoading(true);

        try {
            // Llamar al endpoint de login
            const response = await login({ cedula, contrasenia });

            if (response.status === 'success' && response.data) {
                const { token, rol, idUsuario } = response.data;

                // Verificar que sea SAD o ADM
                if (rol !== 'SAD' && rol !== 'ADM') {
                    showError(
                        'Acceso denegado',
                        'Solo administradores (SAD/ADM) pueden ingresar al sistema',
                        'ACCESS_DENIED'
                    );
                    setIsLoading(false);
                    return;
                }

                // Guardar token en el store
                const userData = {
                    idUsuario: idUsuario,
                    nombres: '',
                    apellidos: '',
                    telefono: '',
                    cedula,
                    direccion: '',
                    estadoUsuario: 'ACTIVO',
                    rol: {
                        idRol: rol === 'SAD' ? 1 : 2,
                        nombreRol: rol as 'SAD' | 'ADM' | 'CLI',
                    },
                };

                const jwtResponse = {
                    token,
                    tipo: 'Bearer',
                    idUsuario: idUsuario,
                    cedula,
                    nombres: '',
                    apellidos: '',
                    rol,
                };

                setAuth(jwtResponse, userData);

                // Mostrar mensaje de éxito
                showSuccess(
                    '¡Bienvenido!',
                    `Redirigiendo al panel de ${rol === 'SAD' ? 'Super Administrador' : 'Administrador'}...`
                );

                // Redirigir según el rol
                setTimeout(() => {
                    if (rol === 'SAD') {
                        router.push('/superadmin/dashboard');
                    } else {
                        router.push('/admin/dashboard');
                    }
                }, 1000);
            } else {
                // Respuesta no exitosa
                showError(
                    'Error al iniciar sesión',
                    response.message || 'Credenciales inválidas'
                );
                setIsLoading(false);
            }
        } catch (error: any) {
            // Extraer información detallada del error
            const errorInfo = extractErrorInfo(error);
            
            // Mostrar error con toda la información del backend
            showError(
                errorInfo.message,
                errorInfo.details,
                errorInfo.errorCode,
                errorInfo.validationErrors
            );
            
            setIsLoading(false);
        }
    };

    /**
     * Limpia el error de un campo cuando el usuario empieza a escribir
     */
    const handleCedulaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const valor = e.target.value.replace(/\D/g, ''); // Solo dígitos
        setCedula(valor.slice(0, 10)); // Máximo 10 dígitos

        if (errores.cedula) {
            setErrores(prev => ({ ...prev, cedula: '' }));
        }
    };

    const handleContraseniaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setContrasenia(e.target.value);

        if (errores.contrasenia) {
            setErrores(prev => ({ ...prev, contrasenia: '' }));
        }
    };

    return (
        <div className="min-h-screen flex">
            {/* ============================================
          SECCIÓN IZQUIERDA - Zona Funcional
          ============================================ */}
            <div className="w-1/2 bg-blanco flex items-center justify-center p-8">
                <div className="w-full max-w-md">
                    {/* Logo de SAFE ROUTE */}
                    <div className="mb-8">
                        <Logo size="lg" showText={true} />
                    </div>

                    {/* Título Principal */}
                    <h2 className="text-3xl font-bold text-azul-petroleo mb-8 text-center">
                        Iniciar Sesión
                    </h2>

                    {/* Formulario de Login */}
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Campo: Usuario (Cédula) */}
                        <Input
                            label="Usuario"
                            type="text"
                            placeholder="Ingresa tu cédula"
                            value={cedula}
                            onChange={handleCedulaChange}
                            error={errores.cedula}
                            required
                            fullWidth
                            maxLength={10}
                            disabled={isLoading}
                        />

                        {/* Campo: Contraseña */}
                        <Input
                            label="Contraseña"
                            type="password"
                            placeholder="Ingresa tu contraseña"
                            value={contrasenia}
                            onChange={handleContraseniaChange}
                            error={errores.contrasenia}
                            required
                            fullWidth
                            disabled={isLoading}
                        />

                        {/* Botón: Ingresar */}
                        <Button
                            type="submit"
                            variant="primary"
                            size="lg"
                            fullWidth
                            className="mt-8"
                            isLoading={isLoading}
                            disabled={isLoading}
                        >
                            {isLoading ? 'Ingresando...' : 'Ingresar'}
                        </Button>
                    </form>

                    {/* Nota informativa */}
                    <div className="mt-6 text-center">
                        <p className="text-sm text-gris-medio">
                            Acceso exclusivo para administradores del sistema
                        </p>
                    </div>
                </div>
            </div>

            {/* ============================================
          SECCIÓN DERECHA - Zona Decorativa
          ============================================ */}
            <div className="w-1/2 bg-azul-petroleo relative overflow-hidden">
                {/* Elementos decorativos opcionales */}
                <div className="absolute inset-0 opacity-10">
                    <div className="absolute top-1/4 right-1/4 w-64 h-64 bg-coral-rojo rounded-full blur-3xl"></div>
                    <div className="absolute bottom-1/4 left-1/4 w-96 h-96 bg-verde-suave rounded-full blur-3xl"></div>
                </div>
            </div>
        </div>
    );
}
