package com.saferoute.service.validator;

import com.saferoute.constants.CancelacionConstants;
import com.saferoute.exception.CancelacionBusinessException;
import com.saferoute.model.Pedido;
import com.saferoute.model.enums.EstadoPedidoEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CancelacionValidator {

    /**
     * Valida que la fecha de cierre del pedido ya haya pasado
     */
    public void validarFechaCierreVencida(Pedido pedido) {
        if (pedido.getFechaCierre() == null || !pedido.getFechaCierre().isBefore(LocalDate.now())) {
            throw new CancelacionBusinessException(CancelacionConstants.ERROR_FECHA_NO_VENCIDA);
        }
    }

    /**
     * Valida que el pedido esté en estado ACTIVO
     */
    public void validarPedidoActivo(Pedido pedido) {
        if (pedido.getEstadoPedido() != EstadoPedidoEnum.ACT) {
            throw new CancelacionBusinessException(
                    String.format(CancelacionConstants.ERROR_PEDIDO_NO_ACTIVO, pedido.getEstadoPedido()));
        }
    }

    /**
     * Valida que el pedido cumpla con todos los requisitos para cancelación
     */
    public void validarPedidoParaCancelacion(Pedido pedido) {
        validarFechaCierreVencida(pedido);
        validarPedidoActivo(pedido);
    }
}
