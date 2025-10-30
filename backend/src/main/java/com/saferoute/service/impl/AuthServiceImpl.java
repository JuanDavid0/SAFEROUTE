package com.saferoute.service.impl;

import com.saferoute.constants.AuthConstants;
import com.saferoute.dto.CambiarContraseniaRequest;
import com.saferoute.dto.JwtResponse;
import com.saferoute.dto.LoginRequest;
import com.saferoute.dto.RegistroRequest;
import com.saferoute.exception.AuthBusinessException;
import com.saferoute.helper.UsuarioCreacionHelper;
import com.saferoute.model.Usuario;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.security.JwtTokenProvider;
import com.saferoute.service.interfaces.IAuthService;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.validator.AuthValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de autenticación y gestión de usuarios.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ILogService logService;
    private final AuthValidator authValidator;
    private final UsuarioCreacionHelper usuarioCreacionHelper;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = autenticarUsuario(loginRequest);
        String token = jwtTokenProvider.generateToken(authentication);
        String rol = extraerRolDeAuthentication(authentication);

        // Obtener el idUsuario del usuario autenticado
        Usuario usuario = usuarioRepository.findByCedula(loginRequest.getCedula())
                .orElseThrow(() -> new AuthBusinessException(AuthConstants.ERROR_USUARIO_NO_ENCONTRADO));
        Integer idUsuario = usuario.getIdUsuario();

        registrarLoginExitoso(loginRequest.getCedula(), rol);

        return new JwtResponse(token, rol, idUsuario);
    }

    private Authentication autenticarUsuario(LoginRequest loginRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCedula(),
                        loginRequest.getContrasenia()));
    }

    private String extraerRolDeAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace(AuthConstants.PREFIJO_ROL_SPRING, ""))
                .orElse(AuthConstants.ROL_CLIENTE);
    }

    private void registrarLoginExitoso(String cedula, String rol) {
        Usuario usuario = usuarioRepository.findByCedula(cedula).orElse(null);
        if (usuario != null) {
            log.info(AuthConstants.LOG_LOGIN_EXITOSO, rol);
            logService.registrarLog(usuario.getIdUsuario(),
                    String.format("Inicio de sesión exitoso - Rol: %s", rol));
        }
    }

    @Override
    public Usuario registro(RegistroRequest registroRequest) {
        authValidator.validarCedulaNoExiste(registroRequest.getCedula());

        Usuario usuario = usuarioCreacionHelper.crearUsuario(registroRequest);
        usuarioCreacionHelper.asignarRol(usuario, AuthConstants.ROL_CLIENTE);

        registrarNuevoUsuario(usuario);

        return usuario;
    }

    private void registrarNuevoUsuario(Usuario usuario) {
        log.info(AuthConstants.LOG_REGISTRO_USUARIO, usuario.getCedula(), AuthConstants.ROL_CLIENTE);
        logService.registrarLog(usuario.getIdUsuario(),
                String.format("Registro de nuevo usuario - Cédula: %s, Rol: %s",
                        usuario.getCedula(), AuthConstants.ROL_CLIENTE));
    }

    @Override
    public void cambiarContrasenia(CambiarContraseniaRequest request) {
        Usuario usuario = buscarUsuarioPorCedula(request.getCedula());

        authValidator.validarContraseniaActual(
                request.getContraseniaActual(),
                usuario.getContrasenia());

        usuario.setContrasenia(passwordEncoder.encode(request.getContraseniaNueva()));
        usuarioRepository.save(usuario);

        registrarCambioContrasenia(usuario);
    }

    private void registrarCambioContrasenia(Usuario usuario) {
        log.info(AuthConstants.LOG_CAMBIO_CONTRASENIA, usuario.getCedula());
        logService.registrarLog(usuario.getIdUsuario(),
                String.format("Cambio de contraseña exitoso - Cédula: %s", usuario.getCedula()));
    }

    @Override
    public Usuario crearAdministrador(RegistroRequest registroRequest, Integer sadUserId) {
        validarUsuarioSAD(sadUserId);
        authValidator.validarCedulaNoExiste(registroRequest.getCedula());

        Usuario administrador = usuarioCreacionHelper.crearUsuario(registroRequest);
        usuarioCreacionHelper.asignarRol(administrador, AuthConstants.ROL_ADMINISTRADOR);

        registrarCreacionAdministrador(administrador, sadUserId);

        return administrador;
    }

    private void validarUsuarioSAD(Integer sadUserId) {
        usuarioRepository.findById(sadUserId)
                .orElseThrow(() -> new AuthBusinessException(AuthConstants.ERROR_USUARIO_SAD_NO_ENCONTRADO));
    }

    private void registrarCreacionAdministrador(Usuario administrador, Integer sadUserId) {
        log.info(AuthConstants.LOG_CREACION_ADMINISTRADOR,
                administrador.getCedula(),
                administrador.getNombres(),
                administrador.getApellidos());

        logService.registrarLog(sadUserId,
                String.format("Creación de nuevo administrador - Cédula: %s, Nombres: %s %s",
                        administrador.getCedula(),
                        administrador.getNombres(),
                        administrador.getApellidos()));
    }

    /**
     * Busca un usuario por cédula o lanza excepción si no existe.
     */
    private Usuario buscarUsuarioPorCedula(String cedula) {
        return usuarioRepository.findByCedula(cedula)
                .orElseThrow(() -> new AuthBusinessException(AuthConstants.ERROR_USUARIO_NO_ENCONTRADO));
    }
}
