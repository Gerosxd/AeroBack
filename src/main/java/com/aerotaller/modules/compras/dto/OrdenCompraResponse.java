package com.aerotaller.modules.compras.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class OrdenCompraResponse {
    private Integer idOrdenCompra;
    private String folio;
    private String proveedor;
    private String almacen;
    private String estado;
    private LocalDateTime fechaEmision;
    private LocalDate fechaEntregaEstimada;
    private String moneda;
    private BigDecimal total;
}