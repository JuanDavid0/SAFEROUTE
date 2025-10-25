package com.saferoute.repository;

import com.saferoute.model.Pedido;
import com.saferoute.model.Usuario;
import com.saferoute.model.enums.EstadoPedidoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByAdmin(Usuario admin);

    List<Pedido> findByEstadoPedido(EstadoPedidoEnum estado);

    /**
     * Buscar pedido por URL hash
     */
    Optional<Pedido> findByUrlHash(String urlHash);

    /**
     * Verificar si existe un pedido con el hash dado
     */
    boolean existsByUrlHash(String urlHash);

    /**
     * Contar pedidos por estado
     */
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estadoPedido = :estado")
    Long countByEstadoPedido(@Param("estado") EstadoPedidoEnum estado);

    /**
     * Obtener pedidos en curso (ACT, RTA, ADU, DST)
     */
    @Query("SELECT p FROM Pedido p WHERE p.estadoPedido IN ('ACT', 'RTA', 'ADU') " +
            "ORDER BY p.fechaCreado DESC")
    List<Pedido> findPedidosEnCurso();
}
