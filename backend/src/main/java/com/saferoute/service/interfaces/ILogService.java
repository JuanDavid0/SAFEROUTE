package com.saferoute.service.interfaces;

import com.saferoute.dto.LogDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface ILogService {

    void registrarLog(Integer idUsuario, String accion);

    void registrarCambioEstadoPedido(Integer idUsuario, Integer idPedido, String estadoAnterior, String estadoNuevo);

    List<LogDTO> obtenerTodosLosLogs();

    List<LogDTO> obtenerLogsPorUsuario(Integer idUsuario);

    List<LogDTO> obtenerLogsPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
