package com.saferoute.service.impl;

import com.saferoute.dto.SolicitudDTO;
import com.saferoute.dto.whatsapp.*;
import com.saferoute.exception.ResourceNotFoundException;
import com.saferoute.model.Pedido;
import com.saferoute.model.Rol;
import com.saferoute.model.Solicitud;
import com.saferoute.model.Usuario;
import com.saferoute.repository.PedidoRepository;
import com.saferoute.repository.SolicitudRepository;
import com.saferoute.repository.UsuarioRepository;
import com.saferoute.service.interfaces.IWhatsAppService;
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
import java.util.Set;

/**
 * Implementación del servicio de notificaciones WhatsApp
 */
@Slf4j
@Service
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
    private final PedidoRepository pedidoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public WhatsAppServiceImpl(
            RestTemplate restTemplate,
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository) {
        this.restTemplate = restTemplate;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void enviarResumenSolicitud(SolicitudDTO solicitudDTO) {
        try {
            log.info("📨 Iniciando envío de resumen de solicitud #{} al cliente", solicitudDTO.getIdSolicitud());

            // Obtener la solicitud completa con relaciones
            Solicitud solicitud = solicitudRepository.findById(solicitudDTO.getIdSolicitud())
                    .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", solicitudDTO.getIdSolicitud()));

            log.info("📋 Cliente: {} - Teléfono: {}",
                    solicitud.getCliente().getNombres(),
                    solicitud.getCliente().getTelefono());

            // Calcular total
            BigDecimal total = solicitud.getProductos().stream()
                    .map(sp -> sp.getPrecio().multiply(BigDecimal.valueOf(sp.getCantidadSolicitada())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String totalFormateado = CURRENCY_FORMATTER.format(total);
            String fechaLimite = solicitud.getPedido().getFechaCierre().format(DATE_FORMATTER);
            String urlSolicitud = baseUrl + "/solicitud/" + solicitud.getIdSolicitud();

            // Preparar parámetros para la plantilla
            List<String> parametros = List.of(
                    String.valueOf(solicitud.getIdSolicitud()), // {{1}} - ID Solicitud
                    totalFormateado, // {{2}} - Total
                    fechaLimite, // {{3}} - Fecha límite
                    urlSolicitud // {{4}} - URL
            );

            // Enviar usando plantilla aprobada (sin límite de 24h)
            enviarMensajePlantilla(solicitud.getCliente().getTelefono(), "resumen_solicitud", parametros);

            log.info("✅ Resumen de solicitud #{} enviado exitosamente", solicitudDTO.getIdSolicitud());
        } catch (Exception e) {
            log.error("❌ Error al enviar resumen de solicitud #{}: {}",
                    solicitudDTO.getIdSolicitud(), e.getMessage(), e);
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }

    @Override
    public void notificarNuevoPedidoActivo(Pedido pedido) {
        try {
            log.info("📨 Notificando nuevo pedido activo #{} a todos los clientes", pedido.getIdPedido());

            // Obtener todos los usuarios con rol CLIENTE activos
            List<Usuario> clientes = usuarioRepository.findAll().stream()
                    .filter(u -> {
                        Set<Rol> roles = u.getRoles();
                        boolean esCliente = roles.stream().anyMatch(r -> "CLI".equals(r.getTipoRol()));
                        boolean estaActivo = "ACTIVO".equals(u.getEstadoUsuario());
                        return esCliente && estaActivo;
                    })
                    .toList();

            String fechaCierre = pedido.getFechaCierre().format(DATE_FORMATTER);

            // Usar el hash del pedido en la URL en lugar del ID
            String urlPedido;
            if (pedido.getUrlHash() != null && !pedido.getUrlHash().isEmpty()) {
                urlPedido = baseUrl + "/pedidos/pedido-disponible/" + pedido.getUrlHash();
                System.out.println("🔗 URL del pedido usando hash: " + urlPedido);
            } else {
                // Fallback al ID si por alguna razón no hay hash (no debería pasar)
                urlPedido = baseUrl + "/pedidos/pedido/" + pedido.getIdPedido();
                log.warn("⚠️ Pedido #{} no tiene URL hash, usando ID en la URL", pedido.getIdPedido());
            }

            // Preparar parámetros para la plantilla
            List<String> parametros = List.of(
                    String.valueOf(pedido.getIdPedido()), // {{1}} - ID Pedido
                    fechaCierre, // {{2}} - Fecha cierre
                    urlPedido // {{3}} - URL
            );

            for (Usuario cliente : clientes) {
                if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
                    try {
                        enviarMensajePlantilla(cliente.getTelefono(), "nuevo_pedido_activo", parametros);
                    } catch (Exception e) {
                        log.error("Error al enviar mensaje a {}: {}", cliente.getTelefono(), e.getMessage());
                    }
                }
            }

            log.info("✅ Notificación de nuevo pedido enviada a {} clientes", clientes.size());
        } catch (Exception e) {
            log.error("❌ Error al notificar nuevo pedido activo: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notificarCambioEstadoPedido(Pedido pedido, String estadoAnterior) {
        try {
            log.info("📨 Notificando cambio de estado del pedido #{}: {} -> {}",
                    pedido.getIdPedido(), estadoAnterior, pedido.getEstadoPedido());

            // Obtener clientes que tienen solicitudes en este pedido
            List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(pedido.getIdPedido());
            List<String> telefonosNotificados = new ArrayList<>();

            String estadoAnteriorTraducido = traducirEstado(estadoAnterior);
            String estadoActualTraducido = traducirEstado(pedido.getEstadoPedido().name());
            String mensajeSegunEstado = obtenerMensajeSegunEstado(pedido.getEstadoPedido().name());

            // Preparar parámetros para la plantilla
            List<String> parametros = List.of(
                    String.valueOf(pedido.getIdPedido()), // {{1}} - ID Pedido
                    estadoAnteriorTraducido, // {{2}} - Estado anterior
                    estadoActualTraducido, // {{3}} - Estado actual
                    mensajeSegunEstado // {{4}} - Mensaje según estado
            );

            for (Solicitud solicitud : solicitudes) {
                String telefono = solicitud.getCliente().getTelefono();

                // Evitar enviar duplicados al mismo número
                if (telefono != null && !telefono.isEmpty() && !telefonosNotificados.contains(telefono)) {
                    try {
                        enviarMensajePlantilla(telefono, "cambio_estado_pedido", parametros);
                        telefonosNotificados.add(telefono);
                    } catch (Exception e) {
                        log.error("Error al enviar mensaje a {}: {}", telefono, e.getMessage());
                    }
                }
            }

            log.info("✅ Notificación de cambio de estado enviada a {} clientes", telefonosNotificados.size());
        } catch (Exception e) {
            log.error("❌ Error al notificar cambio de estado: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notificarCancelacionPedido(Pedido pedido) {
        try {
            log.info("📨 Notificando cancelación del pedido #{}", pedido.getIdPedido());

            // Obtener clientes afectados
            List<Solicitud> solicitudes = solicitudRepository.findByPedido_IdPedido(pedido.getIdPedido());
            List<String> telefonosNotificados = new ArrayList<>();

            // Preparar parámetros para la plantilla
            List<String> parametros = List.of(
                    String.valueOf(pedido.getIdPedido()) // {{1}} - ID Pedido
            );

            for (Solicitud solicitud : solicitudes) {
                String telefono = solicitud.getCliente().getTelefono();

                if (telefono != null && !telefono.isEmpty() && !telefonosNotificados.contains(telefono)) {
                    try {
                        enviarMensajePlantilla(telefono, "pedido_cancelado", parametros);
                        telefonosNotificados.add(telefono);
                    } catch (Exception e) {
                        log.error("Error al enviar mensaje a {}: {}", telefono, e.getMessage());
                    }
                }
            }

            log.info("✅ Notificación de cancelación enviada a {} clientes", telefonosNotificados.size());
        } catch (Exception e) {
            log.error("❌ Error al notificar cancelación: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notificarSolicitudPagada(SolicitudDTO solicitudDTO) {
        try {
            log.info("📨 Notificando solicitud pagada #{} al cliente", solicitudDTO.getIdSolicitud());

            // Obtener la solicitud completa
            Solicitud solicitud = solicitudRepository.findById(solicitudDTO.getIdSolicitud())
                    .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", solicitudDTO.getIdSolicitud()));

            // Calcular total
            BigDecimal total = solicitud.getProductos().stream()
                    .map(sp -> sp.getPrecio().multiply(BigDecimal.valueOf(sp.getCantidadSolicitada())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String totalFormateado = CURRENCY_FORMATTER.format(total);

            // Preparar parámetros para la plantilla
            List<String> parametros = List.of(
                    String.valueOf(solicitud.getIdSolicitud()), // {{1}} - ID Solicitud
                    totalFormateado // {{2}} - Monto total
            );

            enviarMensajePlantilla(solicitud.getCliente().getTelefono(), "solicitud_pagada", parametros);

            log.info("✅ Notificación de solicitud pagada enviada exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al notificar solicitud pagada: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía un mensaje usando una plantilla de WhatsApp
     */
    private void enviarMensajePlantilla(String telefono, String nombrePlantilla, List<String> parametros) {
        try {
            String url = String.format("%s/%s/messages", whatsappApiUrl, phoneNumberId);
            String numeroFormateado = formatearNumeroTelefono(telefono);

            // Construir lista de parámetros
            List<WhatsAppParameter> parameters = parametros.stream()
                    .map(WhatsAppParameter::text)
                    .toList();

            // Construir componente con los parámetros
            WhatsAppComponent component = WhatsAppComponent.builder()
                    .type("body")
                    .parameters(parameters)
                    .build();

            // Construir plantilla
            WhatsAppTemplate template = WhatsAppTemplate.builder()
                    .name(nombrePlantilla)
                    .language(WhatsAppLanguage.spanish())
                    .components(List.of(component))
                    .build();

            // Construir request
            WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
                    .messagingProduct("whatsapp")
                    .recipientType("individual")
                    .to(numeroFormateado)
                    .type("template")
                    .template(template)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);

            HttpEntity<WhatsAppMessageRequest> entity = new HttpEntity<>(request, headers);

            log.info("📤 Enviando plantilla '{}' a WhatsApp - Número: {}", nombrePlantilla, numeroFormateado);
            log.debug("📋 Request body: {}", request);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Plantilla '{}' enviada exitosamente a {} - Response: {}",
                        nombrePlantilla, numeroFormateado, response.getBody());
            } else {
                log.error("⚠️ Error al enviar plantilla. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                log.error("❌ ERROR 401: Token de WhatsApp inválido o expirado");
            } else if (e.getStatusCode().value() == 403) {
                log.error("❌ ERROR 403: Número {} no autorizado", telefono);
            } else if (e.getStatusCode().value() == 400) {
                log.error("❌ ERROR 400: Plantilla '{}' no encontrada o no aprobada. " +
                        "Verifica que la plantilla esté creada y aprobada en Meta for Developers", nombrePlantilla);
            } else {
                log.error("❌ Error HTTP {} al enviar plantilla a {}: {}",
                        e.getStatusCode().value(), telefono, e.getMessage());
            }
            throw new RuntimeException("Error al enviar plantilla de WhatsApp", e);
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar plantilla a {}: {}", telefono, e.getMessage());
            throw new RuntimeException("Error al enviar plantilla de WhatsApp", e);
        }
    }

    /**
     * Traduce el código de estado a texto legible
     */
    private String traducirEstado(String estado) {
        return switch (estado) {
            case "ACT" -> "Activo";
            case "CRD" -> "Cerrado";
            case "RTA" -> "Listo para Aduanas";
            case "ADU" -> "En Aduanas";
            case "DST" -> "En Distribución";
            case "ENT" -> "Entregado";
            case "CRM" -> "Cancelado (Manual)";
            case "CRA" -> "Cancelado (Automático)";
            default -> estado;
        };
    }

    /**
     * Obtiene mensaje adicional según el estado
     */
    private String obtenerMensajeSegunEstado(String estado) {
        return switch (estado) {
            case "RTA" -> "🎯 Tu pedido está listo para aduanas.\n¡Pronto estará en camino!";
            case "ADU" -> "🛃 Tu pedido está en proceso de aduanas.\nEsto puede tomar algunos días.";
            case "DST" -> "🚚 Tu pedido está en distribución.\n¡Llegará pronto a tu destino!";
            case "ENT" -> "🎊 ¡Tu pedido ha sido entregado!\nGracias por confiar en nosotros.";
            default -> "Mantente atento a las próximas actualizaciones.";
        };
    }

    /**
     * Formatea el número de teléfono para WhatsApp
     * Debe ser en formato: código de país + número (sin +)
     * Ejemplo: 573001234567
     */
    private String formatearNumeroTelefono(String telefono) {
        // Remover caracteres no numéricos
        String limpio = telefono.replaceAll("[^0-9]", "");

        // Si no empieza con código de país (57 para Colombia), agregarlo
        if (!limpio.startsWith("57") && limpio.length() == 10) {
            limpio = "57" + limpio;
        }

        return limpio;
    }
}
