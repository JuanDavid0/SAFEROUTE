package com.saferoute.controller;

import com.saferoute.dto.CancelacionSolicitudesDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.ICancelacionSolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cancelar-solicitudes")
@RequiredArgsConstructor
@Slf4j
public class CancelacionController {

    private final ICancelacionSolicitudService cancelacionService;


    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping("/cancelar-vencidas/{idPedido}")
    public ResponseEntity<ApiResponse<CancelacionSolicitudesDTO>> cancelarSolicitudesPedido(
            @PathVariable Integer idPedido) {

        log.info("ADM/SAD solicitó cancelación de solicitudes vencidas para pedido: {}", idPedido);

        CancelacionSolicitudesDTO resultado = cancelacionService.cancelarSolicitudesPendientes(idPedido);

        ApiResponse<CancelacionSolicitudesDTO> response = ApiResponse.success(
                resultado,
                resultado.getMensaje());

        HttpStatus status = resultado.getExitoso() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    @PostMapping("/cancelar-todas-vencidas")
    public ResponseEntity<ApiResponse<CancelacionSolicitudesDTO>> cancelarTodasSolicitudesVencidas() {

        log.info("ADM/SAD solicitó cancelación masiva de todas las solicitudes vencidas");

        CancelacionSolicitudesDTO resultado = cancelacionService.cancelarTodasSolicitudesVencidas();

        ApiResponse<CancelacionSolicitudesDTO> response = ApiResponse.success(
                resultado,
                resultado.getMensaje());

        return ResponseEntity.ok(response);
    }
}
