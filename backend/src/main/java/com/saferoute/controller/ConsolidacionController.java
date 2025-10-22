package com.saferoute.controller;

import com.saferoute.dto.ConsolidacionDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IConsolidacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consolidacion")
public class ConsolidacionController {

    private final IConsolidacionService consolidacionService;

    public ConsolidacionController(IConsolidacionService consolidacionService) {
        this.consolidacionService = consolidacionService;
    }

    /**
     * RF005: Consolidar solicitudes pagadas de un pedido
     * Agrupa todas las solicitudes en estado 'PGD' y cambia el pedido a 'RTA'
     */
    @PostMapping("/pedido/{idPedido}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<ApiResponse<ConsolidacionDTO>> consolidarPedido(
            @PathVariable Integer idPedido,
            @Valid @RequestBody(required = false) Map<String, Object> opciones) {
        ConsolidacionDTO resultado = consolidacionService.consolidarPedido(idPedido, opciones);

        ApiResponse<ConsolidacionDTO> response = ApiResponse.success(
                resultado,
                "Pedido consolidado exitosamente");

        return ResponseEntity.ok(response);
    }
}
