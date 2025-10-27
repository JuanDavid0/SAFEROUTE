package com.saferoute.helper;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.saferoute.constants.FirebaseConstants;
import com.saferoute.validator.ImagenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Helper para operaciones de Firebase Storage
 * Maneja la lógica de construcción de nombres y rutas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseStorageHelper {

    private final Storage storage;
    private final ImagenValidator imagenValidator;

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    /**
     * Construye el nombre único del archivo
     */
    public String construirNombreArchivo(MultipartFile archivo, Integer idProducto) {
        String extension = imagenValidator.obtenerExtension(archivo.getOriginalFilename());
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("producto_%d_%s_%s.%s", idProducto, timestamp, uuid, extension);
    }

    /**
     * Construye la ruta completa del archivo en Firebase
     */
    public String construirRutaCompleta(String nombreArchivo) {
        return FirebaseConstants.PRODUCTOS_FOLDER + nombreArchivo;
    }

    /**
     * Construye el BlobInfo para subir el archivo
     */
    public BlobInfo construirBlobInfo(String rutaCompleta, String contentType) {
        return BlobInfo.newBuilder(bucketName, rutaCompleta)
                .setContentType(contentType)
                .build();
    }

    /**
     * Construye la URL pública del archivo
     */
    public String construirUrlPublica(String rutaCompleta) {
        return String.format(
                "https://storage.googleapis.com/%s/%s",
                bucketName,
                rutaCompleta);
    }

    /**
     * Obtiene el Blob de Firebase Storage
     */
    public Blob obtenerBlob(String urlImagen) {
        String rutaArchivo = extraerRutaDeUrl(urlImagen);
        return storage.get(bucketName, rutaArchivo);
    }

    /**
     * Extrae la ruta del archivo desde la URL
     */
    public String extraerRutaDeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Formato: https://storage.googleapis.com/bucket-name/productos/archivo.jpg
        String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);

        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }

        return null;
    }

    /**
     * Lee los bytes del archivo
     */
    public byte[] leerBytesArchivo(MultipartFile archivo) throws IOException {
        return archivo.getBytes();
    }

    /**
     * Obtiene el content type del archivo
     */
    public String obtenerContentType(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        return contentType != null ? contentType : "application/octet-stream";
    }
}
