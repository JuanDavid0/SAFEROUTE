package com.saferoute.service.impl;

import com.saferoute.constants.LogConstants;
import com.saferoute.dto.LogDTO;
import com.saferoute.exception.LogBusinessException;
import com.saferoute.mapper.LogMapper;
import com.saferoute.model.Log;
import com.saferoute.model.Usuario;
import com.saferoute.repository.LogRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.ILogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de logs del sistema.
 * Registra y consulta todas las acciones realizadas por los usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements ILogService {

    private final LogRepository logRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void registrarLog(Integer idUsuario, String accion) {
        log.info(LogConstants.LOG_REGISTRANDO, idUsuario, accion);

        Usuario usuario = buscarUsuario(idUsuario);
        Log logGuardado = guardarLog(usuario, accion);

        log.info(LogConstants.LOG_REGISTRADO, logGuardado.getIdLog());
    }

    @Override
    @Transactional
    public void registrarCambioEstadoPedido(Integer idUsuario, Integer idPedido, String estadoAnterior,
            String estadoNuevo) {
        log.info(LogConstants.LOG_CAMBIO_ESTADO, idUsuario, idPedido, estadoAnterior, estadoNuevo);

        String accion = String.format(LogConstants.FORMATO_CAMBIO_ESTADO, idPedido, estadoAnterior, estadoNuevo);
        registrarLog(idUsuario, accion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerTodosLosLogs() {
        log.info(LogConstants.LOG_OBTENER_TODOS);

        List<LogDTO> logs = logRepository.findAll().stream()
                .map(LogMapper::toDTO)
                .collect(Collectors.toList());

        log.info(LogConstants.LOG_TOTAL_ENCONTRADOS, logs.size());
        return logs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerLogsPorUsuario(Integer idUsuario) {
        log.info(LogConstants.LOG_OBTENER_POR_USUARIO, idUsuario);

        List<LogDTO> logs = logRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .map(LogMapper::toDTO)
                .collect(Collectors.toList());

        log.info(LogConstants.LOG_TOTAL_ENCONTRADOS, logs.size());
        return logs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerLogsPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info(LogConstants.LOG_OBTENER_POR_FECHA, fechaInicio, fechaFin);

        List<LogDTO> logs = logRepository.findByFechaLogBetween(fechaInicio, fechaFin).stream()
                .map(LogMapper::toDTO)
                .collect(Collectors.toList());

        log.info(LogConstants.LOG_TOTAL_ENCONTRADOS, logs.size());
        return logs;
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Busca un usuario por su ID.
     *
     * @param idUsuario el ID del usuario
     * @return el usuario encontrado
     * @throws LogBusinessException si el usuario no existe
     */
    private Usuario buscarUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new LogBusinessException(
                        String.format(LogConstants.ERROR_USUARIO_NO_ENCONTRADO, idUsuario)));
    }

    /**
     * Crea y guarda un registro de log.
     *
     * @param usuario el usuario que realiza la acción
     * @param accion  la descripción de la acción
     * @return el log guardado
     */
    private Log guardarLog(Usuario usuario, String accion) {
        Log log = new Log();
        log.setUsuario(usuario);
        log.setAccion(accion);
        return logRepository.save(log);
    }
}
