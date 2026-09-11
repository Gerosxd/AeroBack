package com.aerotaller.modules.compras.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RequisicionResponse {
    private Integer idRequisicion;
    private String folio;
    private Integer idUsuarioSolicita;
    private String nombreUsuario;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private String observaciones;
    private List<DetalleRequisicionDTO> detalles;
}