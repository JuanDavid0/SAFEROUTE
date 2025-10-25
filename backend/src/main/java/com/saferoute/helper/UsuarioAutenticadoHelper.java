package com.saferoute.helper;

import com.saferoute.constants.ProductoConstants;
import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Helper para obtener información del usuario autenticado.
 */
@Component
@RequiredArgsConstructor
public class UsuarioAutenticadoHelper {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene el ID del usuario autenticado actual desde el contexto de seguridad.
     *
     * @return ID del usuario autenticado, o null si no hay usuario autenticado
     */
    public Integer obtenerUsuarioAutenticadoId() {
        return obtenerAuthentication()
                .flatMap(this::obtenerCedulaDeAuthentication)
                .flatMap(usuarioRepository::findByCedula)
                .map(Usuario::getIdUsuario)
                .orElse(null);
    }

    private Optional<Authentication> obtenerAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !esUsuarioAnonimo(auth)) {
            return Optional.of(auth);
        }
        return Optional.empty();
    }

    private boolean esUsuarioAnonimo(Authentication auth) {
        return ProductoConstants.PRINCIPAL_ANONIMO.equals(auth.getPrincipal());
    }

    private Optional<String> obtenerCedulaDeAuthentication(Authentication auth) {
        return Optional.ofNullable(auth.getName());
    }
}
