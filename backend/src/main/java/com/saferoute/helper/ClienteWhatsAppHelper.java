package com.saferoute.helper;

import com.saferoute.constants.WhatsAppConstants;
import com.saferoute.model.Rol;
import com.saferoute.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper para filtrado y procesamiento de clientes en notificaciones de
 * WhatsApp.
 */
@Component
public class ClienteWhatsAppHelper {

    /**
     * Filtra usuarios activos con rol de cliente.
     *
     * @param usuarios Lista de todos los usuarios
     * @return Lista de usuarios que son clientes activos
     */
    public List<Usuario> filtrarClientesActivos(List<Usuario> usuarios) {
        return usuarios.stream()
                .filter(this::esClienteActivo)
                .toList();
    }

    /**
     * Valida si un usuario es cliente activo.
     */
    private boolean esClienteActivo(Usuario usuario) {
        return esCliente(usuario) && estaActivo(usuario);
    }

    /**
     * Verifica si un usuario tiene rol de cliente.
     */
    private boolean esCliente(Usuario usuario) {
        Set<Rol> roles = usuario.getRoles();
        return roles.stream()
                .anyMatch(r -> WhatsAppConstants.ROL_CLIENTE.equals(r.getTipoRol()));
    }

    /**
     * Verifica si un usuario está activo.
     */
    private boolean estaActivo(Usuario usuario) {
        return WhatsAppConstants.ESTADO_USUARIO_ACTIVO.equals(usuario.getEstadoUsuario());
    }

    /**
     * Extrae teléfonos únicos válidos de una lista de usuarios.
     *
     * @param usuarios Lista de usuarios
     * @return Lista de teléfonos únicos y válidos
     */
    public List<String> extraerTelefonosUnicos(List<Usuario> usuarios) {
        List<String> telefonos = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            String telefono = usuario.getTelefono();
            if (esTelefonoValido(telefono) && !telefonos.contains(telefono)) {
                telefonos.add(telefono);
            }
        }

        return telefonos;
    }

    /**
     * Valida si un teléfono es válido (no nulo y no vacío).
     */
    public boolean esTelefonoValido(String telefono) {
        return telefono != null && !telefono.isEmpty();
    }

    /**
     * Formatea el número de teléfono para WhatsApp.
     * Debe ser en formato: código de país + número (sin +)
     * Ejemplo: 573001234567
     *
     * @param telefono Número de teléfono a formatear
     * @return Número formateado
     */
    public String formatearNumeroTelefono(String telefono) {
        // Remover caracteres no numéricos
        String limpio = telefono.replaceAll("[^0-9]", "");

        // Si no empieza con código de país (57 para Colombia), agregarlo
        if (!limpio.startsWith(WhatsAppConstants.CODIGO_PAIS_COLOMBIA)
                && limpio.length() == WhatsAppConstants.LONGITUD_TELEFONO_COLOMBIA) {
            limpio = WhatsAppConstants.CODIGO_PAIS_COLOMBIA + limpio;
        }

        return limpio;
    }
}
