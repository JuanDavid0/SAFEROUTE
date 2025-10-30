/**
 * FASE 2: Vista de Login (Funcional)
 * Para SAD y ADM
 * 
 * Funcionalidades:
 * - Validación de cédula (10 dígitos)
 * - Autenticación con backend
 * - Manejo de errores
 * - Redirección según rol
 */

'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Logo, Input, Button } from '@/components/ui';
import { useAuthStore } from '@/stores/authStore';
import { login } from '@/services/authService';
import { validarCedula } from '@/utils/validators';
import { MENSAJES_ERROR } from '@/utils/constants';

export default function LoginPage() {
    const router = useRouter();
    const setAuth = useAuthStore((state) => state.setAuth);

    // Estados del formulario
    const [cedula, setCedula] = useState('');
    const [contrasenia, setContrasenia] = useState('');

    // Estados de validación y UI
    const [errores, setErrores] = useState({
        cedula: '',
        contrasenia: '',
        general: '',
    });
    const [isLoading, setIsLoading] = useState(false);

    /**
     * Valida los campos del formulario
     */
    const validarFormulario = (): boolean => {
        const nuevosErrores = {
            cedula: '',
            contrasenia: '',
            general: '',
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
        e.stopPropagation(); // Prevenir propagación del evento

        // Limpiar error general previo
        setErrores(prev => ({ ...prev, general: '' }));

        // Validar formulario
        if (!validarFormulario()) {
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
                    setErrores(prev => ({
                        ...prev,
                        general: 'Acceso denegado. Solo administradores pueden ingresar.',
                    }));
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

                // Redirigir según el rol
                if (rol === 'SAD') {
                    router.push('/superadmin/dashboard');
                } else {
                    router.push('/admin/dashboard');
                }
            } else {
                // Respuesta no exitosa
                setErrores(prev => ({
                    ...prev,
                    general: response.message || 'Error al iniciar sesión',
                }));
                setIsLoading(false);
            }
        } catch (error: any) {
            // Manejo de errores
            let mensajeError = 'Error al iniciar sesión';

            // El backend devuelve ApiResponse con { status: 'fail', message, error }
            if (error.status === 'fail' || error.status === 'error') {
                mensajeError = error.message || 'Credenciales inválidas';
            } else if (error.message && typeof error.message === 'string') {
                mensajeError = error.message;
            } else if (error.response?.data?.message) {
                mensajeError = error.response.data.message;
            } else {
                mensajeError = MENSAJES_ERROR.NETWORK_ERROR;
            }

            setErrores(prev => ({ ...prev, general: mensajeError }));
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

                    {/* Mensaje de error general */}
                    {errores.general && (
                        <div className="mb-6 p-4 bg-red-50 border border-rojo-intenso rounded-lg">
                            <p className="text-rojo-intenso text-sm font-medium flex items-center gap-2">
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="h-5 w-5"
                                    viewBox="0 0 20 20"
                                    fill="currentColor"
                                >
                                    <path
                                        fillRule="evenodd"
                                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                                        clipRule="evenodd"
                                    />
                                </svg>
                                {errores.general}
                            </p>
                        </div>
                    )}

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
