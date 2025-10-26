package com.saferoute.service.helper;

import com.saferoute.constants.CancelacionConstants;
import com.saferoute.dto.CancelacionSolicitudesDTO;
import com.saferoute.dto.CancelacionSolicitudesDTO.SolicitudCanceladaDTO;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.service.interfaces.IWhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Helper para operaciones de cancelación de solicitudes
 * Aplica el principio DRY (Don't Repeat Yourself)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CancelacionSolicitudHelper {

    private final SolicitudRepository solicitudRepository;
    private final IWhatsAppService whatsAppService;

    /**
     * Procesa la cancelación de una solicitud individual
     */
    public SolicitudCanceladaDTO procesarCancelacionSolicitud(Solicitud solicitud, Pedido pedido) {
        try {
            // Cambiar estado a CANCELADA
            solicitud.setEstadoSolicitud(EstadoSolicitudEnum.CAN);
            solicitudRepository.save(solicitud);

            // Intentar enviar notificación WhatsApp
            boolean notificacionEnviada = enviarNotificacionWhatsApp(solicitud, pedido);

            log.info(CancelacionConstants.LOG_SOLICITUD_CANCELADA, solicitud.getIdSolicitud());

            return construirSolicitudCanceladaDTO(solicitud, notificacionEnviada);

        } catch (Exception e) {
            log.error(CancelacionConstants.LOG_ERROR_CANCELACION, solicitud.getIdSolicitud(), e.getMessage());
            throw e;
        }
    }

    /**
     * Envía notificación WhatsApp y maneja errores
     */
    private boolean enviarNotificacionWhatsApp(Solicitud solicitud, Pedido pedido) {
        try {
            whatsAppService.notificarCancelacionSolicitud(
                    solicitud.getIdSolicitud(),
                    pedido.getIdPedido());
            log.info(CancelacionConstants.LOG_NOTIFICACION_ENVIADA, solicitud.getIdSolicitud());
            return true;
        } catch (Exception e) {
            log.warn(CancelacionConstants.LOG_ERROR_NOTIFICACION, solicitud.getIdSolicitud(), e.getMessage());
            return false;
        }
    }

    /**
     * Construye el DTO de solicitud cancelada
     */
    private SolicitudCanceladaDTO construirSolicitudCanceladaDTO(Solicitud solicitud, boolean notificacionEnviada) {
        return SolicitudCanceladaDTO.builder()
                .idSolicitud(solicitud.getIdSolicitud())
                .nombreCliente(solicitud.getCliente().getNombres() + " " +
                        solicitud.getCliente().getApellidos())
                .telefono(solicitud.getCliente().getTelefono())
                .notificacionEnviada(notificacionEnviada)
                .build();
    }

    /**
     * Construye respuesta cuando no hay solicitudes pendientes
     */
    public CancelacionSolicitudesDTO construirRespuestaSinSolicitudes(Integer idPedido, LocalDate fechaCierre) {
        return CancelacionSolicitudesDTO.builder()
                .idPedido(idPedido)
                .fechaCierre(fechaCierre)
                .fechaProceso(LocalDate.now())
                .totalSolicitudesCanceladas(0)
                .totalSolicitudesNoCanceladas(0)
                .mensaje(CancelacionConstants.MSG_SIN_SOLICITUDES_PENDIENTES)
                .exitoso(true)
                .build();
    }

    /**
     * Construye respuesta cuando no hay pedidos vencidos
     */
    public CancelacionSolicitudesDTO construirRespuestaSinPedidosVencidos() {
        return CancelacionSolicitudesDTO.builder()
                .fechaProceso(LocalDate.now())
                .totalSolicitudesCanceladas(0)
                .totalSolicitudesNoCanceladas(0)
                .mensaje(CancelacionConstants.MSG_SIN_PEDIDOS_VENCIDOS)
                .exitoso(true)
                .build();
    }

    /**
     * Construye respuesta exitosa con solicitudes canceladas
     */
    public CancelacionSolicitudesDTO construirRespuestaExitosa(
            Integer idPedido,
            LocalDate fechaCierre,
            int canceladas,
            int noCanceladas,
            int totalSolicitudes,
            List<SolicitudCanceladaDTO> solicitudesCanceladas) {

        return CancelacionSolicitudesDTO.builder()
                .idPedido(idPedido)
                .fechaCierre(fechaCierre)
                .fechaProceso(LocalDate.now())
                .totalSolicitudesCanceladas(canceladas)
                .totalSolicitudesNoCanceladas(noCanceladas)
                .solicitudesCanceladas(solicitudesCanceladas)
                .mensaje(String.format(CancelacionConstants.MSG_CANCELACION_EXITOSA, canceladas, totalSolicitudes))
                .exitoso(noCanceladas == 0)
                .build();
    }

    /**
     * Construye respuesta consolidada para cancelación masiva
     */
    public CancelacionSolicitudesDTO construirRespuestaMasiva(
            int totalPedidos,
            int totalCanceladas,
            int totalNoCanceladas,
            List<SolicitudCanceladaDTO> todasSolicitudesCanceladas,
            List<String> errores) {

        return CancelacionSolicitudesDTO.builder()
                .fechaProceso(LocalDate.now())
                .totalSolicitudesCanceladas(totalCanceladas)
                .totalSolicitudesNoCanceladas(totalNoCanceladas)
                .solicitudesCanceladas(todasSolicitudesCanceladas)
                .errores(errores)
                .mensaje(String.format(CancelacionConstants.MSG_CANCELACION_MASIVA_EXITOSA,
                        totalPedidos, totalCanceladas))
                .exitoso(true)
                .build();
    }
}
