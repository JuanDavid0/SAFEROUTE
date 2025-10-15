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
    public SolicitudDTO modificarSolicitud(@PathVariable Integer idSolicitud, @RequestBody SolicitudModificacionDTO dto) {
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

    
}
