package com.aerotaller.modules.compras.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrdenCompraRequest {
    private String folio;
    private Integer idCotizacionGanadora;
    private Integer idProveedor;
    private Integer idAlmacenDestino;
    private LocalDate fechaEntregaEstimada;
    private String moneda;
    private BigDecimal tipoCambio;
    private String condicionesPago;
}