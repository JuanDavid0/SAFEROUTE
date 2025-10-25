package com.saferoute.repository;

import com.saferoute.model.Pedido;
import com.saferoute.model.Solicitud;
import com.saferoute.model.Usuario;
import com.saferoute.model.enums.EstadoSolicitudEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
        List<Solicitud> findByCliente(Usuario cliente);

        List<Solicitud> findByPedido(Pedido pedido);

        @Query("SELECT DISTINCT s FROM Solicitud s LEFT JOIN FETCH s.productos sp LEFT JOIN FETCH sp.producto WHERE s.cliente = :cliente")
        List<Solicitud> findByClienteWithProductos(@Param("cliente") Usuario cliente);

        @Query("SELECT DISTINCT s FROM Solicitud s LEFT JOIN FETCH s.productos sp LEFT JOIN FETCH sp.producto WHERE s.pedido = :pedido")
        List<Solicitud> findByPedidoWithProductos(@Param("pedido") Pedido pedido);

        List<Solicitud> findByCliente_Cedula(String cedula);

        List<Solicitud> findByEstadoSolicitud(EstadoSolicitudEnum estado);

        List<Solicitud> findByPedidoFechaCierreBeforeAndEstadoSolicitud(LocalDate fecha, EstadoSolicitudEnum estado);

        List<Solicitud> findByPedido_IdPedido(Integer idPedido);

        List<Solicitud> findByPedido_IdPedidoAndEstadoSolicitud(Integer idPedido, EstadoSolicitudEnum estado);

        // ==================== QUERIES PARA REPORTES ====================

        /**
         * Obtener solicitudes pagadas en un rango de fechas
         */
        @Query("SELECT s FROM Solicitud s WHERE s.estadoSolicitud = 'PGD' " +
                        "AND s.fechaSolicitud BETWEEN :fechaInicio AND :fechaFin")
        List<Solicitud> findSolicitudesPagadasEnRango(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        /**
         * Contar solicitudes por estado
         */
        @Query("SELECT COUNT(s) FROM Solicitud s WHERE s.estadoSolicitud = :estado")
        Long countByEstadoSolicitud(@Param("estado") EstadoSolicitudEnum estado);

        /**
         * Contar clientes únicos con solicitudes
         */
        @Query("SELECT COUNT(DISTINCT s.cliente) FROM Solicitud s")
        Long countClientesConSolicitudes();
}
