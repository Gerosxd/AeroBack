package com.aerotaller.modules.compras.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CotizacionRequest {
    private Integer idSolicitudCot;
    private Integer idProveedor;
    private String folioCotizacionProveedor;
    private String moneda;
    private BigDecimal tipoCambio;
    private String rutaPdf;
    private List<DetalleCotizacionDTO> detalles;

    @Data
    public static class DetalleCotizacionDTO {
        private Integer idDetalleReq;
        private BigDecimal precioUnitario;
        private Integer tiempoEntregaDias;
    }
}