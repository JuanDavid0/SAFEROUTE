package com.saferoute.controller;

import com.saferoute.dto.ProductoDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IProductoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final IProductoService productoService;

    public ProductoController(IProductoService productoService) {
        this.productoService = productoService;
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoDTO>> crearProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO producto = productoService.crearProducto(productoDTO);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto creado exitosamente");

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

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
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

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoDTO>> actualizarProducto(@PathVariable Integer id,
            @Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO producto = productoService.actualizarProducto(id, productoDTO);

        ApiResponse<ProductoDTO> response = ApiResponse.success(
                producto,
                "Producto actualizado exitosamente");

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