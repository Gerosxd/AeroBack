package com.aerotaller.modules.compras.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CotizacionResponse {
    private Integer idCotizacion;
    private Integer idSolicitudCot;
    private Integer idProveedor;
    private String nombreProveedor;
    private String folioCotizacionProveedor;
    private LocalDateTime fechaRecepcion;
    private String moneda;
    private BigDecimal tipoCambio;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private String rutaPdf;
    private Boolean esGanadora;
}