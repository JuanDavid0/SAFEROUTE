package com.saferoute.security;

import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String cedula) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCedulaWithRoles(cedula)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con cédula: " + cedula));

        // Validar que el usuario esté activo
        if ("INACTIVO".equals(usuario.getEstadoUsuario())) {
            throw new UsernameNotFoundException("El usuario está inactivo");
        }

        return new CustomUserDetails(usuario);
    }
}
