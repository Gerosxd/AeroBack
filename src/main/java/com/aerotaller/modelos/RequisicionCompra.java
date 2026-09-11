package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RequisicionCompra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisicionCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_requisicion")
    private Integer idRequisicion;

    @Column(name = "folio", nullable = false, unique = true, length = 30)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_solicita", nullable = false)
    private Usuario usuarioSolicita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoCompra estado;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Builder.Default
    @OneToMany(mappedBy = "requisicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleRequisicion> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDateTime.now();
        }
    }
}