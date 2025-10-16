package com.saferoute.service.impl;

import com.saferoute.dto.UsuarioDTO;
import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.IUsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        // TODO: verificar tipo de elimincacion de usuarios soft? 
        usuarioRepository.delete(usuario);
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
        dto.setCorreo(usuario.getCorreo());
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
