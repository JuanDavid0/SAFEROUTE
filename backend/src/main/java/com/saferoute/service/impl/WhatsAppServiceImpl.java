package com.saferoute.service.impl;

import com.saferoute.constants.WhatsAppConstants;
import com.saferoute.dto.SolicitudDTO;
import com.saferoute.dto.whatsapp.*;
import com.saferoute.exception.ResourceNotFoundException;
import com.saferoute.helper.ClienteWhatsAppHelper;
import com.saferoute.helper.SolicitudMontoHelper;
import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.Usuario;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.IWhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implementación del servicio de notificaciones WhatsApp
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppServiceImpl implements IWhatsAppService {

    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.api.token}")
    private String apiToken;

    @Value("${whatsapp.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMontoHelper montoHelper;
    private final ClienteWhatsAppHelper clienteHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    public void enviarResumenSolicitud(SolicitudDTO solicitudDTO) {
        try {
            log.info(WhatsAppConstants.LOG_ENVIANDO_RESUMEN_SOLICITUD, solicitudDTO.getIdSolicitud());

            Solicitud solicitud = obtenerSolicitud(solicitudDTO.getIdSolicitud());
            log.info(WhatsAppConstants.LOG_CLIENTE_INFO,
                    solicitud.getCliente().getNombres(),
                    solicitud.getCliente().getTelefono());

            List<String> parametros = construirParametrosResumenSolicitud(solicitud);
            enviarMensajePlantilla(solicitud.getCliente().getTelefono(),
                    WhatsAppConstants.TEMPLATE_RESUMEN_SOLICITUD, parametros);

            log.info(WhatsAppConstants.LOG_RESUMEN_ENVIADO, solicitudDTO.getIdSolicitud());
        } catch (Exception e) {
            log.error(WhatsAppConstants.LOG_ERROR_RESUMEN,
                    solicitudDTO.getIdSolicitud(), e.getMessage(), e);
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }

    private Solicitud obtenerSolicitud(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", idSolicitud));
    }

    private List<String> construirParametrosResumenSolicitud(Solicitud solicitud) {
        BigDecimal total = montoHelper.calcularMontoTotal(solicitud.getProductos());
        String totalFormateado = CURRENCY_FORMATTER.format(total);
        String fechaLimite = solicitud.getPedido().getFechaCierre().format(DATE_FORMATTER);

        return List.of(
                String.valueOf(solicitud.getIdSolicitud()),
                totalFormateado,
                fechaLimite);
    }

    @Override
    public void notificarSolicitudPagada(SolicitudDTO solicitudDTO) {
        try {
            log.info(WhatsAppConstants.LOG_NOTIFICANDO_SOLICITUD_PAGADA, solicitudDTO.getIdSolicitud());

            Solicitud solicitud = obtenerSolicitud(solicitudDTO.getIdSolicitud());
            List<String> parametros = construirParametrosSolicitudPagada(solicitud);

            enviarMensajePlantilla(solicitud.getCliente().getTelefono(),
                    WhatsAppConstants.TEMPLATE_SOLICITUD_PAGADA, parametros);

            log.info(WhatsAppConstants.LOG_SOLICITUD_PAGADA_ENVIADA);
        } catch (Exception e) {
            log.error(WhatsAppConstants.LOG_ERROR_SOLICITUD_PAGADA, e.getMessage(), e);
        }
    }

    @Override
    public void notificarActualizacionSolicitud(Integer idSolicitud, Integer idPedido,
            String estadoAnterior, String estadoActual, String mensaje) {
        try {
            log.info(WhatsAppConstants.LOG_NOTIFICANDO_ACTUALIZACION_PEDIDO, idSolicitud, idPedido);

            Solicitud solicitud = obtenerSolicitud(idSolicitud);
            List<String> parametros = construirParametrosActualizacionSolicitud(
                    idSolicitud, idPedido, estadoAnterior, estadoActual, mensaje);

            enviarMensajePlantilla(solicitud.getCliente().getTelefono(),
                    WhatsAppConstants.TEMPLATE_ACTUALIZACION_PEDIDO, parametros);

            log.info(WhatsAppConstants.LOG_ACTUALIZACION_PEDIDO_ENVIADA, idSolicitud);
        } catch (Exception e) {
            log.error(WhatsAppConstants.LOG_ERROR_ACTUALIZACION_PEDIDO, idSolicitud, e.getMessage(), e);
        }
    }

    private List<String> construirParametrosActualizacionSolicitud(Integer idSolicitud, Integer idPedido,
            String estadoAnterior, String estadoActual, String mensaje) {
        return List.of(
                String.valueOf(idSolicitud),
                String.valueOf(idPedido),
                estadoAnterior,
                estadoActual,
                mensaje);
    }

    @Override
    public void notificarNuevoPedido(Pedido pedido) {
        try {
            log.info(WhatsAppConstants.LOG_NOTIFICANDO_NUEVO_PEDIDO, pedido.getIdPedido());

            List<Usuario> clientes = clienteHelper.filtrarClientesActivos(usuarioRepository.findAll());
            List<String> parametros = construirParametrosNuevoPedido(pedido);

            enviarNotificacionMasiva(clientes, WhatsAppConstants.TEMPLATE_NUEVO_PEDIDO, parametros);

            log.info(WhatsAppConstants.LOG_NUEVO_PEDIDO_ENVIADO, clientes.size());
        } catch (Exception e) {
            log.error(WhatsAppConstants.LOG_ERROR_NUEVO_PEDIDO, e.getMessage(), e);
        }
    }

    private List<String> construirParametrosNuevoPedido(Pedido pedido) {
        String fechaCierre = pedido.getFechaCierre().format(DATE_FORMATTER);
        String urlPedido = construirUrlPedido(pedido);

        return List.of(
                String.valueOf(pedido.getIdPedido()),
                fechaCierre,
                urlPedido);
    }

    @Override
    public void notificarCancelacionPedido(Pedido pedido, String motivo) {
        try {
            log.info(WhatsAppConstants.LOG_NOTIFICANDO_CANCELACION_PEDIDO, pedido.getIdPedido());

            // Solo notificar a clientes con solicitudes PAGADAS (PGD)
            List<Solicitud> solicitudesPagadas = solicitudRepository
                    .findByPedido_IdPedidoAndEstadoSolicitud(pedido.getIdPedido(), EstadoSolicitudEnum.PGD);
            List<String> telefonosNotificados = new ArrayList<>();

            // Construir parámetros: {{1}} = ID pedido, {{2}} = motivo
            List<String> parametros = List.of(
                    String.valueOf(pedido.getIdPedido()),
                    motivo);

            enviarNotificacionClientesPedido(solicitudesPagadas, WhatsAppConstants.TEMPLATE_CANCELACION_PEDIDO,
                    parametros, telefonosNotificados);

            log.info(WhatsAppConstants.LOG_CANCELACION_PEDIDO_ENVIADA, telefonosNotificados.size());
        } catch (Exception e) {
            log.error(WhatsAppConstants.LOG_ERROR_CANCELACION_PEDIDO, e.getMessage(), e);
        }
    }

    @Override
    public void notificarCancelacionSolicitud(Integer idSolicitud, Integer idPedido) {
        try {
            log.info(WhatsAppConstants.LOG_NOTIFICANDO_CANCELACION_SOLICITUD, idSolicitud);

            Solicitud solicitud = obtenerSolicitud(idSolicitud);
            List<String> parametros = List.of(
                    String.valueOf(idSolicitud),
                    String.valueOf(idPedido));

            enviarMensajePlantilla(solicitud.getCliente().getTelefono(),
                    WhatsAppConstants.TEMPLATE_CANCELACION_SOLICITUD, parametros);

            log.info(WhatsAppConstants.LOG_CANCELACION_SOLICITUD_ENVIADA);
        } catch (Exception e) {
            log.error(WhatsAppConstants.LOG_ERROR_CANCELACION_SOLICITUD, idSolicitud, e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String construirUrlPedido(Pedido pedido) {
        if (pedido.getUrlHash() != null && !pedido.getUrlHash().isEmpty()) {
            String url = baseUrl + String.format(WhatsAppConstants.URL_FORMAT_PEDIDO_HASH, pedido.getUrlHash());
            log.info(WhatsAppConstants.LOG_URL_PEDIDO_HASH, url);
            return url;
        } else {
            log.warn(WhatsAppConstants.LOG_PEDIDO_SIN_HASH, pedido.getIdPedido());
            return baseUrl + String.format(WhatsAppConstants.URL_FORMAT_PEDIDO_ID, pedido.getIdPedido());
        }
    }

    private void enviarNotificacionMasiva(List<Usuario> clientes, String nombrePlantilla,
            List<String> parametros) {
        for (Usuario cliente : clientes) {
            if (clienteHelper.esTelefonoValido(cliente.getTelefono())) {
                try {
                    enviarMensajePlantilla(cliente.getTelefono(), nombrePlantilla, parametros);
                } catch (Exception e) {
                    log.error(WhatsAppConstants.LOG_ERROR_ENVIO_MENSAJE, cliente.getTelefono(), e.getMessage());
                }
            }
        }
    }

    private void enviarNotificacionClientesPedido(List<Solicitud> solicitudes, String nombrePlantilla,
            List<String> parametros, List<String> telefonosNotificados) {
        for (Solicitud solicitud : solicitudes) {
            String telefono = solicitud.getCliente().getTelefono();

            if (clienteHelper.esTelefonoValido(telefono) && !telefonosNotificados.contains(telefono)) {
                try {
                    enviarMensajePlantilla(telefono, nombrePlantilla, parametros);
                    telefonosNotificados.add(telefono);
                } catch (Exception e) {
                    log.error(WhatsAppConstants.LOG_ERROR_ENVIO_MENSAJE, telefono, e.getMessage());
                }
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ACTIVOS ====================

    private List<String> construirParametrosSolicitudPagada(Solicitud solicitud) {
        BigDecimal total = montoHelper.calcularMontoTotal(solicitud.getProductos());
        String totalFormateado = CURRENCY_FORMATTER.format(total);
        String fechaConfirmacion = java.time.LocalDate.now().format(DATE_FORMATTER);

        return List.of(
                String.valueOf(solicitud.getIdSolicitud()),
                totalFormateado,
                fechaConfirmacion);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Envía un mensaje usando una plantilla de WhatsApp
     */
    private void enviarMensajePlantilla(String telefono, String nombrePlantilla, List<String> parametros) {
        // Determinar el código de idioma según la plantilla
        String languageCode = WhatsAppConstants.TEMPLATE_SOLICITUD_PAGADA.equals(nombrePlantilla)
                ? WhatsAppConstants.LANGUAGE_CODE_ES
                : WhatsAppConstants.LANGUAGE_CODE_ES_CO;

        enviarMensajePlantillaConIdioma(telefono, nombrePlantilla, parametros, languageCode);
    }

    /**
     * Envía un mensaje usando una plantilla de WhatsApp con código de idioma
     * específico
     */
    private void enviarMensajePlantillaConIdioma(String telefono, String nombrePlantilla,
            List<String> parametros, String languageCode) {
        try {
            String url = String.format(WhatsAppConstants.URL_FORMAT_MESSAGES_API, whatsappApiUrl, phoneNumberId);
            String numeroFormateado = clienteHelper.formatearNumeroTelefono(telefono);

            WhatsAppMessageRequest request = construirMensajeRequest(numeroFormateado, nombrePlantilla,
                    parametros, languageCode);
            HttpHeaders headers = construirHeaders();
            HttpEntity<WhatsAppMessageRequest> entity = new HttpEntity<>(request, headers);

            log.info(WhatsAppConstants.LOG_ENVIANDO_PLANTILLA, nombrePlantilla, numeroFormateado);
            log.debug(WhatsAppConstants.LOG_REQUEST_BODY, request);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info(WhatsAppConstants.LOG_PLANTILLA_ENVIADA,
                        nombrePlantilla, numeroFormateado, response.getBody());
            } else {
                log.error(WhatsAppConstants.LOG_ERROR_PLANTILLA,
                        response.getStatusCode(), response.getBody());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            manejarErrorHttp(e, telefono, nombrePlantilla);
            // No lanzamos excepción para no interrumpir el flujo principal
            log.warn(" Notificación de WhatsApp no enviada, pero el proceso continúa normalmente");
        } catch (Exception e) {
            log.error(WhatsAppConstants.ERROR_INESPERADO, telefono, e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
            log.warn(" Notificación de WhatsApp no enviada, pero el proceso continúa normalmente");
        }
    }

    private WhatsAppMessageRequest construirMensajeRequest(String numeroFormateado,
            String nombrePlantilla,
            List<String> parametros,
            String languageCode) {
        List<WhatsAppParameter> parameters = parametros.stream()
                .map(WhatsAppParameter::text)
                .toList();

        WhatsAppComponent component = WhatsAppComponent.builder()
                .type(WhatsAppConstants.COMPONENT_TYPE_BODY)
                .parameters(parameters)
                .build();

        WhatsAppTemplate template = WhatsAppTemplate.builder()
                .name(nombrePlantilla)
                .language(new WhatsAppLanguage(languageCode))
                .components(List.of(component))
                .build();

        return WhatsAppMessageRequest.builder()
                .messagingProduct(WhatsAppConstants.MESSAGING_PRODUCT)
                .recipientType(WhatsAppConstants.RECIPIENT_TYPE)
                .to(numeroFormateado)
                .type(WhatsAppConstants.MESSAGE_TYPE)
                .template(template)
                .build();
    }

    private HttpHeaders construirHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);
        return headers;
    }

    private void manejarErrorHttp(org.springframework.web.client.HttpClientErrorException e,
            String telefono,
            String nombrePlantilla) {
        int statusCode = e.getStatusCode().value();

        if (statusCode == WhatsAppConstants.HTTP_STATUS_UNAUTHORIZED) {
            log.error(WhatsAppConstants.ERROR_401_TOKEN_INVALIDO);
        } else if (statusCode == WhatsAppConstants.HTTP_STATUS_FORBIDDEN) {
            log.error(WhatsAppConstants.ERROR_403_NUMERO_NO_AUTORIZADO, telefono);
        } else if (statusCode == WhatsAppConstants.HTTP_STATUS_BAD_REQUEST) {
            log.error(WhatsAppConstants.ERROR_400_PLANTILLA_NO_ENCONTRADA, nombrePlantilla);
        } else {
            log.error(WhatsAppConstants.ERROR_HTTP_GENERICO, statusCode, telefono, e.getMessage());
        }
    }
}
