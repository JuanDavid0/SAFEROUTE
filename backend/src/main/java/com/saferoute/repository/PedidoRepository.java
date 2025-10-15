package com.saferoute.repository;

import com.saferoute.model.Pedido;
import com.saferoute.model.Usuario;
import com.saferoute.model.enums.EstadoPedidoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByAdmin(Usuario admin);

    List<Pedido> findByEstadoPedido(EstadoPedidoEnum estado);
}
