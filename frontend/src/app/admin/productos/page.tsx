/**
 * Productos - Administrador
 * Gestión completa de productos (CRUD + Imágenes)
 */

'use client';

import React, { useState, useEffect } from 'react';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { Button, Input, Card } from '@/components/ui';
import { Producto } from '@/types';
import * as productosService from '@/services/productosService';

export default function ProductosPage() {
    // Estados del formulario
    const [nombreProducto, setNombreProducto] = useState('');
    const [tipoProducto, setTipoProducto] = useState('');
    const [descripcionProducto, setDescripcionProducto] = useState('');
    const [precioUnitario, setPrecioUnitario] = useState('');
    const [costoUnitario, setCostoUnitario] = useState('');
    const [imagenSeleccionada, setImagenSeleccionada] = useState<File | null>(null);
    const [previewImagen, setPreviewImagen] = useState<string>('');

    // Estado de búsqueda
    const [busqueda, setBusqueda] = useState('');

    // Estados de la UI
    const [mostrarFormulario, setMostrarFormulario] = useState(false);
    const [modoEdicion, setModoEdicion] = useState(false);
    const [productoEditando, setProductoEditando] = useState<Producto | null>(null);

    // Estados de datos
    const [productos, setProductos] = useState<Producto[]>([]);
    const [cargando, setCargando] = useState(false);
    const [mensaje, setMensaje] = useState<{ tipo: 'success' | 'error'; texto: string } | null>(null);

    // Cargar productos al montar el componente
    useEffect(() => {
        cargarProductos();
    }, []);

    const cargarProductos = async () => {
        try {
            setCargando(true);
            const data = await productosService.obtenerProductos();
            setProductos(data);
        } catch (error) {
            mostrarMensaje('error', 'Error al cargar productos');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const mostrarMensaje = (tipo: 'success' | 'error', texto: string) => {
        setMensaje({ tipo, texto });
        setTimeout(() => setMensaje(null), 5000);
    };

    const handleSeleccionarImagen = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setImagenSeleccionada(file);
            // Crear preview
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreviewImagen(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleLimpiarFormulario = () => {
        setNombreProducto('');
        setTipoProducto('');
        setDescripcionProducto('');
        setPrecioUnitario('');
        setCostoUnitario('');
        setImagenSeleccionada(null);
        setPreviewImagen('');
        setModoEdicion(false);
        setProductoEditando(null);
    };

    const validarFormulario = (): string | null => {
        if (!nombreProducto.trim()) return 'El nombre del producto es obligatorio';
        if (!tipoProducto.trim()) return 'El tipo de producto es obligatorio';
        if (!precioUnitario || parseFloat(precioUnitario) <= 0) return 'El precio debe ser mayor a 0';
        if (!costoUnitario || parseFloat(costoUnitario) <= 0) return 'El costo debe ser mayor a 0';
        return null;
    };

    const handleCrearProducto = async () => {
        const error = validarFormulario();
        if (error) {
            mostrarMensaje('error', error);
            return;
        }

        try {
            setCargando(true);

            const productoData = {
                nombreProducto: nombreProducto.trim(),
                tipoProducto: tipoProducto.trim(),
                descripcionProducto: descripcionProducto.trim(),
                precioUnitario: parseFloat(precioUnitario),
                costoUnitario: parseFloat(costoUnitario),
                estadoProducto: 'ACTIVO'
            };

            if (modoEdicion && productoEditando) {
                // Actualizar producto
                if (imagenSeleccionada) {
                    await productosService.actualizarProductoConImagen(
                        productoEditando.idProducto,
                        productoData,
                        imagenSeleccionada
                    );
                } else {
                    await productosService.actualizarProductoSinImagen(
                        productoEditando.idProducto,
                        productoData
                    );
                }
                mostrarMensaje('success', 'Producto actualizado exitosamente');
            } else {
                // Crear producto
                if (imagenSeleccionada) {
                    await productosService.crearProductoConImagen(productoData, imagenSeleccionada);
                } else {
                    await productosService.crearProductoSinImagen(productoData);
                }
                mostrarMensaje('success', 'Producto creado exitosamente');
            }

            handleLimpiarFormulario();
            setMostrarFormulario(false);
            await cargarProductos();
        } catch (error: any) {
            mostrarMensaje('error', error.message || 'Error al guardar producto');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const handleEditarProducto = (producto: Producto) => {
        setNombreProducto(producto.nombreProducto);
        setTipoProducto(producto.tipoProducto);
        setDescripcionProducto(producto.descripcionProducto || '');
        setPrecioUnitario(producto.precioUnitario.toString());
        setCostoUnitario(producto.costoUnitario.toString());
        setPreviewImagen(producto.urlImagen || '');
        setImagenSeleccionada(null);
        setModoEdicion(true);
        setProductoEditando(producto);
        setMostrarFormulario(true);
    };

    const handleEliminarProducto = async (id: number) => {
        if (!confirm('¿Estás seguro de eliminar este producto?')) return;

        try {
            setCargando(true);
            await productosService.eliminarProducto(id);
            mostrarMensaje('success', 'Producto eliminado exitosamente');
            await cargarProductos();
        } catch (error: any) {
            mostrarMensaje('error', error.message || 'Error al eliminar producto');
            console.error('Error:', error);
        } finally {
            setCargando(false);
        }
    };

    const productosFiltrados = productos.filter(p =>
        p.nombreProducto.toLowerCase().includes(busqueda.toLowerCase())
    );

    return (
        <DashboardLayout role="ADM">
            {/* Encabezado */}
            <div className="dashboard-page-header">
                <div className="flex items-center justify-between">
                    <h1 className="dashboard-page-title">Gestión de Productos</h1>
                    <Button
                        variant="primary"
                        onClick={() => {
                            if (mostrarFormulario) {
                                handleLimpiarFormulario();
                            }
                            setMostrarFormulario(!mostrarFormulario);
                        }}
                    >
                        {mostrarFormulario ? '✕ Cancelar' : '+ Agregar Producto'}
                    </Button>
                </div>
            </div>

            <div className="dashboard-content">
                {/* Mensaje de feedback */}
                {mensaje && (
                    <div className={`alert alert-${mensaje.tipo}`}>
                        {mensaje.texto}
                    </div>
                )}

                {/* Barra de búsqueda */}
                <div className="productos-search-bar">
                    <div className="productos-search-container">
                        <svg
                            className="productos-search-icon"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                            />
                        </svg>
                        <input
                            type="text"
                            className="productos-search-input"
                            placeholder="Buscar producto por nombre..."
                            value={busqueda}
                            onChange={(e) => setBusqueda(e.target.value)}
                        />
                    </div>
                </div>

                {/* Formulario de creación (condicional) */}
                {mostrarFormulario && (
                    <div className="productos-form-card">
                        <h2 className="productos-form-title">
                            {modoEdicion ? 'Editar Producto' : 'Nuevo Producto'}
                        </h2>

                        <div className="productos-form-grid">
                            {/* Columna izquierda: Campos */}
                            <div className="productos-form-fields">
                                <div className="form-group">
                                    <label className="form-label">Nombre del Producto *</label>
                                    <Input
                                        type="text"
                                        placeholder="Ej: Leche Deslactosada"
                                        value={nombreProducto}
                                        onChange={(e) => setNombreProducto(e.target.value)}
                                    />
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Tipo de Producto *</label>
                                    <Input
                                        type="text"
                                        placeholder="Ej: Lácteos, Panadería, Cereales..."
                                        value={tipoProducto}
                                        onChange={(e) => setTipoProducto(e.target.value)}
                                    />
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Descripción</label>
                                    <textarea
                                        className="form-textarea"
                                        placeholder="Descripción del producto..."
                                        rows={3}
                                        value={descripcionProducto}
                                        onChange={(e) => setDescripcionProducto(e.target.value)}
                                    />
                                </div>

                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Precio Unitario ($) *</label>
                                        <Input
                                            type="number"
                                            step="0.01"
                                            placeholder="0.00"
                                            value={precioUnitario}
                                            onChange={(e) => setPrecioUnitario(e.target.value)}
                                        />
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Costo Unitario ($) *</label>
                                        <Input
                                            type="number"
                                            step="0.01"
                                            placeholder="0.00"
                                            value={costoUnitario}
                                            onChange={(e) => setCostoUnitario(e.target.value)}
                                        />
                                    </div>
                                </div>
                            </div>

                            {/* Columna derecha: Imagen */}
                            <div className="productos-image-upload">
                                <label className="form-label">Imagen del Producto</label>

                                {previewImagen ? (
                                    <div className="image-preview-container">
                                        <img
                                            src={previewImagen}
                                            alt="Preview"
                                            className="image-preview"
                                        />
                                        <button
                                            className="image-remove-btn"
                                            onClick={() => {
                                                setImagenSeleccionada(null);
                                                setPreviewImagen('');
                                            }}
                                        >
                                            ✕ Quitar imagen
                                        </button>
                                    </div>
                                ) : (
                                    <div className="image-upload-placeholder">
                                        <svg
                                            className="image-upload-icon"
                                            fill="none"
                                            stroke="currentColor"
                                            viewBox="0 0 24 24"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={2}
                                                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                            />
                                        </svg>
                                        <p className="image-upload-text">Sin imagen</p>
                                    </div>
                                )}

                                <input
                                    type="file"
                                    id="imagen-upload"
                                    className="hidden"
                                    accept="image/jpeg,image/jpg,image/png,image/webp"
                                    onChange={handleSeleccionarImagen}
                                />
                                <label htmlFor="imagen-upload" className="btn-select-image">
                                    📷 Seleccionar imagen
                                </label>
                                <p className="image-upload-hint">
                                    JPG, JPEG, PNG, WEBP (Máx. 10MB)
                                </p>
                            </div>
                        </div>

                        {/* Botones de acción */}
                        <div className="productos-form-actions">
                            <Button
                                variant="outline"
                                onClick={handleLimpiarFormulario}
                                disabled={cargando}
                            >
                                Limpiar
                            </Button>
                            <Button
                                variant="primary"
                                onClick={handleCrearProducto}
                                disabled={cargando}
                            >
                                {cargando ? '⏳ Guardando...' : modoEdicion ? '✓ Actualizar' : '✓ Crear Producto'}
                            </Button>
                        </div>
                    </div>
                )}

                {/* Listado de productos */}
                <div className="productos-list-section">
                    <h2 className="productos-list-title">
                        Productos Registrados ({productosFiltrados.length})
                    </h2>

                    {cargando && !mostrarFormulario ? (
                        <div className="productos-empty-state">
                            <p>⏳ Cargando productos...</p>
                        </div>
                    ) : productosFiltrados.length === 0 ? (
                        <div className="productos-empty-state">
                            <p>No se encontraron productos</p>
                        </div>
                    ) : (
                        <div className="productos-grid">
                            {productosFiltrados.map((producto) => (
                                <div key={producto.idProducto} className="producto-card">
                                    {/* Imagen */}
                                    <div className="producto-image-container">
                                        {producto.urlImagen ? (
                                            <img
                                                src={producto.urlImagen}
                                                alt={producto.nombreProducto}
                                                className="producto-image"
                                            />
                                        ) : (
                                            <div className="producto-no-image">
                                                <span>Sin imagen</span>
                                            </div>
                                        )}
                                    </div>

                                    {/* Información */}
                                    <div className="producto-info">
                                        <h3 className="producto-nombre">{producto.nombreProducto}</h3>
                                        <span className="producto-tipo">{producto.tipoProducto}</span>
                                        <p className="producto-descripcion">{producto.descripcionProducto || 'Sin descripción'}</p>

                                        <div className="producto-precios">
                                            <div className="producto-precio">
                                                <span className="precio-label">Precio:</span>
                                                <span className="precio-valor">${producto.precioUnitario.toFixed(2)}</span>
                                            </div>
                                            <div className="producto-costo">
                                                <span className="costo-label">Costo:</span>
                                                <span className="costo-valor">${producto.costoUnitario.toFixed(2)}</span>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Acciones */}
                                    <div className="producto-actions">
                                        <button
                                            className="btn-action btn-edit"
                                            onClick={() => handleEditarProducto(producto)}
                                            disabled={cargando}
                                        >
                                            ✏️ Editar
                                        </button>
                                        <button
                                            className="btn-action btn-delete"
                                            onClick={() => handleEliminarProducto(producto.idProducto)}
                                            disabled={cargando}
                                        >
                                            🗑️ Eliminar
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </DashboardLayout>
    );
}
