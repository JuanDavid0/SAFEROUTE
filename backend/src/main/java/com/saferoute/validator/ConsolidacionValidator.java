package com.saferoute.validator;

import com.saferoute.constants.ConsolidacionConstants;
import com.saferoute.exception.ConsolidacionBusinessException;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.enums.EstadoPedidoEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador para operaciones de consolidación de pedidos.
 */
@Slf4j
@Component
public class ConsolidacionValidator {

    /**
     * Valida que el pedido esté en estado ACTIVO.
     *
     * @param pedido Pedido a validar
     * @throws ConsolidacionBusinessException si el pedido no está activo
     */
    public void validarPedidoActivo(Pedido pedido) {
        if (!EstadoPedidoEnum.ACT.equals(pedido.getEstadoPedido())) {
            log.warn("Intento de consolidar pedido no activo. ID: {}, Estado: {}",
                    pedido.getIdPedido(), pedido.getEstadoPedido());
            throw new ConsolidacionBusinessException(ConsolidacionConstants.ERROR_PEDIDO_NO_ACTIVO);
        }
    }

    /**
     * Valida que existan solicitudes pagadas para consolidar.
     *
     * @param solicitudesPagadas Lista de solicitudes en estado pagado
     * @param idPedido           ID del pedido (para logging)
     * @throws ConsolidacionBusinessException si no hay solicitudes pagadas
     */
    public void validarSolicitudesPagadas(List<Solicitud> solicitudesPagadas, Integer idPedido) {
        if (solicitudesPagadas.isEmpty()) {
            log.warn("Intento de consolidar pedido sin solicitudes pagadas. ID: {}", idPedido);
            throw new ConsolidacionBusinessException(ConsolidacionConstants.ERROR_SIN_SOLICITUDES_PAGADAS);
        }
    }
}
