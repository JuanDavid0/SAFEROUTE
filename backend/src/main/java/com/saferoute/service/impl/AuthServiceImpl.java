package com.saferoute.service.impl;

import com.saferoute.dto.CambiarContraseniaRequest;
import com.saferoute.dto.JwtResponse;
import com.saferoute.dto.LoginRequest;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.model.Usuario;
import com.saferoute.model.Rol;
import com.saferoute.model.UsuarioRol;
import com.saferoute.model.UsuarioRolId;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.repository.RolRepository;
import com.saferoute.repository.UsuarioRolRepository;
import com.saferoute.security.JwtTokenProvider;
import com.saferoute.service.interfaces.IAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCorreo(), loginRequest.getContrasenia()));
        String token = jwtTokenProvider.generateToken(authentication);

        // Obtener el rol del usuario
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("CLI");

        return new JwtResponse(token, rol);
    }

    @Override
    public Usuario registro(RegistroRequest registroRequest) {
        // Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(registroRequest.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombres(registroRequest.getNombres());
        usuario.setApellidos(registroRequest.getApellidos());
        usuario.setCorreo(registroRequest.getCorreo());
        usuario.setTelefono(registroRequest.getTelefono());
        usuario.setCedula(registroRequest.getCedula());
        usuario.setDireccion(registroRequest.getDireccion());
        if (registroRequest.getContrasenia() != null && !registroRequest.getContrasenia().isEmpty()) {
            usuario.setContrasenia(passwordEncoder.encode(registroRequest.getContrasenia()));
        } else {
            String noPassword = "";
            usuario.setContrasenia(passwordEncoder.encode(noPassword));
        }

        // Guardar usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Asignar rol de CLIENTE por defecto
        Rol rolCliente = rolRepository.findByTipoRol("CLI")
                .orElseThrow(() -> new RuntimeException("Rol CLI no encontrado"));

        // Crear relación usuario-rol
        UsuarioRolId usuarioRolId = new UsuarioRolId(rolCliente.getIdRol(), usuarioGuardado.getIdUsuario());
        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setId(usuarioRolId);
        usuarioRol.setRol(rolCliente);
        usuarioRol.setUsuario(usuarioGuardado);

        usuarioRolRepository.save(usuarioRol);

        return usuarioGuardado;
    }


    @Override
    public void cambiarContrasenia(CambiarContraseniaRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getContraseniaActual(), usuario.getContrasenia())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Cambiar a la nueva contraseña
        usuario.setContrasenia(passwordEncoder.encode(request.getContraseniaNueva()));
        usuarioRepository.save(usuario);
    }

    @Override
    public Usuario crearAdministrador(RegistroRequest registroRequest, Integer sadUserId) {
        // Verificar que el usuario que hace la solicitud es SAD
        usuarioRepository.findById(sadUserId)
                .orElseThrow(() -> new RuntimeException("Usuario SAD no encontrado"));

        // Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(registroRequest.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // Crear nuevo administrador
        Usuario administrador = new Usuario();
        administrador.setNombres(registroRequest.getNombres());
        administrador.setApellidos(registroRequest.getApellidos());
        administrador.setCorreo(registroRequest.getCorreo());
        administrador.setTelefono(registroRequest.getTelefono());
        administrador.setCedula(registroRequest.getCedula());
        administrador.setDireccion(registroRequest.getDireccion());
        administrador.setContrasenia(passwordEncoder.encode(registroRequest.getContrasenia()));

        // Guardar administrador
        Usuario administradorGuardado = usuarioRepository.save(administrador);

        // Asignar rol de ADMINISTRADOR
        Rol rolAdmin = rolRepository.findByTipoRol("ADM")
                .orElseThrow(() -> new RuntimeException("Rol ADM no encontrado"));

        // Crear relación usuario-rol
        UsuarioRolId usuarioRolId = new UsuarioRolId(rolAdmin.getIdRol(), administradorGuardado.getIdUsuario());
        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setId(usuarioRolId);
        usuarioRol.setRol(rolAdmin);
        usuarioRol.setUsuario(administradorGuardado);

        usuarioRolRepository.save(usuarioRol);

        return administradorGuardado;
    }
}
