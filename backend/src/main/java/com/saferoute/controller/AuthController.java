package com.saferoute.controller;

import com.saferoute.dto.CambiarContraseniaRequest;
import com.saferoute.dto.JwtResponse;
import com.saferoute.dto.LoginRequest;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.model.Usuario;
import com.saferoute.security.CustomUserDetails;
import com.saferoute.service.interfaces.IAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registro(@Valid @RequestBody RegistroRequest registroRequest) {
        Usuario usuario = authService.registro(registroRequest);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/cambiar-contrasenia")
    public ResponseEntity<Map<String, String>> cambiarContrasenia(
            @Valid @RequestBody CambiarContraseniaRequest request) {
        authService.cambiarContrasenia(request);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña cambiada exitosamente"));
    }

    @PostMapping("/crear-administrador")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<Usuario> crearAdministrador(@Valid @RequestBody RegistroRequest registroRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer sadUserId = userDetails.getId();
        Usuario administrador = authService.crearAdministrador(registroRequest, sadUserId);
        return ResponseEntity.ok(administrador);
    }
}