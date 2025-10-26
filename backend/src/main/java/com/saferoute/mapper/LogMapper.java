package com.saferoute.mapper;

import com.saferoute.dto.LogDTO;
import com.saferoute.model.Log;
import lombok.experimental.UtilityClass;

/**
 * Mapper para convertir entidades Log a DTOs.
 * Centraliza la lógica de transformación.
 */
@UtilityClass
public class LogMapper {

    /**
     * Convierte una entidad Log a LogDTO.
     *
     * @param log la entidad Log
     * @return el LogDTO correspondiente
     */
    public LogDTO toDTO(Log log) {
        LogDTO dto = new LogDTO();
        dto.setIdLog(log.getIdLog());
        dto.setIdUsuario(log.getUsuario().getIdUsuario());
        dto.setNombreUsuario(construirNombreCompleto(log));
        dto.setAccion(log.getAccion());
        dto.setFechaLog(log.getFechaLog());
        return dto;
    }

    /**
     * Construye el nombre completo del usuario.
     *
     * @param log la entidad Log con información del usuario
     * @return el nombre completo (nombres + apellidos)
     */
    private String construirNombreCompleto(Log log) {
        String nombres = log.getUsuario().getNombres();
        String apellidos = log.getUsuario().getApellidos();

        // Manejar null o strings vacíos
        String nombreLimpio = (nombres != null && !nombres.trim().isEmpty()) ? nombres.trim() : "";
        String apellidoLimpio = (apellidos != null && !apellidos.trim().isEmpty()) ? apellidos.trim() : "";

        // Si ambos están vacíos, retornar N/A
        if (nombreLimpio.isEmpty() && apellidoLimpio.isEmpty()) {
            return "N/A";
        }

        // Si solo uno está vacío, retornar el que tiene valor
        if (nombreLimpio.isEmpty()) {
            return apellidoLimpio;
        }

        if (apellidoLimpio.isEmpty()) {
            return nombreLimpio;
        }

        // Ambos tienen valor, concatenar con espacio
        return nombreLimpio + " " + apellidoLimpio;
    }
}
