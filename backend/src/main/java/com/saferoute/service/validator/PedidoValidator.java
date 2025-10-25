package com.saferoute.service.validator;

import com.saferoute.constants.PedidoConstants;
import com.saferoute.exception.PedidoBusinessException;
import com.saferoute.model.Pedido;
import com.saferoute.model.enums.EstadoPedidoEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validador centralizado para reglas de negocio de Pedidos
 * Aplica el principio SRP (Single Responsibility Principle)
 */
@Component
public class PedidoValidator {

        /**
         * Valida que el pedido esté en estado CREADO para permitir modificaciones
         */
        public void validarPedidoEstadoCreado(Pedido pedido, String operacion) {
                if (pedido.getEstadoPedido() != PedidoConstants.ESTADO_CREADO) {
                        throw new PedidoBusinessException(String.format(
                                        PedidoConstants.ERROR_SOLO_ESTADO_CRT,
                                        operacion,
                                        pedido.getEstadoPedido()));
                }
        }

        /**
         * Valida que el pedido esté en estado ACTIVO
         */
        public void validarPedidoEstadoActivo(Pedido pedido) {
                if (pedido.getEstadoPedido() != PedidoConstants.ESTADO_ACTIVO) {
                        throw new PedidoBusinessException(PedidoConstants.ERROR_HASH_SOLO_ACTIVO);
                }
        }

        /**
         * Valida que la transición entre estados sea permitida
         */
        public void validarTransicionEstado(EstadoPedidoEnum actual, EstadoPedidoEnum nuevo) {
                Map<EstadoPedidoEnum, List<EstadoPedidoEnum>> transicionesPermitidas = construirMatrizTransiciones();

                List<EstadoPedidoEnum> estadosPermitidos = transicionesPermitidas.get(actual);
                if (estadosPermitidos == null || !estadosPermitidos.contains(nuevo)) {
                        throw new PedidoBusinessException(String.format(
                                        PedidoConstants.ERROR_TRANSICION_INVALIDA,
                                        actual,
                                        nuevo,
                                        actual,
                                        estadosPermitidos != null && !estadosPermitidos.isEmpty()
                                                        ? estadosPermitidos
                                                        : PedidoConstants.MSG_NINGUNO_ESTADO_FINAL));
                }
        }

        /**
         * Construye la matriz de transiciones válidas entre estados
         * Flujo: CRT → ACT → (CRM|CRA) → [PRD → RCP] → RTA → ADU → ENT
         */
        private Map<EstadoPedidoEnum, List<EstadoPedidoEnum>> construirMatrizTransiciones() {
                Map<EstadoPedidoEnum, List<EstadoPedidoEnum>> transiciones = new HashMap<>();

                // CRT (Creado) puede ir a: ACT (Activo) o CRM (Cerrado Manual)
                transiciones.put(EstadoPedidoEnum.CRT,
                                Arrays.asList(EstadoPedidoEnum.ACT, EstadoPedidoEnum.CRM));

                // ACT (Activo) puede ir a: CRM (Cerrado Manual), CRA (Cerrado Automático)
                transiciones.put(EstadoPedidoEnum.ACT,
                                Arrays.asList(EstadoPedidoEnum.CRM, EstadoPedidoEnum.CRA));

                // CRM (Cerrado Manual) puede ir a: PRD (Perdido), RTA (En Ruta)
                transiciones.put(EstadoPedidoEnum.CRM,
                                Arrays.asList(EstadoPedidoEnum.PRD, EstadoPedidoEnum.RTA));

                // CRA (Cerrado Automático) puede ir a: PRD (Perdido), RTA (En Ruta)
                transiciones.put(EstadoPedidoEnum.CRA,
                                Arrays.asList(EstadoPedidoEnum.PRD, EstadoPedidoEnum.RTA));

                // PRD (Perdido) puede ir a: RCP (Recuperado)
                transiciones.put(EstadoPedidoEnum.PRD,
                                Arrays.asList(EstadoPedidoEnum.RCP));

                // RCP (Recuperado) puede ir a: RTA (En Ruta)
                transiciones.put(EstadoPedidoEnum.RCP,
                                Arrays.asList(EstadoPedidoEnum.RTA));

                // RTA (En Ruta) puede ir a: ADU (Aduana), PRD (Perdido)
                transiciones.put(EstadoPedidoEnum.RTA,
                                Arrays.asList(EstadoPedidoEnum.ADU, EstadoPedidoEnum.PRD));

                // ADU (Aduana) puede ir a: ENT (Entregado), PRD (Perdido)
                transiciones.put(EstadoPedidoEnum.ADU,
                                Arrays.asList(EstadoPedidoEnum.ENT, EstadoPedidoEnum.PRD));

                // ENT (Entregado) es estado final - no puede cambiar
                transiciones.put(EstadoPedidoEnum.ENT, Arrays.asList());

                return transiciones;
        }

        /**
         * Valida que el pedido pueda ser modificado según su estado
         */
        public void validarPedidoModificable(Pedido pedido) {
                EstadoPedidoEnum estado = pedido.getEstadoPedido();

                // Solo CRT y ACT permiten modificaciones
                if (estado != EstadoPedidoEnum.CRT && estado != EstadoPedidoEnum.ACT) {
                        throw new PedidoBusinessException(String.format(
                                        PedidoConstants.ERROR_PEDIDO_NO_MODIFICABLE,
                                        estado));
                }
        }
}
