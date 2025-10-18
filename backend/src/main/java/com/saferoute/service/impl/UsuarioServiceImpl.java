package com.saferoute.service.impl;

import com.saferoute.dto.UsuarioDTO;
import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.IUsuarioService;
import com.saferoute.service.interfaces.ILogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ILogService logService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, ILogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.logService = logService;
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el usuario sea administrador (ADM)
        boolean esAdministrador = usuario.getUsuarioRoles().stream()
                .anyMatch(ur -> "ADM".equals(ur.getRol().getTipoRol()));

        if (!esAdministrador) {
            throw new RuntimeException("Solo se pueden eliminar usuarios con rol de Administrador (ADM)");
        }

        // Soft delete - cambiar estado a INACTIVO
        usuario.setEstadoUsuario("INACTIVO");
        usuarioRepository.save(usuario);

        // Registrar eliminación (soft delete) de usuario en logs
        logService.registrarLog(id,
                "Usuario administrador eliminado (soft delete) - Cédula: " + usuario.getCedula() +
                        ", Nombres: " + usuario.getNombres() + " " + usuario.getApellidos());
    }

    @Override
    public UsuarioDTO obtenerPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirADTO(usuario);
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setTelefono(usuario.getTelefono());
        dto.setCedula(usuario.getCedula());
        dto.setDireccion(usuario.getDireccion());

        // Extraer roles
        List<String> roles = usuario.getUsuarioRoles().stream()
                .map(ur -> ur.getRol().getTipoRol())
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }
}
