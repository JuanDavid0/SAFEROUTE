package com.saferoute.controller;

import com.saferoute.dto.ProductoDTO;
import com.saferoute.service.interfaces.IProductoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final IProductoService productoService;

    public ProductoController(IProductoService productoService) {
        this.productoService = productoService;
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping
    public ProductoDTO crearProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        return productoService.crearProducto(productoDTO);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping
    public List<ProductoDTO> listarProductos() {
        return productoService.listarProductos();
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping("/{id}")
    public ProductoDTO obtenerProducto(@PathVariable Integer id) {
        return productoService.obtenerProductoPorId(id);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping("/{id}")
    public ProductoDTO actualizarProducto(@PathVariable Integer id,
                                          @Valid @RequestBody ProductoDTO productoDTO) {
        return productoService.actualizarProducto(id, productoDTO);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado exitosamente"));
    }
}
