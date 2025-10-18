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
import com.saferoute.service.interfaces.ILogService;
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
    private final ILogService logService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            ILogService logService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCedula(), loginRequest.getContrasenia()));
        String token = jwtTokenProvider.generateToken(authentication);

        // Obtener el rol del usuario
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("CLI");

        // Registrar login exitoso en logs
        Usuario usuario = usuarioRepository.findByCedula(loginRequest.getCedula()).orElse(null);
        if (usuario != null) {
            logService.registrarLog(usuario.getIdUsuario(),
                    "Inicio de sesión exitoso - Rol: " + rol);
        }

        return new JwtResponse(token, rol);
    }

    @Override
    public Usuario registro(RegistroRequest registroRequest) {
        // Verificar si la cédula ya existe
        if (usuarioRepository.findByCedula(registroRequest.getCedula()).isPresent()) {
            throw new RuntimeException("La cédula ya está registrada");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombres(registroRequest.getNombres());
        usuario.setApellidos(registroRequest.getApellidos());
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

        // Registrar registro de nuevo usuario en logs
        logService.registrarLog(usuarioGuardado.getIdUsuario(),
                "Registro de nuevo usuario - Cédula: " + usuarioGuardado.getCedula() + ", Rol: CLI");

        return usuarioGuardado;
    }

    @Override
    public void cambiarContrasenia(CambiarContraseniaRequest request) {
        Usuario usuario = usuarioRepository.findByCedula(request.getCedula())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getContraseniaActual(), usuario.getContrasenia())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Cambiar a la nueva contraseña
        usuario.setContrasenia(passwordEncoder.encode(request.getContraseniaNueva()));
        usuarioRepository.save(usuario);

        // Registrar cambio de contraseña en logs
        logService.registrarLog(usuario.getIdUsuario(),
                "Cambio de contraseña exitoso - Cédula: " + usuario.getCedula());
    }

    @Override
    public Usuario crearAdministrador(RegistroRequest registroRequest, Integer sadUserId) {
        // Verificar que el usuario que hace la solicitud es SAD
        usuarioRepository.findById(sadUserId)
                .orElseThrow(() -> new RuntimeException("Usuario SAD no encontrado"));

        // Verificar si la cédula ya existe
        if (usuarioRepository.findByCedula(registroRequest.getCedula()).isPresent()) {
            throw new RuntimeException("La cédula ya está registrada");
        }

        // Crear nuevo administrador
        Usuario administrador = new Usuario();
        administrador.setNombres(registroRequest.getNombres());
        administrador.setApellidos(registroRequest.getApellidos());
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

        // Registrar creación de administrador en logs
        logService.registrarLog(sadUserId,
                "Creación de nuevo administrador - Cédula: " + administradorGuardado.getCedula() +
                        ", Nombres: " + administradorGuardado.getNombres() + " "
                        + administradorGuardado.getApellidos());

        return administradorGuardado;
    }
}
