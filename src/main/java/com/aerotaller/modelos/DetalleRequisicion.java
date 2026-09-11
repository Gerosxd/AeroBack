package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetalleRequisicion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRequisicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_req")
    private Integer idDetalleReq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_requisicion", nullable = false)
    private RequisicionCompra requisicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_articulo")
    private Articulo articulo;

    @Column(name = "descripcion_libre", length = 255)
    private String descripcionLibre;

    @Column(name = "numero_parte_libre", length = 100)
    private String numeroParteLibre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadSolicitada;
}