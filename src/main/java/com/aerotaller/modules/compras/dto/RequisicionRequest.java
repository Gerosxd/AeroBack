package com.aerotaller.modules.compras.dto;

import lombok.Data;
import java.util.List;

@Data
public class RequisicionRequest {
    private String folio;
    private Integer idUsuarioSolicita;
    private String observaciones;
    private List<DetalleRequisicionDTO> detalles;
}