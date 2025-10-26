package com.saferoute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para respuesta de cancelación de solicitudes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelacionSolicitudesDTO {

    private Integer idPedido;
    private LocalDate fechaCierre;
    private LocalDate fechaProceso;
    private Integer totalSolicitudesCanceladas;
    private Integer totalSolicitudesNoCanceladas;

    @Builder.Default
    private List<SolicitudCanceladaDTO> solicitudesCanceladas = new ArrayList<>();

    @Builder.Default
    private List<String> errores = new ArrayList<>();

    private String mensaje;
    private Boolean exitoso;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolicitudCanceladaDTO {
        private Integer idSolicitud;
        private String nombreCliente;
        private String telefono;
        private Boolean notificacionEnviada;
    }
}
