package com.saferoute.service.interfaces;

import com.saferoute.dto.UsuarioDTO;

import java.util.List;

public interface IUsuarioService {
    /**
     * Elimina un usuario del sistema (RF003.2)
     */
    void eliminarUsuario(Integer id);

    /**
     * Obtiene un usuario por ID
     */
    UsuarioDTO obtenerPorId(Integer id);

    /**
     * Lista todos los usuarios del sistema
     */
    List<UsuarioDTO> listarTodos();
}
