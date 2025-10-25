package com.saferoute.validator;

import com.saferoute.constants.AuthConstants;
import com.saferoute.exception.AuthBusinessException;
import com.saferoute.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Validador para operaciones de autenticación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida que la cédula no esté registrada previamente.
     *
     * @param cedula Cédula a validar
     * @throws AuthBusinessException si la cédula ya está registrada
     */
    public void validarCedulaNoExiste(String cedula) {
        if (usuarioRepository.findByCedula(cedula).isPresent()) {
            log.warn("Intento de registro con cédula ya existente: {}", cedula);
            throw new AuthBusinessException(AuthConstants.ERROR_CEDULA_YA_REGISTRADA);
        }
    }

    /**
     * Valida que la contraseña actual coincida con la almacenada.
     *
     * @param contraseniaActual     Contraseña ingresada por el usuario
     * @param contraseniaAlmacenada Contraseña hasheada en BD
     * @throws AuthBusinessException si la contraseña no coincide
     */
    public void validarContraseniaActual(String contraseniaActual, String contraseniaAlmacenada) {
        if (!passwordEncoder.matches(contraseniaActual, contraseniaAlmacenada)) {
            log.warn("Intento de cambio de contraseña con contraseña actual incorrecta");
            throw new AuthBusinessException(AuthConstants.ERROR_CONTRASENIA_INCORRECTA);
        }
    }
}
