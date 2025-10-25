package com.saferoute.service.impl;

import com.saferoute.constants.ConsolidacionConstants;
import com.saferoute.dto.ConsolidacionDTO;
import com.saferoute.dto.ConsolidacionProductoDTO;
import com.saferoute.exception.ConsolidacionBusinessException;
import com.saferoute.helper.ConsolidacionCalculosHelper;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.interfaces.IConsolidacionService;
import com.saferoute.service.interfaces.ILogService;
import com.saferoute.validator.ConsolidacionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de consolidación de pedidos.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConsolidacionServiceImpl implements IConsolidacionService {

        private final PedidoRepository pedidoRepository;
        private final SolicitudRepository solicitudRepository;
        private final ILogService logService;
        private final ConsolidacionValidator consolidacionValidator;
        private final ConsolidacionCalculosHelper calculosHelper;

        @Override
        public ConsolidacionDTO consolidarPedido(Integer idPedido, Map<String, Object> opciones) {
                log.info(ConsolidacionConstants.LOG_CONSOLIDANDO_PEDIDO, idPedido);

                Pedido pedido = buscarYValidarPedido(idPedido);
                List<Solicitud> solicitudesPagadas = obtenerYValidarSolicitudesPagadas(idPedido);

                cambiarEstadoPedido(pedido);

                ConsolidacionDTO resultado = construirResultado(idPedido, solicitudesPagadas);

                log.info(ConsolidacionConstants.LOG_PEDIDO_CONSOLIDADO,
                                idPedido, resultado.getTotalSolicitudes(), resultado.getMontoTotal());

                return resultado;
        }

        /**
         * Busca el pedido y valida que esté activo.
         */
        private Pedido buscarYValidarPedido(Integer idPedido) {
                Pedido pedido = pedidoRepository.findById(idPedido)
                                .orElseThrow(() -> new ConsolidacionBusinessException(
                                                String.format(ConsolidacionConstants.ERROR_PEDIDO_NO_ENCONTRADO_CON_ID,
                                                                idPedido)));

                consolidacionValidator.validarPedidoActivo(pedido);
                return pedido;
        }

        /**
         * Obtiene las solicitudes pagadas y valida que existan.
         */
        private List<Solicitud> obtenerYValidarSolicitudesPagadas(Integer idPedido) {
                List<Solicitud> solicitudesPagadas = solicitudRepository
                                .findByPedido_IdPedidoAndEstadoSolicitud(idPedido, EstadoSolicitudEnum.PGD);

                consolidacionValidator.validarSolicitudesPagadas(solicitudesPagadas, idPedido);
                return solicitudesPagadas;
        }

        /**
         * Cambia el estado del pedido a RTA (En Ruta) y registra en log.
         */
        private void cambiarEstadoPedido(Pedido pedido) {
                String estadoAnterior = pedido.getEstadoPedido().name();
                pedido.setEstadoPedido(EstadoPedidoEnum.RTA);
                pedidoRepository.save(pedido);

                log.info(ConsolidacionConstants.LOG_CAMBIO_ESTADO_PEDIDO,
                                pedido.getIdPedido(), estadoAnterior, EstadoPedidoEnum.RTA.name());

                logService.registrarCambioEstadoPedido(
                                pedido.getAdmin().getIdUsuario(),
                                pedido.getIdPedido(),
                                estadoAnterior,
                                EstadoPedidoEnum.RTA.name());
        }

        /**
         * Construye el DTO de resultado con totales y productos consolidados.
         */
        private ConsolidacionDTO construirResultado(Integer idPedido, List<Solicitud> solicitudesPagadas) {
                BigDecimal montoTotal = calculosHelper.calcularMontoTotal(solicitudesPagadas);
                List<ConsolidacionProductoDTO> productos = calculosHelper.consolidarProductos(solicitudesPagadas);

                ConsolidacionDTO resultado = new ConsolidacionDTO();
                resultado.setIdPedido(idPedido);
                resultado.setNuevoEstado(EstadoPedidoEnum.RTA.name());
                resultado.setTotalSolicitudes(solicitudesPagadas.size());
                resultado.setMontoTotal(montoTotal);
                resultado.setProductos(productos);
                resultado.setMensaje(ConsolidacionConstants.MENSAJE_PEDIDO_CONSOLIDADO);

                return resultado;
        }
}