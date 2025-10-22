package com.saferoute.controller;

import com.saferoute.dto.CambiarContraseniaRequest;
import com.saferoute.dto.JwtResponse;
import com.saferoute.dto.LoginRequest;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.dto.response.ApiResponse;
import com.saferoute.model.Usuario;
import com.saferoute.security.CustomUserDetails;
import com.saferoute.service.interfaces.IAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);

        ApiResponse<JwtResponse> response = ApiResponse.success(
                jwtResponse,
                "Inicio de sesión exitoso");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<Usuario>> registro(@Valid @RequestBody RegistroRequest registroRequest) {
        Usuario usuario = authService.registro(registroRequest);

        ApiResponse<Usuario> response = ApiResponse.success(
                usuario,
                "Usuario registrado exitosamente");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-contrasenia")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasenia(
            @Valid @RequestBody CambiarContraseniaRequest request) {
        authService.cambiarContrasenia(request);

        ApiResponse<Void> response = ApiResponse.success("Contraseña cambiada exitosamente");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/crear-administrador")
    @PreAuthorize("hasRole('SAD')")
    public ResponseEntity<ApiResponse<Usuario>> crearAdministrador(
            @Valid @RequestBody RegistroRequest registroRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer sadUserId = userDetails.getId();

        Usuario administrador = authService.crearAdministrador(registroRequest, sadUserId);

        ApiResponse<Usuario> response = ApiResponse.success(
                administrador,
                "Administrador creado exitosamente");

        return ResponseEntity.ok(response);
    }
}