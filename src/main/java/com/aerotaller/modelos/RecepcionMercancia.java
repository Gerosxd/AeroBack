package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RecepcionMercancia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionMercancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Integer idRecepcion;

    @Column(name = "folio", nullable = false, unique = true, length = 30)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_compra", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrada_articulo")
    private EntradaArticulo entradaArticulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_recibe", nullable = false)
    private Usuario usuarioRecibe;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "ruta_pdf", length = 255)
    private String rutaPdf;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Builder.Default
    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleRecepcionMercancia> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaRecepcion == null) {
            this.fechaRecepcion = LocalDateTime.now();
        }
    }
}