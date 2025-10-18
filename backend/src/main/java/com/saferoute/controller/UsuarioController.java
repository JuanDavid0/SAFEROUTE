package com.saferoute.controller;

import com.saferoute.dto.UsuarioDTO;
import com.saferoute.service.interfaces.IUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado exitosamente"));
    }

    /**
     * Listar todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SAD', 'ADM')")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Integer id) {
        UsuarioDTO usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }
}
