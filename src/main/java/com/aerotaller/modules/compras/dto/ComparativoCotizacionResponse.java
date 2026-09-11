package com.aerotaller.modules.compras.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ComparativoCotizacionResponse {
    private Integer idRequisicion;
    private String folioRequisicion;
    private List<ProveedorColumnaDTO> proveedores;
    private List<FilaComparativaDTO> filas;

    @Data
    @Builder
    public static class ProveedorColumnaDTO {
        private Integer idCotizacion;
        private Integer idProveedor;
        private String nombreProveedor;
        private String moneda;
        private BigDecimal totalCotizado;
        private Boolean esGanadora;
    }

    @Data
    @Builder
    public static class FilaComparativaDTO {
        private Integer idDetalleReq;
        private String descripcionArticulo;
        private String numeroParte;
        private BigDecimal cantidad;
        private String unidadMedida;
        private List<OfertaProveedorDTO> ofertas;
    }

    @Data
    @Builder
    public static class OfertaProveedorDTO {
        private Integer idCotizacion;
        private Integer idProveedor;
        private BigDecimal precioUnitario;
        private BigDecimal importe;
        private Integer tiempoEntregaDias;
    }
}