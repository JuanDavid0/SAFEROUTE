package com.saferoute.service.interfaces;

import com.saferoute.dto.CancelacionSolicitudesDTO;

/**
 * Servicio para gestión de cancelación de solicitudes.
 */
public interface ICancelacionSolicitudService {

    CancelacionSolicitudesDTO cancelarSolicitudesPendientes(Integer idPedido);

    CancelacionSolicitudesDTO cancelarTodasSolicitudesVencidas();
}
