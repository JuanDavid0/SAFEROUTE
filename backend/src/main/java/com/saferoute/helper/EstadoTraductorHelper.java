package com.saferoute.helper;

import com.saferoute.constants.WhatsAppConstants;
import org.springframework.stereotype.Component;

/**
 * Helper para traducción de estados y generación de mensajes en WhatsApp.
 */
@Component
public class EstadoTraductorHelper {

    /**
     * Traduce el código de estado a texto legible en español.
     *
     * @param estado Código del estado
     * @return Descripción del estado en español
     */
    public String traducirEstado(String estado) {
        return switch (estado) {
            case "ACT" -> WhatsAppConstants.ESTADO_ACTIVO;
            case "CRD" -> WhatsAppConstants.ESTADO_CERRADO;
            case "RTA" -> WhatsAppConstants.ESTADO_LISTO_ADUANAS;
            case "ADU" -> WhatsAppConstants.ESTADO_EN_ADUANAS;
            case "DST" -> WhatsAppConstants.ESTADO_EN_DISTRIBUCION;
            case "ENT" -> WhatsAppConstants.ESTADO_ENTREGADO;
            case "CRM" -> WhatsAppConstants.ESTADO_CANCELADO_MANUAL;
            case "CRA" -> WhatsAppConstants.ESTADO_CANCELADO_AUTOMATICO;
            default -> estado;
        };
    }

    /**
     * Obtiene mensaje adicional según el estado del pedido.
     *
     * @param estado Código del estado
     * @return Mensaje contextual para el estado
     */
    public String obtenerMensajeSegunEstado(String estado) {
        return switch (estado) {
            case "RTA" -> WhatsAppConstants.MENSAJE_LISTO_ADUANAS;
            case "ADU" -> WhatsAppConstants.MENSAJE_EN_ADUANAS;
            case "DST" -> WhatsAppConstants.MENSAJE_EN_DISTRIBUCION;
            case "ENT" -> WhatsAppConstants.MENSAJE_ENTREGADO;
            default -> WhatsAppConstants.MENSAJE_DEFAULT;
        };
    }
}
