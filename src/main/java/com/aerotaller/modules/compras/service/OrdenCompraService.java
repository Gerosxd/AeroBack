package com.aerotaller.modules.compras.service;

import com.aerotaller.modules.compras.dto.OrdenCompraRequest;
import com.aerotaller.modules.compras.dto.OrdenCompraResponse;
import java.util.List;

public interface OrdenCompraService {
    OrdenCompraResponse generarOrdenCompra(OrdenCompraRequest request);
    List<OrdenCompraResponse> listarOrdenes();
}