package com.saferoute.controller;

import com.saferoute.dto.PedidoDTO;
import com.saferoute.service.interfaces.IPedidoService;
import jakarta.validation.Valid;
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
    public PedidoDTO crearPedido(@PathVariable Integer idAdmin, @Valid @RequestBody PedidoDTO dto) {
        return pedidoService.crearPedido(dto, idAdmin);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping
    public List<PedidoDTO> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @GetMapping("/{id}")
    public PedidoDTO obtenerPedido(@PathVariable Integer id) {
        return pedidoService.obtenerPedidoPorId(id);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PutMapping("/{idPedido}/estado/{estado}")
    public PedidoDTO actualizarEstado(@PathVariable Integer idPedido, @PathVariable String estado) {
        return pedidoService.actualizarEstado(idPedido, estado);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @DeleteMapping("/{idPedido}")
    public void cancelarPedido(@PathVariable Integer idPedido) {
        pedidoService.cancelarPedido(idPedido);
    }
}