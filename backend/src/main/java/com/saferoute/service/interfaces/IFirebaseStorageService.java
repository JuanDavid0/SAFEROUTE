package com.saferoute.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz para el servicio de Firebase Storage
 */
public interface IFirebaseStorageService {

    /**
     * Sube una imagen de producto a Firebase Storage
     * 
     * @param archivo    Archivo de imagen
     * @param idProducto ID del producto
     * @return URL pública de la imagen subida
     */
    String subirImagenProducto(MultipartFile archivo, Integer idProducto);

    /**
     * Elimina una imagen de Firebase Storage
     * 
     * @param urlImagen URL de la imagen a eliminar
     */
    void eliminarImagen(String urlImagen);

    /**
     * Actualiza la imagen de un producto (elimina la anterior y sube la nueva)
     * 
     * @param archivo           Nueva imagen
     * @param idProducto        ID del producto
     * @param urlImagenAnterior URL de la imagen anterior (puede ser null)
     * @return URL pública de la nueva imagen
     */
    String actualizarImagenProducto(MultipartFile archivo, Integer idProducto, String urlImagenAnterior);
}
