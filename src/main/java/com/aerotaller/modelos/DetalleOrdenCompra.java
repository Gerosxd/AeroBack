package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetalleOrdenCompra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_oc")
    private Integer idDetalleOc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_compra", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_req", nullable = false)
    private DetalleRequisicion detalleRequisicion;

    @Column(name = "cantidad_ordenada", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadOrdenada;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "importe", nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;
}