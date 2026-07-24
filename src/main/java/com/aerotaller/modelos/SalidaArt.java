package com.aerotaller.modelos;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "SalidaArt")
public class SalidaArt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSalidaArt;

    @Column(nullable = false)
    private String noSalida;

    @Column(nullable = false)
    private LocalDate fecha;

    // P-05: Referencia del documento (OT, requisición, etc.) — formato AG-145-22
    private String referencia;

    private String destinatario;

    private String direccionDestinatario;

    private String encargadoAlmacen;

    private String traslada;

    private String recibe;

    @OneToMany(mappedBy = "salidaArt", cascade = CascadeType.ALL)
    private List<DetalleSalidaArt> detalles;

    public SalidaArt() {}

    public Integer getIdSalida() {
        return idSalidaArt;
    }

    public void setIdSalidaArt(Integer idSalidaArt) {
        this.idSalidaArt = idSalidaArt;
    }

    public String getNoSalida() {
        return noSalida;
    }

    public void setNoSalida(String noSalida) {
        this.noSalida = noSalida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccionDestinatario() {
        return direccionDestinatario;
    }

    public void setDireccionDestinatario(String direccionDestinatario) {
        this.direccionDestinatario = direccionDestinatario;
    }

    public String getEncargadoAlmacen() {
        return encargadoAlmacen;
    }

    public void setEncargadoAlmacen(String encargadoAlmacen) {
        this.encargadoAlmacen = encargadoAlmacen;
    }

    public String getTraslada() {
        return traslada;
    }

    public void setTraslada(String traslada) {
        this.traslada = traslada;
    }

    public String getRecibe() {
        return recibe;
    }

    public void setRecibe(String recibe) {
        this.recibe = recibe;
    }

    public List<DetalleSalidaArt> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleSalidaArt> detalles) {
        this.detalles = detalles;
    }
}