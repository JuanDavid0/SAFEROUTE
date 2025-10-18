package com.saferoute.service.interfaces;

import com.saferoute.dto.LogDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface ILogService {
    /**
     * Registra un cambio de estado en el sistema (RF002.4)
     * 
     * @param idUsuario ID del usuario que realizó la acción
     * @param accion    Descripción de la acción realizada
     */
    void registrarLog(Integer idUsuario, String accion);

    /**
     * Registra un cambio de estado de pedido
     * 
     * @param idUsuario      ID del administrador que realizó el cambio
     * @param idPedido       ID del pedido
     * @param estadoAnterior Estado anterior del pedido
     * @param estadoNuevo    Nuevo estado del pedido
     */
    void registrarCambioEstadoPedido(Integer idUsuario, Integer idPedido, String estadoAnterior, String estadoNuevo);

    /**
     * Consultar todos los logs del sistema
     */
    List<LogDTO> obtenerTodosLosLogs();

    /**
     * Consultar logs por usuario
     */
    List<LogDTO> obtenerLogsPorUsuario(Integer idUsuario);

    /**
     * Consultar logs por rango de fechas
     */
    List<LogDTO> obtenerLogsPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
