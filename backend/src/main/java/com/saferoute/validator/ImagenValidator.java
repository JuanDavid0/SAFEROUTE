package com.saferoute.validator;

import com.saferoute.constants.FirebaseConstants;
import com.saferoute.exception.FirebaseStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * Validador para archivos de imágenes
 */
@Component
@Slf4j
public class ImagenValidator {

    /**
     * Valida que el archivo sea válido para subir
     */
    public void validarArchivo(MultipartFile archivo) {
        validarArchivoNoVacio(archivo);
        validarExtension(archivo);
        validarTamano(archivo);
    }

    /**
     * Valida que el archivo no esté vacío
     */
    private void validarArchivoNoVacio(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new FirebaseStorageException(FirebaseConstants.ERROR_ARCHIVO_VACIO);
        }
    }

    /**
     * Valida que la extensión del archivo sea permitida
     */
    private void validarExtension(MultipartFile archivo) {
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null) {
            throw new FirebaseStorageException(FirebaseConstants.ERROR_ARCHIVO_VACIO);
        }

        String extension = obtenerExtension(nombreArchivo);
        boolean extensionPermitida = Arrays.asList(FirebaseConstants.ALLOWED_EXTENSIONS)
                .contains(extension.toLowerCase());

        if (!extensionPermitida) {
            throw new FirebaseStorageException(FirebaseConstants.ERROR_EXTENSION_NO_PERMITIDA);
        }
    }

    /**
     * Valida que el tamaño del archivo no exceda el límite
     */
    private void validarTamano(MultipartFile archivo) {
        if (archivo.getSize() > FirebaseConstants.MAX_FILE_SIZE) {
            throw new FirebaseStorageException(FirebaseConstants.ERROR_TAMANO_EXCEDIDO);
        }
    }

    /**
     * Obtiene la extensión de un archivo
     */
    public String obtenerExtension(String nombreArchivo) {
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        if (ultimoPunto > 0) {
            return nombreArchivo.substring(ultimoPunto + 1);
        }
        return "";
    }
}
