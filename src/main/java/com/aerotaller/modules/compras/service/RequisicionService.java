package com.aerotaller.modules.compras.service;

import com.aerotaller.modules.compras.dto.RequisicionRequest;
import com.aerotaller.modules.compras.dto.RequisicionResponse;
import java.util.List;

public interface RequisicionService {
    RequisicionResponse crear(RequisicionRequest request);
    RequisicionResponse obtenerPorId(Integer id);
    List<RequisicionResponse> listarTodas();
}