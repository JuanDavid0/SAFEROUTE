package com.saferoute.controller;

import com.saferoute.dto.HistorialPedidoDTO;
import com.saferoute.service.interfaces.IHistorialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para consulta de histórico de pedidos (RF004)
 */
@RestController
@RequestMapping("/historico")
public class HistorialController {

    private final IHistorialService historialService;

    public HistorialController(IHistorialService historialService) {
        this.historialService = historialService;
    }

    /**
     * RF004: Consultar histórico completo de pedidos con filtros
     * Solo para visualización (SuperAdministrador)
     */
    @GetMapping("/pedidos")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<List<HistorialPedidoDTO>> consultarHistorico(
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idProducto,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String estado) {

        List<HistorialPedidoDTO> historial = historialService.consultarHistorico(
                idCliente, idProducto, fechaInicio, fechaFin, estado);
        return ResponseEntity.ok(historial);
    }

    /**
     * Obtener detalle de un pedido histórico
     */
    @GetMapping("/pedidos/{idPedido}")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<HistorialPedidoDTO> obtenerDetallePedido(@PathVariable Integer idPedido) {
        HistorialPedidoDTO detalle = historialService.obtenerDetallePedido(idPedido);
        return ResponseEntity.ok(detalle);
    }
}
