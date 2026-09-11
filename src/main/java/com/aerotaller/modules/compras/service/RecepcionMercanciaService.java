package com.aerotaller.modules.compras.service;

import com.aerotaller.modules.compras.dto.RecepcionMercanciaRequest;
import com.aerotaller.modules.compras.dto.RecepcionMercanciaResponse;
import java.util.List;

public interface RecepcionMercanciaService {
    RecepcionMercanciaResponse registrarRecepcion(RecepcionMercanciaRequest request);
    List<RecepcionMercanciaResponse> listarPorOrdenCompra(Integer idOrdenCompra);
}