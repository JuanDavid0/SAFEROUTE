package com.saferoute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saferoute.dto.ProductoDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IProductoService;

import jakarta.validation.Valid;
import jakarta.validation.Validator;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final IProductoService productoService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProductoController(IProductoService productoService,
            ObjectMapper objectMapper,
            Validator validator) {
        this.productoService = productoService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Crear producto CON imagen
     * Se envía como multipart/form-data
     */
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductoDTO>> crearProducto(
            @RequestParam("producto") String productoJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {

        // Convertir JSON string a DTO
        ProductoDTO productoDTO = objectMapper.readValue(productoJson, ProductoDTO.class);

        // Validar manualmente
        var violations = validator.validate(productoDTO);
        if (!violations.isEmpty()) {
            StringBuilder errMsg = new StringBuilder();
            violations.forEach(v -> errMsg.append(v.getPropertyPath())
                    .append(": ").append(v.getMessage()).append("; "));
            throw new IllegalArgumentException("Errores de validación: " + errMsg.toString());
        }

        ProductoDTO producto = productoService.crearProductoConImagen(productoDTO, imagen);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto creado exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Crear producto SIN imagen (para compatibilidad hacia atrás)
     * Se envía como application/json
     */
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping(value = "/sin-imagen", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductoDTO>> crearProductoSinImagen(
            @Valid @RequestBody ProductoDTO productoDTO) {

        ProductoDTO producto = productoService.crearProducto(productoDTO);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto creado exitosamente (sin imagen)");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoDTO>>> listarProductos() {
        List<ProductoDTO> productos = productoService.listarProductos();

        ApiResponse<List<ProductoDTO>> response = ApiResponse.success(
                productos,
                "Se encontraron " + productos.size() + " producto(s)");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoDTO>> obtenerProducto(@PathVariable Integer id) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto encontrado");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<ProductoDTO>> buscarProductoPorNombre(@RequestParam String nombre) {
        ProductoDTO producto = productoService.buscarProductoPorNombre(nombre);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto encontrado por nombre");

        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar producto CON imagen opcional
     * Se envía como multipart/form-data
     */
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductoDTO>> actualizarProducto(
            @PathVariable Integer id,
            @RequestParam("producto") String productoJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {

        // Convertir JSON string a DTO
        ProductoDTO productoDTO = objectMapper.readValue(productoJson, ProductoDTO.class);

        // Validar manualmente
        var violations = validator.validate(productoDTO);
        if (!violations.isEmpty()) {
            StringBuilder errMsg = new StringBuilder();
            violations.forEach(v -> errMsg.append(v.getPropertyPath())
                    .append(": ").append(v.getMessage()).append("; "));
            throw new IllegalArgumentException("Errores de validación: " + errMsg.toString());
        }

        ProductoDTO producto = productoService.actualizarProductoConImagen(id, productoDTO, imagen);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto actualizado exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar producto SIN cambiar imagen (para compatibilidad hacia atrás)
     * Se envía como application/json
     */
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping(value = "/{id}/sin-imagen", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductoDTO>> actualizarProductoSinImagen(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoDTO productoDTO) {

        ProductoDTO producto = productoService.actualizarProducto(id, productoDTO);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto actualizado exitosamente (sin cambiar imagen)");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);

        ApiResponse<Void> response = ApiResponse.success("Producto eliminado exitosamente");

        return ResponseEntity.ok(response);
    }
}