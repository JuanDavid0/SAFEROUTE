package com.saferoute.service.impl;

import com.saferoute.constants.CancelacionConstants;
import com.saferoute.dto.CancelacionSolicitudesDTO;
import com.saferoute.dto.CancelacionSolicitudesDTO.SolicitudCanceladaDTO;
import com.saferoute.exception.CancelacionBusinessException;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.enums.EstadoPedidoEnum;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.helper.CancelacionSolicitudHelper;
import com.saferoute.service.interfaces.ICancelacionSolicitudService;
import com.saferoute.service.validator.CancelacionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para cancelación semi-automática de solicitudes vencidas.
 * Requiere activación manual por ADM/SAD.
 * Refactorizado siguiendo principios SOLID y buenas prácticas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CancelacionSolicitudServiceImpl implements ICancelacionSolicitudService {

    private final PedidoRepository pedidoRepository;
    private final SolicitudRepository solicitudRepository;
    private final CancelacionValidator validator;
    private final CancelacionSolicitudHelper helper;

    @Override
    public CancelacionSolicitudesDTO cancelarSolicitudesPendientes(Integer idPedido) {
        log.info(CancelacionConstants.LOG_INICIO_CANCELACION_INDIVIDUAL, idPedido);

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new CancelacionBusinessException(
                        String.format(CancelacionConstants.ERROR_PEDIDO_NO_ENCONTRADO, idPedido)));

        // Validar requisitos para cancelación
        validator.validarPedidoParaCancelacion(pedido);

        return procesarCancelacionesPedido(pedido);
    }

    @Override
    public CancelacionSolicitudesDTO cancelarTodasSolicitudesVencidas() {
        log.info(CancelacionConstants.LOG_INICIO_CANCELACION_MASIVA);

        List<Pedido> pedidosVencidos = pedidoRepository
                .findByEstadoPedidoAndFechaCierreBefore(EstadoPedidoEnum.ACT, LocalDate.now());

        if (pedidosVencidos.isEmpty()) {
            return helper.construirRespuestaSinPedidosVencidos();
        }

        return procesarCancelacionMasiva(pedidosVencidos);
    }

    /**
     * Procesa la cancelación masiva de múltiples pedidos
     */
    private CancelacionSolicitudesDTO procesarCancelacionMasiva(List<Pedido> pedidosVencidos) {
        int totalCanceladas = 0;
        int totalNoCanceladas = 0;
        List<SolicitudCanceladaDTO> todasSolicitudesCanceladas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (Pedido pedido : pedidosVencidos) {
            try {
                CancelacionSolicitudesDTO resultado = procesarCancelacionesPedido(pedido);
                totalCanceladas += resultado.getTotalSolicitudesCanceladas();
                totalNoCanceladas += resultado.getTotalSolicitudesNoCanceladas();
                todasSolicitudesCanceladas.addAll(resultado.getSolicitudesCanceladas());

                if (!resultado.getExitoso()) {
                    errores.add(String.format(CancelacionConstants.ERROR_PROCESANDO_PEDIDO,
                            pedido.getIdPedido(), resultado.getMensaje()));
                }
            } catch (Exception e) {
                log.error(CancelacionConstants.LOG_ERROR_PROCESANDO_PEDIDO, pedido.getIdPedido(), e.getMessage());
                errores.add(String.format(CancelacionConstants.ERROR_PROCESANDO_PEDIDO,
                        pedido.getIdPedido(), e.getMessage()));
            }
        }

        return helper.construirRespuestaMasiva(
                pedidosVencidos.size(),
                totalCanceladas,
                totalNoCanceladas,
                todasSolicitudesCanceladas,
                errores);
    }

    /**
     * Procesa la cancelación de solicitudes pendientes para un pedido específico
     */
    private CancelacionSolicitudesDTO procesarCancelacionesPedido(Pedido pedido) {
        List<Solicitud> solicitudesPendientes = solicitudRepository
                .findByPedido_IdPedidoAndEstadoSolicitud(pedido.getIdPedido(), EstadoSolicitudEnum.PDP);

        if (solicitudesPendientes.isEmpty()) {
            return helper.construirRespuestaSinSolicitudes(pedido.getIdPedido(), pedido.getFechaCierre());
        }

        List<SolicitudCanceladaDTO> solicitudesCanceladas = new ArrayList<>();
        int canceladas = 0;
        int noCanceladas = 0;

        for (Solicitud solicitud : solicitudesPendientes) {
            try {
                SolicitudCanceladaDTO solicitudCancelada = helper.procesarCancelacionSolicitud(solicitud, pedido);
                solicitudesCanceladas.add(solicitudCancelada);
                canceladas++;
            } catch (Exception e) {
                log.error(CancelacionConstants.LOG_ERROR_CANCELACION, solicitud.getIdSolicitud(), e.getMessage());
                noCanceladas++;
            }
        }

        return helper.construirRespuestaExitosa(
                pedido.getIdPedido(),
                pedido.getFechaCierre(),
                canceladas,
                noCanceladas,
                solicitudesPendientes.size(),
                solicitudesCanceladas);
    }
}
