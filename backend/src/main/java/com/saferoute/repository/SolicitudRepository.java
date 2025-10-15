package com.saferoute.repository;

import com.saferoute.model.Solicitud;
import com.saferoute.model.Usuario;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByCliente(Usuario cliente);

    List<Solicitud> findByEstadoSolicitud(EstadoSolicitudEnum estado);

    List<Solicitud> findByPedidoFechaCierreBeforeAndEstadoSolicitud(LocalDate fecha, EstadoSolicitudEnum estado);
}
