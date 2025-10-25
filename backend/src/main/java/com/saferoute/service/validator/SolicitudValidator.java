package com.saferoute.service.validator;

import com.saferoute.exception.SolicitudBusinessException;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Validador centralizado para reglas de negocio de Solicitudes
 * Aplica el principio SRP (Single Responsibility Principle)
 */
@Component
public class SolicitudValidator {

    private static final int DIAS_LIMITE_MODIFICACION = 5;
    private static final int MAX_MODIFICACIONES_PERMITIDAS = 3;
    private static final int MIN_PRODUCTOS_EN_SOLICITUD = 1;

    /**
     * Valida que el pedido esté en estado ACTIVO
     */
    public void validarPedidoActivo(Pedido pedido) {
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.ACT) {
            throw new SolicitudBusinessException(String.format(
                    "No se pueden crear solicitudes para este pedido. El pedido debe estar en estado ACTIVO. Estado actual: %s",
                    pedido.getEstadoPedido()));
        }
    }

    /**
     * Valida que no se haya pasado la fecha de cierre del pedido
     */
    public void validarFechaCierreNoVencida(Pedido pedido) {
        LocalDate fechaCierre = pedido.getFechaCierre();
        if (fechaCierre != null && LocalDate.now().isAfter(fechaCierre)) {
            throw new SolicitudBusinessException(String.format(
                    "No se pueden crear solicitudes. La fecha de cierre del pedido (%s) ya pasó",
                    fechaCierre));
        }
    }

    /**
     * Valida que la solicitud esté en estado PDP (Pendiente de Pago)
     */
    public void validarSolicitudEstadoPDP(Solicitud solicitud) {
        if (solicitud.getEstadoSolicitud() != EstadoSolicitudEnum.PDP) {
            throw new SolicitudBusinessException(String.format(
                    "Solo se pueden modificar solicitudes en estado Pendiente de Pago (PDP). Estado actual: %s",
                    solicitud.getEstadoSolicitud()));
        }
    }

    /**
     * Valida que estemos dentro de la ventana de modificación (más de 5 días antes
     * del cierre)
     */
    public void validarVentanaModificacion(Pedido pedido) {
        LocalDate fechaCierre = pedido.getFechaCierre();
        if (fechaCierre != null) {
            LocalDate fechaLimite = fechaCierre.minusDays(DIAS_LIMITE_MODIFICACION);
            if (LocalDate.now().isAfter(fechaLimite)) {
                throw new SolicitudBusinessException(String.format(
                        "No se pueden realizar modificaciones %d días antes del cierre del pedido",
                        DIAS_LIMITE_MODIFICACION));
            }
        }
    }

    /**
     * Valida que la solicitud tenga modificaciones restantes disponibles
     */
    public void validarModificacionesDisponibles(Solicitud solicitud) {
        if (solicitud.getModificacionesRestantes() <= 0) {
            throw new SolicitudBusinessException(String.format(
                    "Se alcanzó el número máximo de modificaciones para esta solicitud (%d modificaciones)",
                    MAX_MODIFICACIONES_PERMITIDAS));
        }
    }

    /**
     * Valida que la solicitud tenga más de un producto antes de eliminar
     */
    public void validarCantidadProductosParaEliminar(Solicitud solicitud) {
        if (solicitud.getProductos().size() <= MIN_PRODUCTOS_EN_SOLICITUD) {
            throw new SolicitudBusinessException(
                    "No se puede eliminar el único producto de la solicitud. Cancele la solicitud en su lugar.");
        }
    }

    /**
     * Valida todas las condiciones necesarias para modificar una solicitud
     */
    public void validarSolicitudModificable(Solicitud solicitud) {
        validarSolicitudEstadoPDP(solicitud);
        validarVentanaModificacion(solicitud.getPedido());
        validarModificacionesDisponibles(solicitud);
    }

    /**
     * Valida que el pedido esté disponible para crear solicitudes
     */
    public void validarPedidoDisponible(Pedido pedido) {
        validarPedidoActivo(pedido);
        validarFechaCierreNoVencida(pedido);
    }
}
