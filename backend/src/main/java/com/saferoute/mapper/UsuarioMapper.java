package com.saferoute.mapper;

import com.saferoute.dto.UsuarioDTO;
import com.saferoute.model.Usuario;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entidades Usuario a DTOs.
 * Centraliza la lógica de transformación.
 */
@UtilityClass
public class UsuarioMapper {

    /**
     * Convierte una entidad Usuario a UsuarioDTO.
     *
     * @param usuario la entidad Usuario
     * @return el UsuarioDTO correspondiente
     */
    public UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setTelefono(usuario.getTelefono());
        dto.setCedula(usuario.getCedula());
        dto.setDireccion(usuario.getDireccion());
        dto.setRoles(extraerRoles(usuario));
        dto.setEstado(usuario.getEstadoUsuario()); // Mapear estado del usuario
        return dto;
    }

    /**
     * Extrae los roles del usuario.
     *
     * @param usuario el usuario con roles
     * @return lista de strings con los tipos de rol
     */
    private List<String> extraerRoles(Usuario usuario) {
        return usuario.getUsuarioRoles().stream()
                .map(ur -> ur.getRol().getTipoRol())
                .collect(Collectors.toList());
    }
}
