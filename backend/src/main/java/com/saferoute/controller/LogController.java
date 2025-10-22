package com.saferoute.controller;

import com.saferoute.dto.LogDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.ILogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para consulta de logs del sistema
 */
@RestController
@RequestMapping("/logs")
public class LogController {

    private final ILogService logService;

    public LogController(ILogService logService) {
        this.logService = logService;
    }

    /**
     * Obtener todos los logs del sistema (Solo SuperAdministrador)
     */
    @GetMapping
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<ApiResponse<List<LogDTO>>> obtenerTodosLosLogs() {
        List<LogDTO> logs = logService.obtenerTodosLosLogs();

        ApiResponse<List<LogDTO>> response = ApiResponse.success(
                logs,
                "Se encontraron " + logs.size() + " log(s) en el sistema");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener logs por usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<ApiResponse<List<LogDTO>>> obtenerLogsPorUsuario(@PathVariable Integer idUsuario) {
        List<LogDTO> logs = logService.obtenerLogsPorUsuario(idUsuario);

        ApiResponse<List<LogDTO>> response = ApiResponse.success(
                logs,
                "Se encontraron " + logs.size() + " log(s) para el usuario con ID " + idUsuario);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener logs por rango de fechas
     */
    @GetMapping("/fecha")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<ApiResponse<List<LogDTO>>> obtenerLogsPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<LogDTO> logs = logService.obtenerLogsPorFecha(fechaInicio, fechaFin);

        ApiResponse<List<LogDTO>> response = ApiResponse.success(
                logs,
                "Se encontraron " + logs.size() + " log(s) en el rango de fechas especificado");

        return ResponseEntity.ok(response);
    }
}
