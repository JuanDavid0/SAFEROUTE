package com.saferoute.service.impl;

import com.saferoute.dto.LogDTO;
import com.saferoute.model.Log;
import com.saferoute.model.Usuario;
import com.saferoute.repository.LogRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.ILogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements ILogService {

    private final LogRepository logRepository;
    private final UsuarioRepository usuarioRepository;

    public LogServiceImpl(LogRepository logRepository, UsuarioRepository usuarioRepository) {
        this.logRepository = logRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void registrarLog(Integer idUsuario, String accion) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Log log = new Log();
        log.setUsuario(usuario);
        log.setAccion(accion);
        logRepository.save(log);
    }

    @Override
    @Transactional
    public void registrarCambioEstadoPedido(Integer idUsuario, Integer idPedido, String estadoAnterior,
            String estadoNuevo) {
        String accion = String.format("Cambio de estado del pedido #%d: %s -> %s",
                idPedido, estadoAnterior, estadoNuevo);
        registrarLog(idUsuario, accion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerTodosLosLogs() {
        return logRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerLogsPorUsuario(Integer idUsuario) {
        return logRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogDTO> obtenerLogsPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return logRepository.findByFechaLogBetween(fechaInicio, fechaFin).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private LogDTO mapToDTO(Log log) {
        LogDTO dto = new LogDTO();
        dto.setIdLog(log.getIdLog());
        dto.setIdUsuario(log.getUsuario().getIdUsuario());
        dto.setNombreUsuario(log.getUsuario().getNombres() + " " + log.getUsuario().getApellidos());
        dto.setAccion(log.getAccion());
        dto.setFechaLog(log.getFechaLog());
        return dto;
    }
}
