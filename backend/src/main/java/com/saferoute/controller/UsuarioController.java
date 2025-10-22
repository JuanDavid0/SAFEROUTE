package com.saferoute.controller;

import com.saferoute.dto.UsuarioDTO;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.service.interfaces.IUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de usuarios (RF003)
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Eliminar usuario
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<ApiResponse<Void>> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);

        ApiResponse<Void> response = ApiResponse.success("Usuario eliminado exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> listarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos();

        ApiResponse<List<UsuarioDTO>> response = ApiResponse.success(
                usuarios,
                "Se encontraron " + usuarios.size() + " usuario(s)");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerUsuario(@PathVariable Integer id) {
        UsuarioDTO usuario = usuarioService.obtenerPorId(id);

        ApiResponse<UsuarioDTO> response = ApiResponse.success(
                usuario,
                "Usuario encontrado");

        return ResponseEntity.ok(response);
    }
}
