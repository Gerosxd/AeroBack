package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetalleRecepcionMercancia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRecepcionMercancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_recepcion")
    private Integer idDetalleRecepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recepcion", nullable = false)
    private RecepcionMercancia recepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_oc", nullable = false)
    private DetalleOrdenCompra detalleOrdenCompra;

    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadRecibida;

    @Column(name = "observaciones_item", length = 255)
    private String observacionesItem;
}