package com.aerotaller.modules.compras.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecepcionMercanciaResponse {
    private Integer idRecepcion;
    private String folio;
    private Integer idOrdenCompra;
    private String folioOrdenCompra;
    private String estadoOrdenResultante;
    private LocalDateTime fechaRecepcion;
    private String usuarioRecibe;
    private String observaciones;
    private List<DetalleRecepcionResponse> partidas;

    @Data
    @Builder
    public static class DetalleRecepcionResponse {
        private Integer idDetalleRecepcion;
        private Integer idDetalleOc;
        private String descripcionArticulo;
        private java.math.BigDecimal cantidadOrdenada;
        private java.math.BigDecimal cantidadRecibidaAhora;
        private java.math.BigDecimal totalRecibidoAcumulado;
        private java.math.BigDecimal saldoPendiente;
        private String observacionesItem;
    }
}