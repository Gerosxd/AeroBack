package com.aerotaller.modelos;

import jakarta.persistence.*;

@Entity
@Table(name = "OTDiscrepancia")
public class OTDiscrepancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOTDiscrepancia")
    private Integer idOTDiscrepancia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OT", nullable = false)
    private NuevaOT ot;

    @Column(name = "Codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "Descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "Estatus", length = 100)
    private String estatus;

    @Column(name = "Acciones", length = 255)
    private String acciones;

    @Column(name = "Aeronavegable", length = 2)
    private String aeronavegable;

    @Column(name = "FechaAutorizada")
    private java.time.LocalDate fechaAutorizada;

    @Column(name = "AccionCorrectiva", length = 500)
    private String accionCorrectiva;

    @Column(name = "FechaLiberacion")
    private java.time.LocalDate fechaLiberacion;

    @Column(name = "EfectuadoPor", length = 150)
    private String efectuadoPor;

    @Column(name = "InspeccionadoPor", length = 150)
    private String inspeccionadoPor;

    @Column(name = "ParteAsociada", length = 255)
    private String parteAsociada;

    public OTDiscrepancia() {
    }

    public Integer getIdOTDiscrepancia() {
        return idOTDiscrepancia;
    }

    public void setIdOTDiscrepancia(Integer idOTDiscrepancia) {
        this.idOTDiscrepancia = idOTDiscrepancia;
    }

    public NuevaOT getOt() {
        return ot;
    }

    public void setOt(NuevaOT ot) {
        this.ot = ot;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getAcciones() {
        return acciones;
    }

    public void setAcciones(String acciones) {
        this.acciones = acciones;
    }

    public String getAeronavegable() {
        return aeronavegable;
    }

    public void setAeronavegable(String aeronavegable) {
        this.aeronavegable = aeronavegable;
    }

    public java.time.LocalDate getFechaAutorizada() {
        return fechaAutorizada;
    }

    public void setFechaAutorizada(java.time.LocalDate fechaAutorizada) {
        this.fechaAutorizada = fechaAutorizada;
    }

    public String getAccionCorrectiva() {
        return accionCorrectiva;
    }

    public void setAccionCorrectiva(String accionCorrectiva) {
        this.accionCorrectiva = accionCorrectiva;
    }

    public java.time.LocalDate getFechaLiberacion() {
        return fechaLiberacion;
    }

    public void setFechaLiberacion(java.time.LocalDate fechaLiberacion) {
        this.fechaLiberacion = fechaLiberacion;
    }

    public String getEfectuadoPor() {
        return efectuadoPor;
    }

    public void setEfectuadoPor(String efectuadoPor) {
        this.efectuadoPor = efectuadoPor;
    }

    public String getInspeccionadoPor() {
        return inspeccionadoPor;
    }

    public void setInspeccionadoPor(String inspeccionadoPor) {
        this.inspeccionadoPor = inspeccionadoPor;
    }

    public String getParteAsociada() {
        return parteAsociada;
    }

    public void setParteAsociada(String parteAsociada) {
        this.parteAsociada = parteAsociada;
    }
}