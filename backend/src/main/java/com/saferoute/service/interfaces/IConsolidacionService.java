package com.saferoute.service.interfaces;

import com.saferoute.dto.ConsolidacionDTO;
import java.util.Map;

public interface IConsolidacionService {
    ConsolidacionDTO consolidarPedido(Integer idPedido, Map<String, Object> opciones);
}