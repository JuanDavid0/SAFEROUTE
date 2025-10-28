package com.saferoute.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.saferoute.constants.FirebaseConstants;
import com.saferoute.exception.FirebaseStorageException;
import com.saferoute.helper.FirebaseStorageHelper;
import com.saferoute.service.interfaces.IFirebaseStorageService;
import com.saferoute.validator.ImagenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación del servicio de Firebase Storage
 */
@Service
@Slf4j
public class FirebaseStorageServiceImpl implements IFirebaseStorageService {

    private final Storage storage;
    private final ImagenValidator validator;
    private final FirebaseStorageHelper helper;

    @Autowired
    public FirebaseStorageServiceImpl(
            @Autowired(required = false) Storage storage,
            ImagenValidator validator,
            @Autowired(required = false) FirebaseStorageHelper helper) {
        this.storage = storage;
        this.validator = validator;
        this.helper = helper;
    }

    private void verificarFirebaseDisponible() {
        if (storage == null || helper == null) {
            throw new FirebaseStorageException(
                    "Firebase Storage no está configurado. Coloca el archivo firebase-service-account.json en src/main/resources/");
        }
    }

    @Override
    public String subirImagenProducto(MultipartFile archivo, Integer idProducto) {
        verificarFirebaseDisponible();

        try {
            log.info(FirebaseConstants.LOG_SUBIENDO_IMAGEN, archivo.getOriginalFilename());

            // Validar archivo
            validator.validarArchivo(archivo);

            // Construir nombre y ruta
            String nombreArchivo = helper.construirNombreArchivo(archivo, idProducto);
            String rutaCompleta = helper.construirRutaCompleta(nombreArchivo);

            // Subir archivo
            BlobInfo blobInfo = helper.construirBlobInfo(
                    rutaCompleta,
                    helper.obtenerContentType(archivo));

            Blob blob = storage.create(blobInfo, helper.leerBytesArchivo(archivo));

            // Hacer el archivo público para que sea accesible sin autenticación
            blob.createAcl(com.google.cloud.storage.Acl.of(
                    com.google.cloud.storage.Acl.User.ofAllUsers(),
                    com.google.cloud.storage.Acl.Role.READER));

            // Construir URL pública
            String urlPublica = helper.construirUrlPublica(rutaCompleta);

            log.info(FirebaseConstants.LOG_IMAGEN_SUBIDA, urlPublica);
            return urlPublica;

        } catch (FirebaseStorageException e) {
            log.error(FirebaseConstants.ERROR_SUBIR_ARCHIVO, e);
            throw e;
        } catch (Exception e) {
            log.error(FirebaseConstants.ERROR_SUBIR_ARCHIVO, e);
            throw new FirebaseStorageException(FirebaseConstants.ERROR_SUBIR_ARCHIVO, e);
        }
    }

    @Override
    public void eliminarImagen(String urlImagen) {
        verificarFirebaseDisponible();

        if (urlImagen == null || urlImagen.isEmpty()) {
            return; // No hay imagen que eliminar
        }

        try {
            log.info(FirebaseConstants.LOG_ELIMINANDO_IMAGEN, urlImagen);

            Blob blob = helper.obtenerBlob(urlImagen);

            if (blob != null && blob.exists()) {
                blob.delete();
                log.info(FirebaseConstants.LOG_IMAGEN_ELIMINADA);
            } else {
                log.warn(" El archivo no existe en Firebase Storage: {}", urlImagen);
            }

        } catch (Exception e) {
            log.error(FirebaseConstants.ERROR_ELIMINAR_ARCHIVO, e);
            // No lanzamos excepción para no interrumpir el flujo si la imagen no existe
        }
    }

    @Override
    public String actualizarImagenProducto(MultipartFile archivo, Integer idProducto, String urlImagenAnterior) {
        // Eliminar imagen anterior si existe
        if (urlImagenAnterior != null && !urlImagenAnterior.isEmpty()) {
            eliminarImagen(urlImagenAnterior);
        }

        // Subir nueva imagen
        return subirImagenProducto(archivo, idProducto);
    }
}
