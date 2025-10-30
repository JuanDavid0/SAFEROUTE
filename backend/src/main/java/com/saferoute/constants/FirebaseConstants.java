package com.saferoute.constants;

/**
 * Constantes para el servicio de Firebase Storage
 */
public class FirebaseConstants {

    // Configuración
    public static final String BUCKET_NAME_PROPERTY = "firebase.storage.bucket";
    public static final String CREDENTIALS_PATH_PROPERTY = "firebase.credentials.path";

    // Rutas de almacenamiento
    public static final String PRODUCTOS_FOLDER = "productos/";

    // Formatos permitidos
    public static final String[] ALLOWED_EXTENSIONS = { "jpg", "jpeg", "png", "webp" };
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // Mensajes de error
    public static final String ERROR_ARCHIVO_VACIO = "El archivo está vacío";
    public static final String ERROR_EXTENSION_NO_PERMITIDA = "Extensión de archivo no permitida. Solo se permiten: jpg, jpeg, png, webp";
    public static final String ERROR_TAMANO_EXCEDIDO = "El tamaño del archivo excede el límite de 10MB";
    public static final String ERROR_SUBIR_ARCHIVO = "Error al subir el archivo a Firebase Storage";
    public static final String ERROR_ELIMINAR_ARCHIVO = "Error al eliminar el archivo de Firebase Storage";
    public static final String ERROR_INICIALIZAR_FIREBASE = "Error al inicializar Firebase";

    // Mensajes de log
    public static final String LOG_SUBIENDO_IMAGEN = "📤 Subiendo imagen de producto: {}";
    public static final String LOG_IMAGEN_SUBIDA = " Imagen subida exitosamente: {}";
    public static final String LOG_ELIMINANDO_IMAGEN = "🗑️ Eliminando imagen: {}";
    public static final String LOG_IMAGEN_ELIMINADA = " Imagen eliminada exitosamente";
    public static final String LOG_FIREBASE_INICIALIZADO = " Firebase Storage inicializado correctamente";

    private FirebaseConstants() {
        // Clase de constantes, no debe ser instanciada
    }
}
