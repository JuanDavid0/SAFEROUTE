package com.saferoute.service;

import com.saferoute.model.Solicitud;
import com.saferoute.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolicitudAuthorizationService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    /**
     * Valida que la solicitud pertenece al cliente con la cédula especificada
     * 
     * @param idSolicitud ID de la solicitud a verificar
     * @param cedula      Cédula del cliente (extraída del token OTP)
     * @throws RuntimeException si la solicitud no existe o no pertenece al cliente
     */
    public void validateSolicitudOwnership(Integer idSolicitud, String cedula) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id: " + idSolicitud));

        String cedulaCliente = solicitud.getCliente().getCedula();

        if (!cedulaCliente.equals(cedula)) {
            throw new RuntimeException("No tienes autorización para modificar esta solicitud. " +
                    "Solo puedes modificar tus propias solicitudes.");
        }
    }

    /**
     * Obtiene una solicitud validando que pertenece al cliente
     */
    public Solicitud getSolicitudWithOwnershipValidation(Integer idSolicitud, String cedula) {
        validateSolicitudOwnership(idSolicitud, cedula);
        return solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id: " + idSolicitud));
    }
}
