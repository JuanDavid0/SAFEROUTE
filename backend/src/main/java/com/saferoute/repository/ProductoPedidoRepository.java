package com.saferoute.repository;

import com.saferoute.model.ProductoPedido;
import com.saferoute.model.ProductoPedidoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoPedidoRepository extends JpaRepository<ProductoPedido, ProductoPedidoId> {
}
