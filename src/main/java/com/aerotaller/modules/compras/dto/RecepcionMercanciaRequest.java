package com.aerotaller.modules.compras.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RecepcionMercanciaRequest {
    private String folio;
    private Integer idOrdenCompra;
    private Integer idUsuarioRecibe;
    private Integer idEntradaArticulo; // Opcional, enlace a tu tabla EntradaArticulo si ya existe
    private String observaciones;
    private String rutaPdf;
    private List<DetalleRecepcionRequest> partidas;

    @Data
    public static class DetalleRecepcionRequest {
        private Integer idDetalleOc;
        private BigDecimal cantidadRecibida;
        private String observacionesItem;
    }
}