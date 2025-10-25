package com.saferoute.controller;

import com.saferoute.dto.PedidoDTO;
import com.saferoute.dto.ProductoPedidoDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IPedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final IPedidoService pedidoService;

    public PedidoController(IPedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping("/{idAdmin}")
    public ResponseEntity<ApiResponse<PedidoDTO>> crearPedido(
            @PathVariable Integer idAdmin,
            @Valid @RequestBody PedidoDTO dto) {
        PedidoDTO pedido = pedidoService.crearPedido(dto, idAdmin);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Pedido creado exitosamente");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoDTO>>> listarPedidos() {
        List<PedidoDTO> pedidos = pedidoService.listarPedidos();

        ApiResponse<List<PedidoDTO>> response = ApiResponse.success(
                pedidos,
                String.format("Se encontraron %d pedido(s)", pedidos.size()));

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoDTO>> obtenerPedido(@PathVariable Integer id) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(id);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Pedido encontrado");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping("/{idPedido}")
    public ResponseEntity<ApiResponse<PedidoDTO>> actualizarPedido(
            @PathVariable Integer idPedido,
            @Valid @RequestBody PedidoDTO dto) {
        PedidoDTO pedido = pedidoService.actualizarPedido(idPedido, dto);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Pedido actualizado exitosamente");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping("/{idPedido}/estado/{estado}")
    public ResponseEntity<ApiResponse<PedidoDTO>> actualizarEstado(
            @PathVariable Integer idPedido,
            @PathVariable String estado) {
        PedidoDTO pedido = pedidoService.actualizarEstado(idPedido, estado);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                String.format("Estado del pedido actualizado a %s", estado));

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @DeleteMapping("/{idPedido}")
    public ResponseEntity<ApiResponse<Void>> cancelarPedido(@PathVariable Integer idPedido) {
        pedidoService.cancelarPedido(idPedido);

        ApiResponse<Void> response = ApiResponse.success(
                "Pedido cancelado exitosamente (estado cambiado a CRM)");

        return ResponseEntity.ok(response);
    }

    // ========== GESTIÓN DE PRODUCTOS DEL PEDIDO ==========

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping("/{idPedido}/productos")
    public ResponseEntity<ApiResponse<PedidoDTO>> agregarProducto(
            @PathVariable Integer idPedido,
            @Valid @RequestBody ProductoPedidoDTO productoDTO) {
        PedidoDTO pedido = pedidoService.agregarProducto(idPedido, productoDTO);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Producto agregado exitosamente al pedido");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @DeleteMapping("/{idPedido}/productos/{idProducto}")
    public ResponseEntity<ApiResponse<PedidoDTO>> eliminarProducto(
            @PathVariable Integer idPedido,
            @PathVariable Integer idProducto) {
        PedidoDTO pedido = pedidoService.eliminarProducto(idPedido, idProducto);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Producto eliminado exitosamente del pedido");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping("/{idPedido}/productos/{idProducto}")
    public ResponseEntity<ApiResponse<PedidoDTO>> modificarProducto(
            @PathVariable Integer idPedido,
            @PathVariable Integer idProducto,
            @Valid @RequestBody ProductoPedidoDTO productoDTO) {
        PedidoDTO pedido = pedidoService.modificarProducto(idPedido, idProducto, productoDTO);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Producto del pedido modificado exitosamente");

        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint publico para obtener un pedido activo mediante su hash unico
     * Este endpoint NO requiere autenticacion y permite a los clientes
     * acceder a un pedido especifico mediante una URL unica
     */
    @GetMapping("/pedido-disponible/{hash}")
    public ResponseEntity<ApiResponse<PedidoDTO>> obtenerPedidoPorHash(@PathVariable String hash) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorHash(hash);

        ApiResponse<PedidoDTO> response = ApiResponse.success(
                pedido,
                "Pedido encontrado");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint administrativo para generar/obtener el hash de un pedido
     * Solo para uso interno de administradores
     */
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping("/{idPedido}/generar-hash")
    public ResponseEntity<ApiResponse<String>> generarUrlHash(@PathVariable Integer idPedido) {
        String hash = pedidoService.generarUrlHash(idPedido);

        ApiResponse<String> response = ApiResponse.success(
                hash,
                "Hash generado exitosamente para el pedido");

        return ResponseEntity.ok(response);
    }
}