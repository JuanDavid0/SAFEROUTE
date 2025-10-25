package com.saferoute.service.interfaces;

import java.io.ByteArrayOutputStream;

public interface IExcelService {

    ByteArrayOutputStream generarInformeCliente(Integer idCliente);

    ByteArrayOutputStream generarInformePedidoConsolidado(Integer idPedido);
}
