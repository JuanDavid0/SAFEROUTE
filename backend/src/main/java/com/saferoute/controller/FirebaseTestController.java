package com.saferoute.controller;

import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IFirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para pruebas de Firebase Storage
 * Este controlador es temporal para probar la funcionalidad de subida de
 * imágenes
 */
@RestController
@RequestMapping("test/firebase")
@RequiredArgsConstructor
@Slf4j
public class FirebaseTestController {

        private final IFirebaseStorageService firebaseStorageService;

        /**
         * Endpoint de prueba para subir una imagen
         * POST /api/test/firebase/upload?idProducto=1
         * 
         * @param imagen     Archivo de imagen (form-data con key "imagen")
         * @param idProducto ID del producto (query param)
         * @return URL de la imagen subida
         */
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('SAD', 'ADM')")
        public ResponseEntity<ApiResponse<Map<String, String>>> subirImagen(
                        @RequestParam("imagen") MultipartFile imagen,
                        @RequestParam("idProducto") Integer idProducto) {

                log.info("📤 Solicitud de subida de imagen para producto ID: {}", idProducto);

                String urlImagen = firebaseStorageService.subirImagenProducto(imagen, idProducto);

                Map<String, String> data = new HashMap<>();
                data.put("urlImagen", urlImagen);
                data.put("mensaje", "Imagen subida exitosamente");

                ApiResponse<Map<String, String>> response = ApiResponse.success(
                                data,
                                "Imagen subida correctamente");

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Endpoint de prueba para eliminar una imagen
         * DELETE /api/test/firebase/delete
         * 
         * @param urlImagen URL de la imagen a eliminar (request param)
         * @return Confirmación de eliminación
         */
        @DeleteMapping("/delete")
        @PreAuthorize("hasAnyRole('SAD', 'ADM')")
        public ResponseEntity<ApiResponse<Map<String, String>>> eliminarImagen(
                        @RequestParam("urlImagen") String urlImagen) {

                log.info("🗑️ Solicitud de eliminación de imagen: {}", urlImagen);

                firebaseStorageService.eliminarImagen(urlImagen);

                Map<String, String> data = new HashMap<>();
                data.put("mensaje", "Imagen eliminada exitosamente");

                ApiResponse<Map<String, String>> response = ApiResponse.success(
                                data,
                                "Imagen eliminada correctamente");

                return ResponseEntity.ok(response);
        }

        /**
         * Endpoint de prueba para actualizar una imagen
         * PUT /api/test/firebase/update?idProducto=1&urlAnterior=https://...
         * 
         * @param imagen      Nueva imagen
         * @param idProducto  ID del producto
         * @param urlAnterior URL de la imagen anterior (opcional)
         * @return URL de la nueva imagen
         */
        @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('SAD', 'ADM')")
        public ResponseEntity<ApiResponse<Map<String, String>>> actualizarImagen(
                        @RequestParam("imagen") MultipartFile imagen,
                        @RequestParam("idProducto") Integer idProducto,
                        @RequestParam(value = "urlAnterior", required = false) String urlAnterior) {

                log.info("🔄 Solicitud de actualización de imagen para producto ID: {}", idProducto);

                String urlNueva = firebaseStorageService.actualizarImagenProducto(imagen, idProducto, urlAnterior);

                Map<String, String> data = new HashMap<>();
                data.put("urlNueva", urlNueva);
                data.put("urlAnterior", urlAnterior);
                data.put("mensaje", "Imagen actualizada exitosamente");

                ApiResponse<Map<String, String>> response = ApiResponse.success(
                                data,
                                "Imagen actualizada correctamente");

                return ResponseEntity.ok(response);
        }
}
