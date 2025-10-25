package com.saferoute.service.interfaces;

import com.saferoute.dto.UsuarioDTO;

import java.util.List;

public interface IUsuarioService {

    void eliminarUsuario(Integer id);

    UsuarioDTO obtenerPorId(Integer id);

    List<UsuarioDTO> listarTodos();
}
