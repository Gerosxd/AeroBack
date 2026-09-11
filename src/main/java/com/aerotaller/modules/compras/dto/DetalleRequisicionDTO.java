package com.aerotaller.modules.compras.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleRequisicionDTO {
    private Integer idDetalleReq;
    private Integer idArticulo;
    private String descripcionLibre;
    private String numeroParteLibre;
    private Integer idUnidadMedida;
    private String nombreUnidadMedida;
    private BigDecimal cantidadSolicitada;
}