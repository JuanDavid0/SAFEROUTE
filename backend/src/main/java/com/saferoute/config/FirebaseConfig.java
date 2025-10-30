package com.saferoute.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.saferoute.constants.FirebaseConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuración de Firebase Storage
 * Solo se inicializa si el archivo de credenciales existe
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path:https://mi-firebase-credenciales-bucket.s3.us-east-2.amazonaws.com/firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.storage.bucket:}")
    private String bucketName;

    /**
     * Verifica si el archivo de credenciales existe en el classpath
     */
    private boolean credentialsFileExists() {
        try {
            // Primero intentar como recurso del classpath
            Resource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                return true;
            }

            // Si no está en classpath, intentar como archivo del sistema
            File credentialsFile = new File(credentialsPath);
            boolean exists = credentialsFile.exists();

            if (!exists) {
                log.warn(" Firebase credentials file not found at: {}", credentialsPath);
                log.warn(" Firebase Storage will NOT be initialized.");
                log.warn(" To enable Firebase: place firebase-service-account.json in src/main/resources/");
            }

            return exists;
        } catch (Exception e) {
            log.warn(" Error checking Firebase credentials file", e);
            return false;
        }
    }

    /**
     * Inicializa Firebase App solo si el archivo de credenciales existe
     */
    @Bean
    public FirebaseApp firebaseApp() {
        if (!credentialsFileExists()) {
            log.info(" Firebase App initialization SKIPPED (credentials file not found)");
            return null;
        }

        try {
            InputStream serviceAccount;

            // Intentar cargar desde classpath primero (más confiable)
            try {
                Resource resource = new ClassPathResource("firebase-service-account.json");
                if (resource.exists()) {
                    serviceAccount = resource.getInputStream();
                    log.info("Loading Firebase credentials from classpath");
                } else {
                    // Fallback a ruta del sistema de archivos
                    serviceAccount = new FileInputStream(credentialsPath);
                    log.info("Loading Firebase credentials from file system: {}", credentialsPath);
                }
            } catch (Exception e) {
                // Último intento con ruta del sistema
                serviceAccount = new FileInputStream(credentialsPath);
                log.info("Loading Firebase credentials from file system: {}", credentialsPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(bucketName)
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info(FirebaseConstants.LOG_FIREBASE_INICIALIZADO);
            log.info("🪣 Storage bucket: {}", bucketName);

            return app;

        } catch (Exception e) {
            log.error(FirebaseConstants.ERROR_INICIALIZAR_FIREBASE, e);
            log.warn(" Firebase App will NOT be available");
            return null;
        }
    }

    /**
     * Proporciona el cliente de Storage solo si Firebase App se inicializó
     * correctamente
     */
    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public Storage storage() {
        if (!credentialsFileExists()) {
            log.info(" Firebase Storage initialization SKIPPED (credentials file not found)");
            return null;
        }

        try {
            FileInputStream serviceAccount = new FileInputStream(credentialsPath);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            return StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

        } catch (Exception e) {
            log.error(" Error initializing Firebase Storage", e);
            log.warn(" Firebase Storage will NOT be available");
            return null;
        }
    }
}