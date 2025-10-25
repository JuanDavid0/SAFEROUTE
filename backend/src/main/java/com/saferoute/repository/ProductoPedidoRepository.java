package com.saferoute.repository;

import com.saferoute.model.ProductoPedido;
import com.saferoute.model.ProductoPedidoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoPedidoRepository extends JpaRepository<ProductoPedido, ProductoPedidoId> {

    @Query("SELECT pp FROM ProductoPedido pp WHERE pp.pedido.idPedido = :idPedido AND pp.producto.idProducto = :idProducto")
    Optional<ProductoPedido> findByPedidoAndProducto(@Param("idPedido") Integer idPedido,
            @Param("idProducto") Integer idProducto);
}
