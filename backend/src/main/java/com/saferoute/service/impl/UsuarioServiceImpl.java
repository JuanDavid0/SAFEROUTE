package com.saferoute.service.impl;

import com.saferoute.constants.UsuarioConstants;
import com.saferoute.dto.UsuarioDTO;
import com.saferoute.exception.UsuarioBusinessException;
import com.saferoute.helper.UsuarioLogHelper;
import com.saferoute.mapper.UsuarioMapper;
import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.service.interfaces.IUsuarioService;
import com.saferoute.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de usuarios.
 * Maneja operaciones CRUD y validaciones de negocio para usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ILogService logService;

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        log.info(UsuarioConstants.LOG_ELIMINANDO_USUARIO, id);

        Usuario usuario = buscarUsuario(id);
        UsuarioValidator.validarEsAdministrador(usuario);
        realizarSoftDelete(usuario);
        registrarEliminacionEnLog(id, usuario);

        log.info(UsuarioConstants.LOG_USUARIO_ELIMINADO, id);
    }

    @Override
    public UsuarioDTO obtenerPorId(Integer id) {
        log.info(UsuarioConstants.LOG_OBTENER_POR_ID, id);

        Usuario usuario = buscarUsuario(id);
        return UsuarioMapper.toDTO(usuario);
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        log.info(UsuarioConstants.LOG_LISTAR_TODOS);

        List<UsuarioDTO> usuarios = usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toDTO)
                .collect(Collectors.toList());

        log.info(UsuarioConstants.LOG_TOTAL_ENCONTRADOS, usuarios.size());
        return usuarios;
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Busca un usuario por su ID.
     *
     * @param id el ID del usuario
     * @return el usuario encontrado
     * @throws UsuarioBusinessException si el usuario no existe
     */
    private Usuario buscarUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioBusinessException(
                        String.format(UsuarioConstants.ERROR_USUARIO_NO_ENCONTRADO, id)));
    }

    /**
     * Realiza soft delete del usuario cambiando su estado a INACTIVO.
     *
     * @param usuario el usuario a eliminar
     */
    private void realizarSoftDelete(Usuario usuario) {
        usuario.setEstadoUsuario(UsuarioConstants.ESTADO_INACTIVO);
        usuarioRepository.save(usuario);
    }

    /**
     * Registra la eliminación del usuario en el log del sistema.
     *
     * @param id      el ID del usuario eliminado
     * @param usuario el usuario eliminado
     */
    private void registrarEliminacionEnLog(Integer id, Usuario usuario) {
        String mensaje = UsuarioLogHelper.construirMensajeEliminacion(usuario);
        logService.registrarLog(id, mensaje);
    }
}
