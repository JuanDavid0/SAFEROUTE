package com.saferoute.controller;

import com.saferoute.dto.*;
import com.saferoute.service.interfaces.ISolicitudService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final ISolicitudService solicitudService;

    public SolicitudController(ISolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PreAuthorize("hasRole('CLI')")
    @PostMapping("/{idCliente}")
    public SolicitudDTO crearSolicitud(@PathVariable Integer idCliente, @Valid @RequestBody SolicitudDTO dto) {
        return solicitudService.crearSolicitud(dto, idCliente);
    }

    @PostMapping("/public/nueva")
    public SolicitudDTO crearSolicitudClienteNuevo(@Valid @RequestBody SolicitudClienteDTO dto) {
        return solicitudService.crearSolicitudClienteNuevo(dto);
    }

    @PutMapping("/{idSolicitud}/modificar")
    public SolicitudDTO modificarSolicitud(@PathVariable Integer idSolicitud,
            @RequestBody SolicitudModificacionDTO dto) {
        return solicitudService.modificarSolicitud(idSolicitud, dto);
    }

    @DeleteMapping("/{idSolicitud}")
    public void cancelarSolicitud(@PathVariable Integer idSolicitud) {
        solicitudService.cancelarSolicitud(idSolicitud);
    }

    @GetMapping("/{idCliente}")
    public List<SolicitudDTO> listarSolicitudesCliente(@PathVariable Integer idCliente) {
        return solicitudService.listarSolicitudesCliente(idCliente);
    }

    /**
     * Cambiar estado de solicitud manualmente (para confirmación de pago manual)
     */
    @PutMapping("/{idSolicitud}/estado")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public java.util.Map<String, String> cambiarEstadoSolicitud(
            @PathVariable Integer idSolicitud,
            @RequestBody java.util.Map<String, String> body) {

        String nuevoEstado = body.get("nuevoEstado"); // "PGD" o "CAN"
        solicitudService.cambiarEstado(idSolicitud, nuevoEstado);
        return java.util.Map.of("mensaje", "Estado actualizado a " + nuevoEstado);
    }

    /**
     * Listar solicitudes de un pedido por estado (filtro)
     */
    @GetMapping("/pedido/{idPedido}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public List<SolicitudDTO> listarSolicitudesPorPedidoYEstado(
            @PathVariable Integer idPedido,
            @RequestParam(required = false) String estado) {

        if (estado != null) {
            return solicitudService.listarPorPedidoYEstado(idPedido, estado);
        } else {
            return solicitudService.listarPorPedido(idPedido);
        }
    }
}
