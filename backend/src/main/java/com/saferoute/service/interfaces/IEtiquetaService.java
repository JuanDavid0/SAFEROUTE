package com.saferoute.service.interfaces;

import com.saferoute.dto.EtiquetasResponseDTO;

import java.io.ByteArrayOutputStream;

public interface IEtiquetaService {

    EtiquetasResponseDTO generarEtiquetasJSON(Integer idPedido);

    ByteArrayOutputStream generarEtiquetasPDF(Integer idPedido);
}
