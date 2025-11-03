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
import java.net.URL;

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
     * Obtiene el InputStream de las credenciales desde diferentes fuentes
     */
    private InputStream getCredentialsInputStream() throws IOException {
        // 1. Intentar desde classpath
        try {
            Resource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                log.info("Loading Firebase credentials from classpath");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            log.debug("Credentials not found in classpath, trying other sources...");
        }

        // 2. Si es una URL (comienza con http:// o https://)
        if (credentialsPath.startsWith("http://") || credentialsPath.startsWith("https://")) {
            log.info("Loading Firebase credentials from URL: {}", credentialsPath);
            URL url = new URL(credentialsPath);
            return url.openStream();
        }

        // 3. Intentar desde filesystem
        File credentialsFile = new File(credentialsPath);
        if (credentialsFile.exists()) {
            log.info("Loading Firebase credentials from file system: {}", credentialsPath);
            return new FileInputStream(credentialsFile);
        }

        throw new IOException("Firebase credentials not found in any location: " + credentialsPath);
    }

    /**
     * Verifica si las credenciales están disponibles
     */
    private boolean credentialsAvailable() {
        try {
            InputStream stream = getCredentialsInputStream();
            stream.close();
            return true;
        } catch (Exception e) {
            log.warn("Firebase credentials not available: {}", e.getMessage());
            log.warn("Firebase Storage will NOT be initialized.");
            return false;
        }
    }

    /**
     * Inicializa Firebase App solo si el archivo de credenciales existe
     */
    @Bean
    public FirebaseApp firebaseApp() {
        if (!credentialsAvailable()) {
            log.info("Firebase App initialization SKIPPED (credentials not available)");
            return null;
        }

        try {
            InputStream serviceAccount = getCredentialsInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(bucketName)
                    .build();

            serviceAccount.close();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info(FirebaseConstants.LOG_FIREBASE_INICIALIZADO);
            log.info("Storage bucket: {}", bucketName);

            return app;

        } catch (Exception e) {
            log.error(FirebaseConstants.ERROR_INICIALIZAR_FIREBASE, e);
            log.warn("Firebase App will NOT be available");
            return null;
        }
    }

    /**
     * Proporciona el cliente de Storage solo si Firebase App se inicializó correctamente
     */
    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public Storage storage() {
        if (!credentialsAvailable()) {
            log.info("Firebase Storage initialization SKIPPED (credentials not available)");
            return null;
        }

        try {
            InputStream serviceAccount = getCredentialsInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            serviceAccount.close();

            Storage storageInstance = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            log.info("Firebase Storage initialized successfully");
            return storageInstance;

        } catch (Exception e) {
            log.error("Error initializing Firebase Storage", e);
            log.warn("Firebase Storage will NOT be available");
            return null;
        }
    }
}