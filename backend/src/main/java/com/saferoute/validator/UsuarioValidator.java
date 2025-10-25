package com.saferoute.validator;

import com.saferoute.constants.UsuarioConstants;
import com.saferoute.exception.UsuarioBusinessException;
import com.saferoute.model.Usuario;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Validador de reglas de negocio para usuarios.
 * Centraliza todas las validaciones relacionadas con usuarios.
 */
@Slf4j
@UtilityClass
public class UsuarioValidator {

    /**
     * Valida que el usuario tenga rol de administrador.
     *
     * @param usuario el usuario a validar
     * @throws UsuarioBusinessException si el usuario no es administrador
     */
    public void validarEsAdministrador(Usuario usuario) {
        log.debug(UsuarioConstants.LOG_VALIDANDO_ROL, usuario.getIdUsuario());

        boolean esAdministrador = usuario.getUsuarioRoles().stream()
                .anyMatch(ur -> UsuarioConstants.ROL_ADMINISTRADOR.equals(ur.getRol().getTipoRol()));

        if (!esAdministrador) {
            throw new UsuarioBusinessException(UsuarioConstants.ERROR_SOLO_ADMINISTRADORES);
        }
    }
}
