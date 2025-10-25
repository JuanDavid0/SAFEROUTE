package com.saferoute.helper;

import com.saferoute.constants.UsuarioConstants;
import com.saferoute.model.Usuario;
import lombok.experimental.UtilityClass;

/**
 * Helper para construcción de mensajes de log relacionados con usuarios.
 * Centraliza la lógica de formateo de mensajes.
 */
@UtilityClass
public class UsuarioLogHelper {

    /**
     * Construye el mensaje de log para eliminación de usuario.
     *
     * @param usuario el usuario eliminado
     * @return el mensaje formateado
     */
    public String construirMensajeEliminacion(Usuario usuario) {
        return String.format(
                UsuarioConstants.FORMATO_LOG_ELIMINACION,
                usuario.getCedula(),
                usuario.getNombres(),
                usuario.getApellidos());
    }
}
