package com.aerotaller.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "EstadoCompra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;
}