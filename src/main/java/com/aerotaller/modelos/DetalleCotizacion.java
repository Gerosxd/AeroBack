package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DetalleCotizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_cot")
    private Integer idDetalleCot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cotizacion", nullable = false)
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_req", nullable = false)
    private DetalleRequisicion detalleRequisicion;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "importe", nullable = false, precision = 14, scale = 2)
    private BigDecimal importe;

    @Column(name = "tiempo_entrega_dias")
    private Integer tiempoEntregaDias;
}