package com.aerotaller.modules.compras.service;

import com.aerotaller.modules.compras.dto.ComparativoCotizacionResponse;
import com.aerotaller.modules.compras.dto.CotizacionRequest;
import com.aerotaller.modules.compras.dto.CotizacionResponse;
import java.util.List;

public interface CotizacionService {
    CotizacionResponse registrarCotizacion(CotizacionRequest request);
    ComparativoCotizacionResponse obtenerComparativoPorRequisicion(Integer idRequisicion);
    List<CotizacionResponse> listarPorRequisicion(Integer idRequisicion);
}